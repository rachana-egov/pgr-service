package digit.web.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.models.Document;
import org.egov.common.contract.workflow.State;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a PGR workflow
 */
@ApiModel(description = "A Object holds the basic data for a PGR workflow")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = { "id" })
@ToString
public class ProcessInstance {

    @Size(max = 64)
    @JsonProperty("id")
    private String id;

    @NotNull
    @Size(max = 128)
    @JsonProperty("tenantId")
    private String tenantId;

    @NotNull
    @Size(max = 128)
    @JsonProperty("businessService")
    private String businessService;

    @NotNull
    @Size(max = 128)
    @JsonProperty("businessId")
    private String businessId;

    @NotNull
    @Size(max = 128)
    @JsonProperty("action")
    private String action;

    @NotNull
    @Size(max = 64)
    @JsonProperty("moduleName")
    private String moduleName;

    @JsonProperty("state")
    private State state;

    private String notificationAction;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("documents")
    @Valid
    private List<Document> documents;

    @JsonProperty("assignes")
    private List<User> assignes;

    @JsonProperty("assigner")
    private User assigner = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    @JsonProperty("businesssServiceSla")
    private Long businesssServiceSla = null;

    public ProcessInstance addDocumentsItem(Document documentsItem) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        if (!this.documents.contains(documentsItem))
            this.documents.add(documentsItem);

        return this;
    }

}
