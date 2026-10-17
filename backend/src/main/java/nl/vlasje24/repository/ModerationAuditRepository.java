package nl.vlasje24.repository;

import nl.vlasje24.domain.ModerationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationAuditRepository extends JpaRepository<ModerationAudit, Long> {

    @Override
    @EntityGraph(attributePaths = {"admin", "targetUser", "targetComment"})
    Page<ModerationAudit> findAll(Pageable pageable);
}
