package nl.vlasje24.repository;

import nl.vlasje24.domain.ChartEntry;
import nl.vlasje24.domain.ChartEntryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartEntryWriteRepository extends JpaRepository<ChartEntry, ChartEntryId> {
}
