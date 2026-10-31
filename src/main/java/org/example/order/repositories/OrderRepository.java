package org.example.order.repositories;

import jakarta.persistence.LockModeType;
import org.example.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);

    Page<Order> findByDeletedFalse(Pageable pageable);

    @Transactional(readOnly = true)
    @Query(value = """
            SELECT o.id
            FROM orders o
            WHERE o.payment_status IN ('NOT_PAID', 'FAILED')
              AND o.created_at <= :cutoff
              AND (:afterId IS NULL OR o.id > :afterId)
            ORDER BY o.id ASC
            LIMIT :batchSize
            """, nativeQuery = true)
    List<UUID> findExpiredOrderIds(
            @Param("cutoff") Instant cutoff,
            @Param("afterId") UUID afterId,
            @Param("batchSize") int batchSize);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("""
    DELETE FROM OrderItem item
    WHERE item.order.id IN (
        SELECT o.id FROM Order o WHERE o.createdAt <= :cutoff
    )
    """)
    int deleteOldOrderItems(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("""
    DELETE FROM OrderVersion version
    WHERE version.order.id IN (
        SELECT o.id FROM Order o WHERE o.createdAt <= :cutoff
    )
    """)
    int deleteOldOrderVersions(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("DELETE FROM Order o WHERE o.createdAt <= :cutoff")
    int deleteOldOrders(@Param("cutoff") Instant cutoff);

}
