package com.task_scheduler.task

import com.task_scheduler.model.Task

/**
 * @author Guy Komari
 */
interface TaskHandler {
  fun handleTask(task: Task)
  fun validate(task: Task)
}
