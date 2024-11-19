package digit.service;

import digit.config.PGRConfiguration;
import digit.kafka.Producer;
import digit.repository.PGRRepository;
import digit.repository.ServiceRequestRepository;
import digit.util.MDMSUtils;
import digit.validator.ServiceRequestValidator;
import digit.web.models.PGREntity;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.*;

@org.springframework.stereotype.Service
public class PGRService {



   private EnrichmentService enrichmentService;
//
    private UserService userService;
//
    private WorkflowService workflowService;
//
      private ServiceRequestValidator validator;
//
//    private ServiceRequestValidator validator;
//
      private Producer producer;
//
      private PGRConfiguration config;
//
      private PGRRepository repository;
//
      private MDMSUtils mdmsUtils;


    @Autowired
    public PGRService(EnrichmentService enrichmentService, Producer producer, PGRConfiguration config, PGRRepository repository, ServiceRequestValidator validator, MDMSUtils mdmsUtils, WorkflowService workflowService, UserService userService) {
        this.enrichmentService = enrichmentService;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
        this.validator = validator;
        this.mdmsUtils = mdmsUtils;
        this.workflowService = workflowService;
        this.userService = userService;
    }


    /**
     * Creates a complaint in the system
     * @param request The service request containg the complaint information
     * @return
     */
    public ServiceRequest create(ServiceRequest request){
        String tenantId = request.getPgrEntity().getService().getTenantId();
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request.getPgrEntity());
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */

    public List<PGREntity> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateSearch(requestInfo, criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        if(criteria.getMobileNumber()!=null && CollectionUtils.isEmpty(criteria.getUserIds()))
            return new ArrayList<>();

        criteria.setIsPlainSearch(false);

        List<PGREntity> pgrEntities = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(pgrEntities))
            return new ArrayList<>();;

        userService.enrichUsers(pgrEntities);
        List<PGREntity> enrichedPgrWrappers = workflowService.enrichWorkflow(requestInfo,pgrEntities);
        Map<Long, List<PGREntity>> sortedPgrEntities = new TreeMap<>(Collections.reverseOrder());
        for(PGREntity svc : enrichedPgrWrappers){
            if(sortedPgrEntities.containsKey(svc.getService().getAuditDetails().getCreatedTime())){
                sortedPgrEntities.get(svc.getService().getAuditDetails().getCreatedTime()).add(svc);
            }else{
                List<PGREntity> pgrEntityList = new ArrayList<>();
                pgrEntityList.add(svc);
                sortedPgrEntities.put(svc.getService().getAuditDetails().getCreatedTime(), pgrEntityList);
            }
        }
        List<PGREntity> sortedPgrWrappers = new ArrayList<>();
        for(Long createdTimeDesc : sortedPgrEntities.keySet()){
            sortedPgrWrappers.addAll(sortedPgrEntities.get(createdTimeDesc));
        }
       //return serviceWrappers;
        return sortedPgrWrappers;
    }

    /**
     * Returns the total number of comaplaints matching the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params for which count is required
     * @return
     */

    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        criteria.setIsPlainSearch(false);
        Integer count = repository.getCount(criteria);
        return count;
    }

    /**
     * Updates the complaint (used to forward the complaint from one application status to another)
     * @param request The request containing the complaint to be updated
     * @return The updated complaint
     * @return
     */
    public ServiceRequest update(ServiceRequest request){
        // Call mdms to fetch the master data
        Object mdmsData = mdmsUtils.mDMSCall(request);

        // Validate the update request
        validator.validateUpdate(request, mdmsData);

        // Enrich the update request
        enrichmentService.enrichUpdateRequest(request);

        // Update the workflow status
        workflowService.updateWorkflowStatus(request);

        // Push the updated complaint to kafka
        producer.push(config.getUpdateTopic(),request.getPgrEntity());

        // Return the updated complaint
        return request;
    }


    public Map<String, Integer> getDynamicData(String tenantId) {

        Map<String,Integer> dynamicData = repository.fetchDynamicData(tenantId);

        return dynamicData;
    }

//    public int getComplaintTypes() {
//
//        return Integer.valueOf(config.getComplaintTypes());
//    }
}