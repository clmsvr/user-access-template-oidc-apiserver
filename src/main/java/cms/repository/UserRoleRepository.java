package cms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cms.domain.model.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	
    //Query com Multi bag problem
	//@Query("from UserRole u left join fetch u.roles r left join fetch r.permissions where u.oidcId = :oidcId ")
	UserRole findByOidcId(@Param("oidcId") String oidcId);

    @Query(value = """
    		select concat('ROLE_' , r.name) from user u, user_has_role ur, role r
			where u.id = ur.user_id and ur.role_id = r.id 
			and u.oidc_id = :oidcId
			union
			select a.name from user u, user_has_role ur, role r, role_has_authority ra, authority a
			where u.id = ur.user_id and ur.role_id = r.id and r.id = ra.role_id and ra.authority_id = a.id
			and u.oidc_id = :oidcId    		
    		""",
    nativeQuery = true)
    List<String> listAuthoritiesByOidcId(@Param("oidcId") String oidcId);
    
}
