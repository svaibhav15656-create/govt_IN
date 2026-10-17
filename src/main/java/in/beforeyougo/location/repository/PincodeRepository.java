package in.beforeyougo.location.repository;

import in.beforeyougo.location.entity.Pincode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PincodeRepository extends JpaRepository<Pincode, Long> {

    List<Pincode> findByPincode(@Param("pincode") String pincode);

    @Query("SELECT p FROM Pincode p WHERE p.pincode LIKE CONCAT(:prefix, '%') ORDER BY p.pincode")
    List<Pincode> searchByPrefix(@Param("prefix") String prefix);

    Optional<Pincode> findByPincodeAndVillageCode(String pincode, Integer villageCode);

    long count();
}
