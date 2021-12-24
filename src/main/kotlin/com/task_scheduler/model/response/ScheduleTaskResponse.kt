package com.task_scheduler.model.response

import kotlinx.serialization.Serializable

/**
 * @author Guy Komari
 */
@Serializable
data class ScheduleTaskResponse(
  val id: String
)
