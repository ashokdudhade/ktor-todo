package com.relevant.programmer

import com.codahale.metrics.Slf4jReporter
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.relevant.programmer.service.TodoService
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.routing.routing
import java.util.*
import io.ktor.metrics.dropwizard.*
import com.google.inject.name.*
import java.util.concurrent.TimeUnit
import com.google.inject.*
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.relevant.programmer.dao.DAOFacade
import com.relevant.programmer.dao.implementation.DAOFacadeDatabase
import com.relevant.programmer.routes.TodoRoutes
import io.ktor.request.uri
import io.ktor.server.netty.EngineMain
import io.ktor.util.AttributeKey
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import java.io.File


fun main(args: Array<String>): Unit = EngineMain.main(args)

val InjectorKey = AttributeKey<Injector>("injector")

val ApplicationCall.injector: Injector get() = attributes[InjectorKey]


val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDev get() = envKind == "dev"
val Application.isProd get() = envKind != "dev"

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    // Create main injector
    val injector = Guice.createInjector(MainModule(this), TodoModule())

    // Intercept application call and put child injector into attributes
    intercept(ApplicationCallPipeline.Features) {
        call.attributes.put(InjectorKey, injector.createChildInjector(CallModule(call)))
    }


    val correlationId = "x-correlation-id"
    install(CallLogging) {
        mdc(correlationId) {
            // call: ApplicationCall ->
            it.request.headers[correlationId]
        }
        mdc("requestId") {
            // call: ApplicationCall ->
            UUID.randomUUID().toString()
        }
    }

    if (!isDev) {
        install(DropwizardMetrics) {
            val reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
            reporter.start(10, TimeUnit.SECONDS);
        }
    }




    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
    }

    install(StatusPages)


}

// Some service providing data inside a call
class CallService @Inject constructor(private val call: ApplicationCall) {
    fun information() = call.request.uri
}

// A module for each call
class CallModule(private val call: ApplicationCall) : AbstractModule() {
    override fun configure() {
        bind(ApplicationCall::class.java).toInstance(call)
        bind(CallService::class.java)
    }
}


class MainModule(private val application: Application) : AbstractModule() {
    override fun configure() {
        bind(Application::class.java).toInstance(application)
    }
}

class TodoModule() : AbstractModule() {
    override fun configure() {
        bind(TodoRoutes::class.java).asEagerSingleton()

        val dir = File("build/db")


        val pool = ComboPooledDataSource().apply {
            driverClass = Driver::class.java.name
            jdbcUrl = "jdbc:h2:file:${dir.canonicalFile.absolutePath}"
            user = ""
            password = ""
        }


        val dao: DAOFacade =
            DAOFacadeDatabase(Database.connect(pool))
        dao.init()
        bind(TodoService::class.java).asEagerSingleton()
        bind(DAOFacade::class.java).toInstance(dao)
    }
}
