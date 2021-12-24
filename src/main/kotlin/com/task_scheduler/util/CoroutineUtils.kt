package com.task_scheduler.util

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * @author Guy Komari
 */
fun Route.suspendHandler(requestHandler: suspend (RoutingContext) -> Unit) {
  handler { routingContext ->
    CoroutineScope(routingContext.vertx().dispatcher()).async {
      requestHandler(routingContext)
    }.invokeOnCompletion {
      it?.run { routingContext.fail(it) }
    }
  }
}
