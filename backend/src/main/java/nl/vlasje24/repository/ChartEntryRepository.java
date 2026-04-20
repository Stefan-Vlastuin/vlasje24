package nl.vlasje24.repository;

import nl.vlasje24.domain.ChartEntry;
import nl.vlasje24.domain.ChartEntryId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChartEntryRepository extends JpaRepository<ChartEntry, ChartEntryId> {

    @Query("""
            SELECT ce FROM ChartEntry ce
            JOIN FETCH ce.chartWeek
            WHERE ce.song.songId = :songId
            ORDER BY ce.chartWeek.date ASC
            """)
    List<ChartEntry> findBySongIdWithChartWeek(@Param("songId") Integer songId);

    List<ChartEntry> findByIdWeekId(Integer weekId);

    @Query("""
            SELECT ce.song.songId, SUM(25 - ce.id.position), COUNT(ce), MIN(ce.id.position)
            FROM ChartEntry ce JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            GROUP BY ce.song.songId
            ORDER BY SUM(25 - ce.id.position) DESC, COUNT(ce) DESC, MIN(ce.id.position) ASC
            """)
    List<Object[]> findTopByPoints(@Param("year") Integer year, Pageable pageable);

    @Query("""
            SELECT ce.song.songId, SUM(25 - ce.id.position), COUNT(ce), MIN(ce.id.position)
            FROM ChartEntry ce JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            GROUP BY ce.song.songId
            ORDER BY COUNT(ce) DESC, SUM(25 - ce.id.position) DESC, MIN(ce.id.position) ASC
            """)
    List<Object[]> findTopByWeeks(@Param("year") Integer year, Pageable pageable);

    @Query("""
            SELECT ce.song.songId, SUM(25 - ce.id.position), COUNT(ce), MIN(ce.id.position)
            FROM ChartEntry ce JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            GROUP BY ce.song.songId
            ORDER BY MIN(ce.id.position) ASC, SUM(25 - ce.id.position) DESC, COUNT(ce) DESC
            """)
    List<Object[]> findTopByPosition(@Param("year") Integer year, Pageable pageable);

    @Query("""
            SELECT COUNT(DISTINCT ce.song.songId)
            FROM ChartEntry ce JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            """)
    long countDistinctSongs(@Param("year") Integer year);

    @Query("""
            SELECT aos.artist.artistId, SUM(25 - ce.id.position), COUNT(DISTINCT ce.song.songId)
            FROM ChartEntry ce JOIN ce.song.artists aos JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            GROUP BY aos.artist.artistId
            ORDER BY SUM(25 - ce.id.position) DESC, COUNT(DISTINCT ce.song.songId) DESC
            """)
    List<Object[]> findTopArtistsByPoints(@Param("year") Integer year, Pageable pageable);

    @Query("""
            SELECT aos.artist.artistId, SUM(25 - ce.id.position), COUNT(DISTINCT ce.song.songId)
            FROM ChartEntry ce JOIN ce.song.artists aos JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            GROUP BY aos.artist.artistId
            ORDER BY COUNT(DISTINCT ce.song.songId) DESC, SUM(25 - ce.id.position) DESC
            """)
    List<Object[]> findTopArtistsByHits(@Param("year") Integer year, Pageable pageable);

    @Query("""
            SELECT COUNT(DISTINCT aos.artist.artistId)
            FROM ChartEntry ce JOIN ce.song.artists aos JOIN ce.chartWeek cw
            WHERE (:year IS NULL OR YEAR(cw.date) = :year)
            """)
    long countDistinctArtists(@Param("year") Integer year);
}
