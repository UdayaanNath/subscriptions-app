package org.demo.com.subscriptionsapp.api.dto.searchCriteria;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class BaseSearchCriteria {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private int page = 0;
    private int size = 0;

    public Pageable toPageable() {
        int pageNumber = Math.max(page, 0);
        int pageSize = size < 1 ? DEFAULT_PAGE_SIZE : size;
        return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
    }
}
