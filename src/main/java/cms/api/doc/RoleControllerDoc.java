package cms.api.doc;

import java.util.List;

import cms.api.model.RoleModel;
import cms.api.model.input.RoleInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Roles")
@SecurityRequirement(name = "security_auth")
public interface RoleControllerDoc {

	@Operation(summary = "Lista Roles cadastradas.")
	List<RoleModel> list();

	@Operation(summary = "Buca Role por id.")
	RoleModel recover(long id);
	
	@Operation(summary = "Cria nova Role")
	RoleModel create(RoleInput input);

	@Operation(summary = "Atualiza uma Role cadastrada.")
	RoleModel update(long id, RoleInput input);

	@Operation(summary = "Deleta uma Role cadastrada.d")
	void delete(long id);

}