package digit.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.PGRService;
import digit.util.PGRConstants;
import digit.util.ResponseInfoFactory;
import digit.web.models.PGREntity;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import digit.web.models.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import javax.validation.Valid;

//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Controller
@RequestMapping("/pgr-service")
@Slf4j
public class RequestAPIController {

    private final ObjectMapper objectMapper;

    private PGRService pgrService;

    private ResponseInfoFactory responseInfoFactory;


    @Autowired
    public RequestAPIController(ObjectMapper objectMapper, PGRService pgrService, ResponseInfoFactory responseInfoFactory) {
        this.objectMapper = objectMapper;
        this.pgrService = pgrService;
        this.responseInfoFactory = responseInfoFactory;
    }


    @RequestMapping(value="/request/_create", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestsCreatePost(@Valid @RequestBody ServiceRequest request) throws IOException {
        System.out.println(request);
        ServiceRequest enrichedReq = pgrService.create(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        PGREntity serviceWrapper = PGREntity.builder().service(enrichedReq.getPgrEntity().getService()).build();
        ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).pgREntities(Collections.singletonList(serviceWrapper)).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/request/_search", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestsSearchPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                              @Valid @ModelAttribute RequestSearchCriteria criteria) {

        String tenantId = criteria.getTenantId();
        List<PGREntity> serviceWrappers = pgrService.search(requestInfoWrapper.getRequestInfo(), criteria);
        Map<String,Integer> dynamicData = pgrService.getDynamicData(tenantId);

        int complaintsResolved = dynamicData.get(PGRConstants.COMPLAINTS_RESOLVED);
        int averageResolutionTime = dynamicData.get(PGRConstants.AVERAGE_RESOLUTION_TIME);
        int complaintTypes = pgrService.getComplaintTypes();

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
        ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).serviceWrappers(serviceWrappers).complaintsResolved(complaintsResolved)
                .averageResolutionTime(averageResolutionTime).complaintTypes(complaintTypes).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


//
//    @RequestMapping(value="/request/_update", method = RequestMethod.POST)
//    public ResponseEntity<ServiceResponse> requestsUpdatePost(@Valid @RequestBody ServiceRequest request) throws IOException {
//        ServiceRequest enrichedReq = pgrService.update(request);
//        ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(enrichedReq.getService()).workflow(enrichedReq.getWorkflow()).build();
//        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
//        ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).serviceWrappers(Collections.singletonList(serviceWrapper)).build();
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    @RequestMapping(value="/request/_count", method = RequestMethod.POST)
//    public ResponseEntity<CountResponse> requestsCountPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
//                                                           @Valid @ModelAttribute RequestSearchCriteria criteria) {
//        Integer count = pgrService.count(requestInfoWrapper.getRequestInfo(), criteria);
//        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
//        CountResponse response = CountResponse.builder().responseInfo(responseInfo).count(count).build();
//        return new ResponseEntity<>(response, HttpStatus.OK);
//
//    }

}
