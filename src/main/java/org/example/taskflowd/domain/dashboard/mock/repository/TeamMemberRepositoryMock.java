package org.example.taskflowd.domain.dashboard.mock.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TeamMemberRepositoryMock {

	public List<Object[]> getTeamProgressStats(List<Long> teamIds) {
		// Mock 데이터 반환 - 빈 리스트
		return new ArrayList<>();
	}
}
