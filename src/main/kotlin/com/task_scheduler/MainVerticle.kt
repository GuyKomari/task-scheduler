package com.task_scheduler

import com.task_scheduler.redis.RedisApiProvider
import com.task_scheduler.service.TaskProcessor
import com.task_scheduler.service.TaskSampler
import com.task_scheduler.util.extractServerPort
import com.task_scheduler.util.loadConfiguration
import com.task_scheduler.web.HttpServerVerticle
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import mu.KLogging

/**
 * @author Guy Komari
 */
class MainVerticle : CoroutineVerticle() {

  private companion object : KLogging()

  override suspend fun start() {
    val configuration = loadConfiguration(vertx = vertx, config = config)

    RedisApiProvider.init(vertx = vertx, config = configuration)

    vertx.deployVerticle(
      TaskSampler::class.java.name,
      deploymentOptionsOf(
        config = configuration,
        worker = true,
        instances = 1,
        workerPoolSize = 1,
        workerPoolName = "TaskSampler"
      )
    ).await()
    vertx.deployVerticle(
      TaskProcessor::class.java.name, deploymentOptionsOf(
        config = configuration,
        worker = true,
        instances = 1,
        workerPoolSize = 1,
        workerPoolName = "TaskProcessor"
      )
    ).await()

    vertx.deployVerticle(
      HttpServerVerticle::class.java.name,
      deploymentOptionsOf(instances = 1, config = configuration)
    ).await()

    logger.info { "Started http server on port ${extractServerPort(configuration)}" }
  }
}
