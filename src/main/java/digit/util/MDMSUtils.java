package digit.util;

import digit.config.PGRConfiguration;
import digit.repository.ServiceRequestRepository;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static digit.util.PGRConstants.MDMS_MODULE_NAME;
import static digit.util.PGRConstants.MDMS_SERVICEDEF;

@Component
public class MDMSUtils {



    private PGRConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public MDMSUtils(PGRConfiguration config, ServiceRequestRepository serviceRequestRepository) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * Calls MDMS service to fetch pgr master data
     * @param request
     * @return
     */
    public Object mDMSCall(ServiceRequest request){
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getPgrEntity().getService().getTenantId();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo,tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        return result;
    }


    /**
     * Returns mdms search criteria based on the tenantId
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo,String tenantId){
        List<ModuleDetail> pgrModuleRequest = getPGRModuleRequest();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.addAll(pgrModuleRequest);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }


    /**
     * Creates request to search serviceDef from MDMS
     * @return request to search UOM from MDMS
     */
    private List<ModuleDetail> getPGRModuleRequest() {

        // master details for TL module
        List<MasterDetail> pgrMasterDetails = new ArrayList<>();

        // filter to only get code field from master data
        final String filterCode = "$.[?(@.active==true)]";

        pgrMasterDetails.add(MasterDetail.builder().name(MDMS_SERVICEDEF).filter(filterCode).build());

        ModuleDetail pgrModuleDtls = ModuleDetail.builder().masterDetails(pgrMasterDetails)
                .moduleName(MDMS_MODULE_NAME).build();


        return Collections.singletonList(pgrModuleDtls);

    }


    /**
     * Returns the url for mdms search endpoint
     *
     * @return url for mdms search endpoint
     */
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }

}