package cms.api.model.input;

import org.hibernate.validator.constraints.Length;

import cms.annotations.ValidaEstados;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInputCreate
{
	@NotBlank
    private String name;
	
	@NotBlank
	@Email
    private String email;
	
    private String city;
    
    @ValidaEstados
    private String state;
    
    private String comment;       
}
