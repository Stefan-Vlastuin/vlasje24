package nl.vlasje24.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artist")
@Getter
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private Integer artistId;

    @Column(nullable = false)
    private String name;

    public Artist(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "artist")
    private List<ArtistOfSong> songs = new ArrayList<>();
}
