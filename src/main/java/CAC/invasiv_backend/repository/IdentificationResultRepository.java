package CAC.invasiv_backend.repository;

import CAC.invasiv_backend.dto.IdentificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentificationResultRepository extends JpaRepository<IdentificationResult, Long> {
}

