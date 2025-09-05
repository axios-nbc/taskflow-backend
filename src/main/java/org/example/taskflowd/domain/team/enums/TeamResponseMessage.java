package org.example.taskflowd.domain.team.enums;

import jakarta.annotation.Resource;
import lombok.Getter;
import org.example.taskflowd.common.enums.ResponseMessage;
import org.example.taskflowd.domain.team.entity.TeamMember;

// 각자 Message를 명세서에 따라 등록하시면 됩니다!
@Getter
public enum TeamResponseMessage implements ResponseMessage {

    //TEAM
    TEAM_LIST_INQUIRE("팀 목록을 조회했습니다."),
    TEAM_OBJECT_INQUIRE("팀 정보를 조회했습니다."),
    TEAM_CREATED("팀이 생성되었습니다."),
    TEAM_UPDATED("팀 정보가 수정되었습니다."),
    TEAM_DELETED("팀이 삭제되었습니다."),
    TEAM_MEMBER_LIST_INQUIRE("팀 멤버 목록을 조회했습니다."),
    TEAM_MEMBER_ADDED("팀 멤버가 추가되었습니다."),
    TEAM_MEMBER_REMOVED("팀 멤버가 제거되었습니다."),
    AVAILABLE_USERS_INQUIRE("사용 가능한 사용자 목록을 조회했습니다.");

    private final String message;

    TeamResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
