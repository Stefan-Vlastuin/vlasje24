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
public class ChartEntryId implements Serializable {

    @Column(name = "week_id")
    private Integer weekId;

    @Column(name = "position")
    private Integer position;

    public ChartEntryId(Integer weekId, Integer position) {
        this.weekId = weekId;
        this.position = position;
    }
}
