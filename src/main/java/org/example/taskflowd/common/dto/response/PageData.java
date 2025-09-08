package org.example.taskflowd.common.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageData<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
    public static <T> PageData<T> from(Page<T> p) {
        return new PageData<>(
                List.copyOf(p.getContent()), // 불변 노출
                p.getTotalElements(),
                p.getTotalPages(),
                p.getSize(),
                p.getNumber()
        );
    }
}
