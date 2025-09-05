package org.example.taskflowd.domain.task.dto.request;

import jakarta.validation.constraints.*;
import org.example.taskflowd.domain.task.enums.TaskPriority;

import java.time.LocalDateTime;

public record TaskCreateRequest(
  @NotBlank
  @Size(max = 140)
  String title,

  @NotBlank
  @Size(max = 1000)
  String description,

  @NotNull
  @FutureOrPresent
  LocalDateTime dueDate,

  //@NotBlank
  TaskPriority priority,

  //@NotBlank
  @Positive
  Long assigneeId
){
}
