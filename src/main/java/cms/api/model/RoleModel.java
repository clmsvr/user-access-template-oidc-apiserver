package cms.api.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class RoleModel
{
	private Long id;	
    private String name;
    private String description;
    
	private Set<AuthorityModel> authorities = new HashSet<>();
}
