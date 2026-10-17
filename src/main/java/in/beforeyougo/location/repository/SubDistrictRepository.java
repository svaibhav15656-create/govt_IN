package in.beforeyougo.location.repository;

import in.beforeyougo.location.entity.SubDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubDistrictRepository extends JpaRepository<SubDistrict, Integer> {

    @Query("SELECT sd FROM SubDistrict sd WHERE LOWER(sd.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (sd.nameLocal IS NOT NULL AND sd.nameLocal LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY sd.nameEn")
    List<SubDistrict> searchByName(@Param("query") String query);

    List<SubDistrict> findByDistrictCode(@Param("districtCode") Integer districtCode);

    long count();
}
