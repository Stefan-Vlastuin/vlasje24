package nl.vlasje24.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "date")
@Getter
@NoArgsConstructor
public class ChartWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "week_id")
    private Integer weekId;

    @Column(nullable = false)
    private LocalDate date;

    public ChartWeek(LocalDate date) {
        this.date = date;
    }

    @OneToMany(mappedBy = "chartWeek")
    @OrderBy("id.position ASC")
    private List<ChartEntry> entries = new ArrayList<>();
}
