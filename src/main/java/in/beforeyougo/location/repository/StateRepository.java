package in.beforeyougo.location.repository;

import in.beforeyougo.location.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {

    @Query("SELECT s FROM State s WHERE LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (s.nameLocal IS NOT NULL AND s.nameLocal LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY s.nameEn")
    List<State> searchByName(@Param("query") String query);

    long count();
}
