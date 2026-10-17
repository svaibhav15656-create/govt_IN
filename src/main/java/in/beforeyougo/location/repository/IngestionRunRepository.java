package in.beforeyougo.location.repository;

import in.beforeyougo.location.entity.IngestionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {

    List<IngestionRun> findByRunTypeOrderByStartedAtDesc(String runType);
}
