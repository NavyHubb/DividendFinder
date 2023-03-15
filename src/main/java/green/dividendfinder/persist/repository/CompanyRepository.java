package green.dividendfinder.persist.repository;

import green.dividendfinder.persist.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByTicker(String ticker);
    Optional<CompanyEntity> findByName(String name);
}
