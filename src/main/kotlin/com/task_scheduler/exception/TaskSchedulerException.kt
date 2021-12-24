package com.task_scheduler.exception

/**
 * @author Guy Komari
 */
open class TaskSchedulerException(
  open val statusCode: Int,
  override val message: String
) : RuntimeException(message)

data class BadRequestException(
  override val statusCode: Int = 400,
  override val message: String
) : TaskSchedulerException(statusCode, message)
