package com.task_scheduler.util

import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.coroutines.await

/**
 * @author Guy Komari
 */
suspend fun loadConfiguration(vertx: Vertx, config: JsonObject = JsonObject()): JsonObject {
  return if (config.isEmpty) {
    ConfigRetriever
      .create(vertx, configRetrieverOptionsOf(includeDefaultStores = true)).config.await()
  } else config
}

private const val REDIS_CONNECTION = "REDIS_CONNECTION"
private const val SERVER_PORT = "SERVER_PORT"

fun extractRedisConnection(config: JsonObject): String = config.getString(
  REDIS_CONNECTION,
  "redis://localhost:6379"
)

fun extractServerPort(config: JsonObject): Int = config.getInteger(
  SERVER_PORT,
  8888
)
