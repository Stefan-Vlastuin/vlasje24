package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.*;
import nl.vlasje24.dto.CreateArtistDto;
import nl.vlasje24.dto.CreateChartDto;
import nl.vlasje24.dto.CreateSongDto;
import nl.vlasje24.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final ArtistOfSongRepository artistOfSongRepository;
    private final ChartWeekRepository chartWeekRepository;
    private final ChartEntryRepository chartEntryRepository;

    @Transactional
    public Integer createArtist(CreateArtistDto dto) {
        Artist artist = new Artist(dto.name());
        return artistRepository.save(artist).getArtistId();
    }

    @Transactional
    public Integer createSong(CreateSongDto dto) {
        Song song = new Song(dto.title(), dto.imageUrl(), dto.previewUrl());
        song = songRepository.save(song);

        List<Integer> artistIds = dto.artistIds();
        for (int i = 0; i < artistIds.size(); i++) {
            Artist artist = artistRepository.findById(artistIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Artiest niet gevonden"));
            artistOfSongRepository.save(new ArtistOfSong(song, artist, (byte) (i + 1)));
        }

        return song.getSongId();
    }

    @Transactional
    public Integer createChart(CreateChartDto dto) {
        if (dto.songIds().size() != 24) {
            throw new IllegalArgumentException("Een chart moet precies 24 nummers bevatten");
        }

        ChartWeek chartWeek = chartWeekRepository.save(new ChartWeek(dto.date()));

        List<Integer> songIds = dto.songIds();
        for (int i = 0; i < songIds.size(); i++) {
            Song song = songRepository.findById(songIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Nummer niet gevonden"));
            chartEntryRepository.save(new ChartEntry(chartWeek, song, i + 1));
        }

        return chartWeek.getWeekId();
    }
}
