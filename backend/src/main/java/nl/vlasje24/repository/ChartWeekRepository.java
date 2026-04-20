package nl.vlasje24.repository;

import nl.vlasje24.domain.ChartWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChartWeekRepository extends JpaRepository<ChartWeek, Integer> {

    Optional<ChartWeek> findTopByOrderByWeekIdDesc();

    @Query("""
            SELECT DISTINCT cw FROM ChartWeek cw
            LEFT JOIN FETCH cw.entries ce
            LEFT JOIN FETCH ce.song s
            WHERE cw.weekId = :weekId
            ORDER BY ce.id.position ASC
            """)
    Optional<ChartWeek> findByWeekIdWithEntries(@Param("weekId") Integer weekId);

    boolean existsByWeekId(Integer weekId);

    @Query("SELECT DISTINCT YEAR(cw.date) FROM ChartWeek cw ORDER BY YEAR(cw.date) DESC")
    List<Integer> findDistinctYears();
}
