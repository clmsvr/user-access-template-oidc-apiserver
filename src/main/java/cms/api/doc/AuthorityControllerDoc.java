package cms.api.doc;

import java.util.List;

import cms.api.model.AuthorityModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authorities")
@SecurityRequirement(name = "security_auth")
public interface AuthorityControllerDoc {

	@Operation(summary = "Lista todas as Authorities cadastradas no sistema.")
	List<AuthorityModel> list();

}