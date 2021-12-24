package com.task_scheduler.web

import com.task_scheduler.exception.TaskSchedulerException
import com.task_scheduler.service.TaskScheduler
import com.task_scheduler.util.extractServerPort
import com.task_scheduler.util.suspendHandler
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.json.JsonObject
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.LoggerFormat
import io.vertx.ext.web.handler.LoggerHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging

/**
 * @author Guy Komari
 */
class HttpServerVerticle : CoroutineVerticle() {
  companion object {
    private val log: KLogger = KotlinLogging.logger {}
  }

  private val taskScheduler: TaskScheduler by lazy {
    TaskScheduler.getInstance()
  }

  override suspend fun start() {
    val healthChecks = HealthChecks.create(vertx)

    val router = Router.router(this.vertx).apply {
      this.createBasicRoutes(healthChecks)
      this.createApisRoutes()
    }

    this.vertx.createHttpServer()
      .requestHandler(router)
      .listen(extractServerPort(config))
      .await()

    healthChecks.register("http-server") { it.complete(Status.OK()) }
  }

  private fun Router.createBasicRoutes(healthChecks: HealthChecks) {
    this.route().apply {
      this.handler(BodyHandler.create())
      this.handler(ResponseContentTypeHandler.create())
      this.handler(LoggerHandler.create(LoggerFormat.TINY))
      this.failureHandler { ctx ->
        val statusCode = when {
          ctx.failure() is TaskSchedulerException -> (ctx.failure() as TaskSchedulerException).statusCode
          ctx.statusCode() != -1 -> ctx.statusCode()
          ctx.failure() is ReplyException -> (ctx.failure() as ReplyException).failureCode()
          else -> 500
        }

        log.error(ctx.failure()) { ctx.failure().message ?: "Api Failed" }

        ctx.response()
          .putHeader("Content-Type", "application/json")
          .setStatusCode(statusCode)
          .end(JsonObject().put("error", ctx.failure().message ?: "").encodePrettily())
      }
    }

    this.get("/health*").handler(HealthCheckHandler.createWithHealthChecks(healthChecks))
  }

  private fun Router.createApisRoutes() {
    this.post("/api/v1/schedule").suspendHandler { ctx ->
      val scheduleTaskResponse = taskScheduler.scheduleTask(
        scheduleTaskReq = Json.decodeFromString(ctx.body().asString())
      )

      ctx.response()
        .putHeader("Content-Type", "application/json")
        .setStatusCode(200)
        .setChunked(true)
        .end(Json.encodeToString(scheduleTaskResponse))
    }
  }
}
