package cms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cms.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByOidcId(String oidcId);
	
	List<User> findByEmail(String email);
}
