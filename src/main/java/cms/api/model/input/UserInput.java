package cms.api.model.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserInput
{
	@NotBlank
    private String name;
	
    private String city;
    private String state;
    
    private String comment;       
}
