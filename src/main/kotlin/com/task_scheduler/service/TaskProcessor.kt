package com.task_scheduler.service

import com.task_scheduler.const.RedisConsts.EXECUTION_QUEUE
import com.task_scheduler.model.Task
import com.task_scheduler.redis.RedisApiProvider
import com.task_scheduler.task.TaskHandlerProvider
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.redis.client.RedisAPI
import kotlinx.coroutines.async
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging

/**
 * (long) polling tasks from [EXECUTION_QUEUE] and processing each of them.
 *
 * @author Guy Komari
 */
class TaskProcessor : CoroutineVerticle() {
  companion object {
    private val log: KLogger = KotlinLogging.logger {}
    private const val WAITING_TIME_SEC = 30
  }

  private val redisApi: RedisAPI by lazy { RedisApiProvider.redisApi() }

  override suspend fun start() {
    log.info { "Starting Task Processor" }
    async(vertx.dispatcher()) { pollExecutionQueue() }
  }

  private suspend fun pollExecutionQueue() {
    while (true) {
      try {
        val res = redisApi.blpop(listOf(EXECUTION_QUEUE, "$WAITING_TIME_SEC")).await()

        if (res == null) {
          log.debug { "no tasks to process after waiting $WAITING_TIME_SEC. Polling again..." }
        } else {
          val task = Json.decodeFromString<Task>(res.elementAt(1).toString())
          TaskHandlerProvider.getHandler(task.type).handleTask(task)
        }
      } catch (e: Exception) {
        log.error(e) { "failed to process task" }
      }
    }
  }
}
