package com.task_scheduler.util

import com.task_scheduler.model.Task
import com.task_scheduler.model.request.ScheduleTaskRequest

/**
 * @author Guy Komari
 */
fun ScheduleTaskRequest.toTask(): Task {
  return Task(
    type = this.type,
    time = this.time,
    input = this.input
  )
}
