package cms.domain.model;

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
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // !!!
@Entity
public class Role
{
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
	
	@NotBlank
    private String name;
    private String description;
    
    @ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "role_has_authority", 
	  joinColumns = @JoinColumn(name = "role_id"), 
	  inverseJoinColumns = @JoinColumn(name = "authority_id"))	
	private Set<Authority> authorities = new HashSet<>();
    
    
	public boolean removeAuthority(Authority authority) {
		//Atenção, precisa ddefinir HashCode() and Equals() 
	    return getAuthorities().remove(authority);
	}

	public boolean addAuthority(Authority authority) {
		
		//Atenção, precisa ddefinir HashCode() and Equals() 
		//eh um Set
		return getAuthorities().add(authority);

//quando nao eh um Set, para nao haver duplicação	
//		var list = getAuthorities();
//		if (list.contains(authority))
//			return false;
//	    list.add(authority);
//	    return true;
	}     
}
