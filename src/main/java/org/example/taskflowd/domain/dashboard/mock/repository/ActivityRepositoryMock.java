package org.example.taskflowd.domain.dashboard.mock.repository;

import java.util.ArrayList;
import java.util.List;

import org.example.taskflowd.domain.dashboard.mock.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ActivityRepositoryMock {

	public Page<Activity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
		// Mock 데이터 반환 - 빈 페이지
		List<Activity> mockActivities = new ArrayList<>();
		return new PageImpl<>(mockActivities, pageable, 0);
	}
}
