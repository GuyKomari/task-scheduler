package com.task_scheduler.redis

import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.RedisAPI
import mu.KLogger
import mu.KotlinLogging

/**
 * @author Guy Komari
 */
object RedisUtils {
  private val log: KLogger = KotlinLogging.logger {}
  private val redisApi: RedisAPI by lazy { RedisApiProvider.redisApi() }

  suspend fun acquireLock(
    lockName: String,
    identifier: String,
    lockTimeoutSec: Int = 5,
  ): Boolean {
    log.debug { "trying to acquire lock - $lockName" }
    val lock = redisApi.eval(
      listOf(
        """
            | if redis.call('exists', KEYS[1]) == 0 then
            |     return redis.call('setex', KEYS[1], unpack(ARGV))
            | end
          """.trimMargin(),
        "1",
        lockName,
        "$lockTimeoutSec",
        identifier
      )
    ).await()

    if (lock != null && lock.toString() == "OK") {
      log.debug { "acquired lock - $lockName" }
      return true
    } else {
      log.debug { "failed to acquire lock - $lockName" }
    }

    return false
  }


  suspend fun releaseLock(lockName: String, identifier: String) {
    log.debug { "releasing lock - $lockName" }

    redisApi.eval(
      listOf(
        """
        | if redis.call('get', KEYS[1]) == ARGV[1] then
        |     return redis.call('del', KEYS[1]) or true
        | end
      """.trimMargin(),
        "1",
        lockName,
        identifier
      )
    ).await()
  }
}
