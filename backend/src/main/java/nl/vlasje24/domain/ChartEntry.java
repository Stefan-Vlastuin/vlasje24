package nl.vlasje24.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chart")
@Getter
@NoArgsConstructor
public class ChartEntry {

    @EmbeddedId
    private ChartEntryId id;

    @ManyToOne(optional = false)
    @MapsId("weekId")
    @JoinColumn(name = "week_id")
    private ChartWeek chartWeek;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "song_id")
    private Song song;

    public ChartEntry(ChartWeek chartWeek, Song song, Integer position) {
        this.id = new ChartEntryId(chartWeek.getWeekId(), position);
        this.chartWeek = chartWeek;
        this.song = song;
    }
}
