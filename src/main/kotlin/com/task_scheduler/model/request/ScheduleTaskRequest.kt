package com.task_scheduler.model.request

import com.task_scheduler.exception.BadRequestException
import com.task_scheduler.model.TaskType
import com.task_scheduler.util.currentTimeSeconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * @author Guy Komari
 */
@Serializable
data class ScheduleTaskRequest(
  val type: TaskType,
  val time: Long,
  val input: JsonObject,
) {
  init {
    if (time < currentTimeSeconds()) {
      throw BadRequestException(
        message = "time - $time should be in the future (current time sec = ${currentTimeSeconds()})"
      )
    }
  }
}
