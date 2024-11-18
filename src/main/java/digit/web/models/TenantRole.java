package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.Role;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * User role carries the tenant related role information for the user. A user can have multiple roles per tenant based on the need of the tenant. A user may also have multiple roles for multiple tenants.
 */
@Schema(description = "User role carries the tenant related role information for the user. A user can have multiple roles per tenant based on the need of the tenant. A user may also have multiple roles for multiple tenants.")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:12:16.237571556+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRole   {
        @JsonProperty("tenantId")
          @NotNull

                private String tenantId = null;

        @JsonProperty("roles")
          @NotNull
          @Valid
                private List<Role> roles = new ArrayList<>();


        public TenantRole addRolesItem(Role rolesItem) {
        this.roles.add(rolesItem);
        return this;
        }

}
