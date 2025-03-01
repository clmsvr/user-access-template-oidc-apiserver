package cms.api.model.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleInput
{
	@NotBlank
    private String name;
    private String description;
    
}
