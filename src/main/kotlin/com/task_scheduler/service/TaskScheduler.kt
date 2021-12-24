package com.task_scheduler.service

import com.task_scheduler.const.RedisConsts.EXECUTION_QUEUE
import com.task_scheduler.const.RedisConsts.WAITING_QUEUE
import com.task_scheduler.model.Task
import com.task_scheduler.model.request.ScheduleTaskRequest
import com.task_scheduler.model.response.ScheduleTaskResponse
import com.task_scheduler.redis.RedisApiProvider
import com.task_scheduler.task.TaskHandlerProvider
import com.task_scheduler.util.currentTimeSeconds
import com.task_scheduler.util.toTask
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.RedisAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging

/**
 * @author Guy Komari
 */
class TaskScheduler private constructor() {

  private val redisApi: RedisAPI by lazy { RedisApiProvider.redisApi() }

  companion object {
    private val log: KLogger = KotlinLogging.logger {}
    private var INSTANCE: TaskScheduler? = null

    fun getInstance(): TaskScheduler =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: TaskScheduler().also { INSTANCE = it }
      }
  }

  suspend fun scheduleTask(scheduleTaskReq: ScheduleTaskRequest): ScheduleTaskResponse {
    val task = scheduleTaskReq.toTask().also {
      TaskHandlerProvider.getHandler(it.type).validate(it)
      schedule(it)
    }

    return ScheduleTaskResponse(id = task.id)
  }

  /**
   * Schedule the task for immediate processing or future processing
   */
  private suspend fun schedule(task: Task) {
    val taskAsJsonStr = Json.encodeToString(task)

    val id = task.id
    val time = task.time
    if (time - currentTimeSeconds() > 0) {
      log.debug { "Task $id should be processed in the future" }
      redisApi.zadd(listOf(WAITING_QUEUE, time.toString(), taskAsJsonStr)).await()
    } else {
      log.debug { "Task $id should be processed immediately" }
      redisApi.rpush(listOf(EXECUTION_QUEUE, taskAsJsonStr)).await()
    }
  }
}
