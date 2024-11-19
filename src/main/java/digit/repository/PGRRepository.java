package digit.repository;

import digit.repository.rowmapper.PGRQueryBuilder;
import digit.repository.rowmapper.PGRRowMapper;
import digit.util.PGRConstants;
import digit.util.PGRUtils;
import digit.web.models.PGREntity;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.Service;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.Workflow;
import org.egov.tracer.model.CustomException;
import org.egov.common.exception.InvalidTenantIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class PGRRepository {


    private PGRQueryBuilder queryBuilder;

    private PGRRowMapper rowMapper;

    private JdbcTemplate jdbcTemplate;

    private PGRUtils utils;


    @Autowired
    public PGRRepository(PGRQueryBuilder queryBuilder, PGRRowMapper rowMapper, JdbcTemplate jdbcTemplate, PGRUtils utils) {
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }




    /**
     * searches services based on search criteria and then wraps it into serviceWrappers
     * @param criteria
     * @return
     */
    public List<PGREntity> getServiceWrappers(RequestSearchCriteria criteria){
        List<Service> services = getServices(criteria);
        List<String> serviceRequestids = services.stream().map(Service::getServiceRequestId).collect(Collectors.toList());
        Map<String, Workflow> idToWorkflowMap = new HashMap<>();
        List<PGREntity> serviceWrappers = new ArrayList<>();

        for(Service service : services){
            Workflow workflow = idToWorkflowMap.get(service.getServiceRequestId());
            if (workflow == null) {
                workflow = new Workflow(); // or handle appropriately based on the business logic
            }
            PGREntity serviceWrapper = PGREntity.builder().service(service).workflow(workflow).build();
            serviceWrappers.add(serviceWrapper);
        }
        return serviceWrappers;
    }

    /**
     * searches services based on search criteria
     * @param criteria
     * @return
     */
    public List<Service> getServices(RequestSearchCriteria criteria) {

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getPGRSearchQuery(criteria, preparedStmtList);
        List<Service> services =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return services;
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public Integer getCount(RequestSearchCriteria criteria) {

        String tenantId = criteria.getTenantId();
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getCountQuery(criteria, preparedStmtList);
        try {
            query = utils.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_REQUEST_COUNT_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        return count;
    }


    public Map<String, Integer> fetchDynamicData(String tenantId) {
        List<Object> preparedStmtListCompalintsResolved = new ArrayList<>();
        String query = queryBuilder.getResolvedComplaints(tenantId,preparedStmtListCompalintsResolved );
//        try {
//            query = utils.replaceSchemaPlaceholder(query, tenantId);
//        } catch (Exception e) {
//            throw new CustomException("PGR_SEARCH_ERROR",
//                    "TenantId length is not sufficient to replace query schema in a multi state instance");
//        }
        int complaintsResolved = jdbcTemplate.queryForObject(query,preparedStmtListCompalintsResolved.toArray(),Integer.class);

        List<Object> preparedStmtListAverageResolutionTime = new ArrayList<>();
        query = queryBuilder.getAverageResolutionTime(tenantId, preparedStmtListAverageResolutionTime);
//        try {
//            query = utils.replaceSchemaPlaceholder(query, tenantId);
//        } catch (Exception e) {
//            throw new CustomException("PGR_SEARCH_ERROR",
//                    "TenantId length is not sufficient to replace query schema in a multi state instance");
//        }
        int averageResolutionTime = jdbcTemplate.queryForObject(query, preparedStmtListAverageResolutionTime.toArray(),Integer.class);

        Map<String, Integer> dynamicData = new HashMap<String,Integer>();
        dynamicData.put(PGRConstants.COMPLAINTS_RESOLVED, complaintsResolved);
        dynamicData.put(PGRConstants.AVERAGE_RESOLUTION_TIME, averageResolutionTime);

        return dynamicData;
    }



}
