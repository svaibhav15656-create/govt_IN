package in.beforeyougo.location.repository;

import in.beforeyougo.location.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {

    @Query("SELECT d FROM District d WHERE LOWER(d.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (d.nameLocal IS NOT NULL AND d.nameLocal LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY d.nameEn")
    List<District> searchByName(@Param("query") String query);

    List<District> findByStateCode(@Param("stateCode") Integer stateCode);

    long count();
}
