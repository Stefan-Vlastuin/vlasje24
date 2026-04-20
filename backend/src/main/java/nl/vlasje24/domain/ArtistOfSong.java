package nl.vlasje24.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artist_of_song")
@Getter
@NoArgsConstructor
public class ArtistOfSong {

    @EmbeddedId
    private ArtistOfSongId id;

    @ManyToOne(optional = false)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @ManyToOne(optional = false)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "artist_order", nullable = false)
    private Byte artistOrder;

    public ArtistOfSong(Song song, Artist artist, Byte artistOrder) {
        this.id = new ArtistOfSongId(song.getSongId(), artist.getArtistId());
        this.song = song;
        this.artist = artist;
        this.artistOrder = artistOrder;
    }
}
