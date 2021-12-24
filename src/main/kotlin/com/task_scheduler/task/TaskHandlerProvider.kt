package com.task_scheduler.task

import com.task_scheduler.model.TaskType
import com.task_scheduler.task.impl.PrintMessageTask

/**
 * @author Guy Komari
 */
object TaskHandlerProvider {
  private val tasks: Map<TaskType, TaskHandler> = mapOf(
    TaskType.PRINT_MESSAGE to PrintMessageTask,
  )

  fun getHandler(taskType: TaskType): TaskHandler {
    return tasks[taskType] ?: throw RuntimeException("Could not find handler function to type $taskType")
  }
}
