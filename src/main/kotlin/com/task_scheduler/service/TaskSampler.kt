package com.task_scheduler.service

import com.task_scheduler.const.RedisConsts.EXECUTION_QUEUE
import com.task_scheduler.const.RedisConsts.WAITING_QUEUE
import com.task_scheduler.model.Task
import com.task_scheduler.redis.RedisApiProvider
import com.task_scheduler.redis.RedisUtils
import com.task_scheduler.util.currentTimeSeconds
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.redis.client.RedisAPI
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging

/**
 * Samples [WAITING_QUEUE] for tasks that should be processed now and dispatch them to [EXECUTION_QUEUE].
 *
 * @author Guy Komari
 */
class TaskSampler : CoroutineVerticle() {
  companion object {
    private const val POLLING_INTERVAL_MS = 1000L
    private val log: KLogger = KotlinLogging.logger {}
  }

  private val redisApi: RedisAPI by lazy { RedisApiProvider.redisApi() }

  override suspend fun start() {
    log.info { "Starting Task Sampler" }
    async(vertx.dispatcher()) { pollWaitingQueue() }
  }

  private suspend fun pollWaitingQueue() {
    while (true) {
      try {
        log.debug { "trying to fetch the first task from $WAITING_QUEUE" }
        val res = redisApi.zrange(listOf(WAITING_QUEUE, "0", "0", "WITHSCORES")).await()

        if (res.any()) {
          val response = res.first()
          val time = response.elementAt(1).toLong()
          val taskAsJson = response.elementAt(0).toString()

          val task = Json.decodeFromString<Task>(taskAsJson)
          val taskId = task.id

          val lockIdentifier = UUID.randomUUID().toString()
          log.debug { "first task id is - $taskId" }

          if (time > currentTimeSeconds()) {
            log.debug { "still not the time for $taskId - delay and try again" }
            delay(POLLING_INTERVAL_MS)
            continue
          } else {
            val lockName = "lock:$taskId"
            if (!RedisUtils.acquireLock(lockName, lockIdentifier)) {
              log.debug { "didnt acquire lock for task $taskId. Delay and try to fetch the next tasks..." }
              delay(5)
              continue
            }

            log.debug { "acquired lock for task $taskId - removing from $WAITING_QUEUE and pushing to $EXECUTION_QUEUE" }
            redisApi.eval(
              listOf(
                """
                | if redis.call('zrem', KEYS[1], ARGV[1]) == 1 then
                |    redis.call('rpush', KEYS[2], ARGV[1])
                | end
                """.trimMargin(),
                "2",
                WAITING_QUEUE,
                EXECUTION_QUEUE,
                taskAsJson,
              )
            ).await()

            RedisUtils.releaseLock(lockName, lockIdentifier)
          }
        } else {
          log.debug { "no tasks found - delay and try again" }
          delay(POLLING_INTERVAL_MS)
          continue
        }
      } catch (e: Exception) {
        log.error(e) { "failed to schedule tasks" }
        delay(POLLING_INTERVAL_MS)
      }
    }
  }
}
