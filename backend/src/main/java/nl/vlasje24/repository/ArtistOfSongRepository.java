package nl.vlasje24.repository;

import nl.vlasje24.domain.ArtistOfSong;
import nl.vlasje24.domain.ArtistOfSongId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistOfSongRepository extends JpaRepository<ArtistOfSong, ArtistOfSongId> {
}
