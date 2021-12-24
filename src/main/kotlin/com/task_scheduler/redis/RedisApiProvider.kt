package com.task_scheduler.redis

import com.task_scheduler.util.extractRedisConnection
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.redis.client.redisOptionsOf
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions

/**
 * Initialize and provides [RedisAPI] instance
 *
 * @author Guy Komari
 */
object RedisApiProvider {
  private var redisClient: Redis? = null
  private var redisApi: RedisAPI? = null

  fun init(vertx: Vertx, config: JsonObject) {
    synchronized(this) {
      redisClient = Redis.createClient(
        vertx, redisOptionsOf(
          connectionString = extractRedisConnection(config)
        )
      )
      redisApi = RedisAPI.api(redisClient)
    }
  }

  fun redisApi(): RedisAPI = redisApi
    ?: throw RuntimeException("RedisProvider has not been initialized, did you call init(...)?")
}
