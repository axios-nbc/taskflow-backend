package org.example.taskflowd.domain.dashboard.mock.repository;

import java.util.ArrayList;
import java.util.List;

import org.example.taskflowd.domain.dashboard.mock.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamRepositoryMock {

	public List<Team> findTeamsByUserId(Long userId) {
		// Mock 데이터 반환 - 빈 리스트
		return new ArrayList<>();
	}
}