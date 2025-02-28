package cms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cms.domain.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	@Query("from Role r left join fetch r.authorities")
	@Override
	List<Role> findAll();
}
