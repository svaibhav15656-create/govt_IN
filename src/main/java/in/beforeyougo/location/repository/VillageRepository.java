package in.beforeyougo.location.repository;

import in.beforeyougo.location.entity.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Integer> {

    @Query("SELECT v FROM Village v WHERE LOWER(v.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (v.nameLocal IS NOT NULL AND v.nameLocal LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY v.nameEn")
    List<Village> searchByName(@Param("query") String query);

    List<Village> findBySubDistrictCode(@Param("subDistrictCode") Integer subDistrictCode);

    long count();
}
