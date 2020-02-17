package com.relevant.programmer

import com.codahale.metrics.Slf4jReporter
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import java.util.*
import io.ktor.metrics.dropwizard.*
import java.util.concurrent.TimeUnit




fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDev get() = envKind == "dev"
val Application.isProd get() = envKind != "dev"

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val envConfig = environment.config.config("ktor.dev")

    val correlationId = "x-correlation-id"
    install(CallLogging){
        mdc(correlationId) { // call: ApplicationCall ->
            it.request.headers[correlationId]
        }
        mdc("requestId") { // call: ApplicationCall ->
           UUID.randomUUID().toString()
        }
    }

    install(DropwizardMetrics) {
        val reporter = Slf4jReporter.forRegistry(registry)
            .outputTo(log)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
        reporter.start(10, TimeUnit.SECONDS);
    }

    intercept(ApplicationCallPipeline.Features) {
        val requestId = UUID.randomUUID()
        logger.attach("req.Id", requestId.toString()) {
            logger.info("Interceptor[start]")
            proceed()
            logger.info("Interceptor[end]")
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
    }

    install(StatusPages)

    routing {
        get("/"){
            call.respond("Hello")

        }

        todo()

    }

}

