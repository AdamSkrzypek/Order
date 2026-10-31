package org.example.order.repositories;

import org.example.order.domain.entity.OrderVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderVersionRepository extends JpaRepository<OrderVersion, UUID>,
        JpaSpecificationExecutor<OrderVersion> {
}
