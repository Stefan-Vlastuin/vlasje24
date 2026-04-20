package nl.vlasje24.repository;

import nl.vlasje24.domain.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Integer> {

    @Query("""
            SELECT DISTINCT s FROM Song s
            LEFT JOIN FETCH s.artists aos
            LEFT JOIN FETCH aos.artist
            WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<Song> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM Song s
            LEFT JOIN FETCH s.artists aos
            LEFT JOIN FETCH aos.artist
            WHERE s.songId = :songId
            """)
    Optional<Song> findByIdWithArtists(@Param("songId") Integer songId);
}
