package cms.api.doc;

import java.util.List;

import cms.api.model.AuthorityModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Roles")
public interface RoleAuthorityControllerDoc {

	@Operation(summary = "Lista as Authorities associadas a uma Role.")
	List<AuthorityModel> list(Long roleId);

	@Operation(summary = "Desassocia uma Authorities a uma Role.")
	void unlinkAuthority(Long roleId, Long authorityId);

	@Operation(summary = "Associa uma Authorities a uma Role.")
	void linkAuthority(Long roleId, Long authorityId);

}