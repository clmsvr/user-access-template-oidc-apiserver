package cms.api.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class UserModel
{
	//private Long id;
	
	@NotBlank
	private String oidcId;
	@NotBlank
	private String providerName; 
	
	@NotBlank
    private String email; 
	@NotBlank
    private String name;
	
    private String city;
    private String state;
    
//    private int    numBlocksSubtitled;
//    private int    numBlocksTranslated;    
    
    private String comment;       
    
    private LocalDateTime   creationDate = LocalDateTime.now() ;
    private LocalDateTime   updateDate = LocalDateTime.now() ; 
}
