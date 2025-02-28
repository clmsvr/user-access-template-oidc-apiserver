package cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cms.domain.model.Authority;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

}
