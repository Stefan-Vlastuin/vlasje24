package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.Artist;
import nl.vlasje24.domain.ArtistOfSong;
import nl.vlasje24.dto.ArtistDetailDto;
import nl.vlasje24.dto.SongDetailDto;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ArtistRepository;
import nl.vlasje24.repository.ChartEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final SongService songService;
    private final ChartEntryRepository chartEntryRepository;

    @Transactional(readOnly = true)
    public ArtistDetailDto getArtistDetail(Integer artistId) {
        Artist artist = artistRepository.findByIdWithSongs(artistId)
                .orElseThrow(() -> new NotFoundException("Artist not found: " + artistId));

        // Only include songs that actually have chart entries
        List<SongDetailDto> songs = artist.getSongs().stream()
                .map(ArtistOfSong::getSong)
                .filter(song -> !chartEntryRepository.findBySongIdWithChartWeek(song.getSongId()).isEmpty())
                .map(song -> songService.getSongDetail(song.getSongId()))
                .toList();

        int hitCount = songs.size();
        int totalPoints = songs.stream().mapToInt(SongDetailDto::totalPoints).sum();

        return new ArtistDetailDto(artist.getArtistId(), artist.getName(), hitCount, totalPoints, songs);
    }
}
