package nl.vlasje24.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class ArtistOfSongId implements Serializable {

    @Column(name = "song_id")
    private Integer songId;

    @Column(name = "artist_id")
    private Integer artistId;

    public ArtistOfSongId(Integer songId, Integer artistId) {
        this.songId = songId;
        this.artistId = artistId;
    }
}
