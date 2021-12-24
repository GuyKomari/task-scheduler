package com.task_scheduler.task.impl

import com.task_scheduler.model.Task
import com.task_scheduler.task.TaskHandler
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import mu.KLogger
import mu.KotlinLogging

/**
 * @author Guy Komari
 */
object PrintMessageTask : TaskHandler {
  private val log: KLogger = KotlinLogging.logger {}

  override fun handleTask(task: Task) {
    log.info { task.input["message"].toString() }
  }

  override fun validate(task: Task) {
    val message = task.input["message"]

    require(message is JsonPrimitive && message.jsonPrimitive.isString && message.content.isNotBlank()) {
      "Print-Message-Task requires non blank string 'message' property in input body"
    }
  }
}
