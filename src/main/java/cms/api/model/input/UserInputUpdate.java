package cms.api.model.input;

import cms.annotations.ValidaEstados;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInputUpdate
{
	@NotBlank
    private String name;
	
    private String city;
    
    @ValidaEstados
    private String state;
    
    private String comment;       
}
