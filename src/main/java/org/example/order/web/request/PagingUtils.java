package org.example.order.web.request;

import lombok.experimental.UtilityClass;
import org.example.order.domain.entity.AbstractEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PagingUtils {

    public Pageable toPageable(DefaultPageRequest request) {

        Sort sort = Sort.by(request.direction(), request.sortBy().property())
                .and(Sort.by(Sort.Direction.DESC, AbstractEntity.Fields.id));

        return PageRequest.of(request.page(), request.size(), sort);
    }
}
