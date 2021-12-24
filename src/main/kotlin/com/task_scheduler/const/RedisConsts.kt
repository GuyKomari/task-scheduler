package com.task_scheduler.const

/**
 * @author Guy Komari
 */
object RedisConsts {
  /**
   * List of tasks to process now
   */
  const val EXECUTION_QUEUE = "execution-queue"

  /**
   * Sorted set of the tasks to process in the future
   */
  const val WAITING_QUEUE = "waiting-queue"
}
