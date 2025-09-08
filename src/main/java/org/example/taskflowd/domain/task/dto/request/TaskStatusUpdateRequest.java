package org.example.taskflowd.domain.task.dto.request;

import jakarta.validation.constraints.*;
import org.example.taskflowd.domain.task.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskStatusUpdateRequest(
//  @NotBlank
  TaskStatus status
){
}
