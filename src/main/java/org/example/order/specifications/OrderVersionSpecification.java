package org.example.order.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.order.domain.entity.AbstractEntity;
import org.example.order.domain.entity.OrderVersion;
import org.example.order.web.request.FilterDto;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class OrderVersionSpecification implements Specification<OrderVersion> {

    private final UUID orderId;
    private final FilterDto filter;

    public OrderVersionSpecification(UUID orderId, FilterDto filter) {
        this.orderId = orderId;
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<OrderVersion> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(
                root.get(OrderVersion.Fields.order).get(AbstractEntity.Fields.id), orderId));

        if (filter != null) {
            if (filter.previousOrderStatus() != null) {
                predicates.add(cb.equal(root.get(OrderVersion.Fields.previousOrderStatus),
                        filter.previousOrderStatus()));
            }
            if (filter.nextOrderStatus() != null) {
                predicates.add(cb.equal(root.get(OrderVersion.Fields.nextOrderStatus),
                        filter.nextOrderStatus()));
            }
            if (filter.previousPaymentStatus() != null) {
                predicates.add(cb.equal(root.get(OrderVersion.Fields.previousPaymentStatus),
                        filter.previousPaymentStatus()));
            }
            if (filter.nextPaymentStatus() != null) {
                predicates.add(cb.equal(root.get(OrderVersion.Fields.nextPaymentStatus),
                        filter.nextPaymentStatus()));
            }
            if (filter.sourceOfChange() != null) {
                predicates.add(cb.equal(root.get(OrderVersion.Fields.sourceOfChange),
                        filter.sourceOfChange()));
            }
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }

}
