package digit.service;

import digit.config.PGRConfiguration;
import digit.kafka.Producer;
import digit.repository.ServiceRequestRepository;
import digit.validator.ServiceRequestValidator;
import digit.web.models.PGREntity;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import digit.util.MdmsUtil;
import java.util.*;

@org.springframework.stereotype.Service
public class PGRService {



   private EnrichmentService enrichmentService;
//
//    private UserService userService;
//
//    private WorkflowService workflowService;
//
      private ServiceRequestValidator validator;
//
//    private ServiceRequestValidator validator;
//
      private Producer producer;
//
      private PGRConfiguration config;
//
      private ServiceRequestRepository repository;
//
//    private MDMSUtils mdmsUtils;


    @Autowired
    public PGRService(EnrichmentService enrichmentService, Producer producer, PGRConfiguration config, ServiceRequestRepository repository, ServiceRequestValidator validator) {
        this.enrichmentService = enrichmentService;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
        this.validator = validator;
    }


    /**
     * Creates a complaint in the system
     * @param request The service request containg the complaint information
     * @return
     */
    public ServiceRequest create(ServiceRequest request){
        String tenantId = request.getPgrEntity().getService().getTenantId();
        //Object mdmsData = MdmsUtil.mDMSCall(request);
        //validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
        //workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request.getPgrEntity());
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */

    /// TODO: still working
    public List<PGREntity> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateSearch(requestInfo, criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        if(criteria.getMobileNumber()!=null && CollectionUtils.isEmpty(criteria.getUserIds()))
            return new ArrayList<>();

        criteria.setIsPlainSearch(false);

        List<PGREntity> serviceWrappers = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(serviceWrappers))
            return new ArrayList<>();;

        //userService.enrichUsers(serviceWrappers);
        List<ServiceWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo,serviceWrappers);
        Map<Long, List<ServiceWrapper>> sortedWrappers = new TreeMap<>(Collections.reverseOrder());
        for(ServiceWrapper svc : enrichedServiceWrappers){
            if(sortedWrappers.containsKey(svc.getService().getAuditDetails().getCreatedTime())){
                sortedWrappers.get(svc.getService().getAuditDetails().getCreatedTime()).add(svc);
            }else{
                List<ServiceWrapper> serviceWrapperList = new ArrayList<>();
                serviceWrapperList.add(svc);
                sortedWrappers.put(svc.getService().getAuditDetails().getCreatedTime(), serviceWrapperList);
            }
        }
        List<ServiceWrapper> sortedServiceWrappers = new ArrayList<>();
        for(Long createdTimeDesc : sortedWrappers.keySet()){
            sortedServiceWrappers.addAll(sortedWrappers.get(createdTimeDesc));
        }
        return sortedServiceWrappers;
    }


    public Map<String, Integer> getDynamicData(String tenantId) {

        Map<String,Integer> dynamicData = repository.fetchDynamicData(tenantId);

        return dynamicData;
    }

    public int getComplaintTypes() {

        return Integer.valueOf(config.getComplaintTypes());
    }
}