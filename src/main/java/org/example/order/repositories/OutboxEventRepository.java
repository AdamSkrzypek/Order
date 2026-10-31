package org.example.order.repositories;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.example.order.domain.entity.OutboxEvent;
import org.example.order.domain.enums.OutboxEventStatus;
import org.hibernate.Timeouts;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = "" + Timeouts.SKIP_LOCKED_MILLI))
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<OutboxEvent> findAndLockByStatus(@Param("status") OutboxEventStatus status, Pageable pageable);
}

