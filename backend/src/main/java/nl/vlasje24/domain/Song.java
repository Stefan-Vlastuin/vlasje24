package nl.vlasje24.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "song")
@Getter
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private Integer songId;

    @Column(nullable = false)
    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "preview_url")
    private String previewUrl;

    public Song(String title, String imageUrl, String previewUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
    }

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("artistOrder ASC")
    @BatchSize(size = 50)
    private List<ArtistOfSong> artists = new ArrayList<>();

    @OneToMany(mappedBy = "song")
    private List<ChartEntry> chartEntries = new ArrayList<>();
}
