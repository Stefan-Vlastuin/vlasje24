package nl.vlasje24.repository;

import nl.vlasje24.domain.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Integer> {

    List<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
            SELECT DISTINCT a FROM Artist a
            LEFT JOIN FETCH a.songs aos
            LEFT JOIN FETCH aos.song s
            WHERE a.artistId = :artistId
            """)
    Optional<Artist> findByIdWithSongs(@Param("artistId") Integer artistId);
}
