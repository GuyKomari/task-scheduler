package com.task_scheduler.model

import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * @author Guy Komari
 */
@Serializable
data class Task(
  val id: String = UUID.randomUUID().toString(),
  val type: TaskType,
  val time: Long,
  val input: JsonObject
)
