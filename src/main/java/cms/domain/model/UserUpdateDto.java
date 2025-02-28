package cms.domain.model;

import lombok.Data;

@Data
public class UserUpdateDto
{
    private String name;
	
    private String city;
    private String state;
    
    private String comment;       
}
