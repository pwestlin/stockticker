package nu.westlin.ticker.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nu.westlin.ticker.model.Stock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.boot.logging.LogLevel
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import kotlin.system.measureTimeMillis

// TODO: Ktor client instead of Spring Kofu?

fun main(args: Array<String>) {
    app.run(args).getBean<Client>().printStocks()
}

val loggingConfig = configuration {
    logging {
        level = LogLevel.WARN
        level("nu.westlin.ticker", LogLevel.DEBUG)
    }
}

val app = application(WebApplicationType.NONE) {
    configurationProperties<ClientProperties>()
    enable(loggingConfig)

    beans {
        bean<Client>()
    }
}

data class ClientProperties(val servers: Map<String, String>)

class Client(private val properties: ClientProperties) {
    companion object {
        private val logger = LoggerFactory.getLogger(Client::class.java)
    }

    private val webClient = WebClient.create()

    fun printStocks() = runBlocking {
        // TODO petves: Only show stocks that have changed it's price and show the diff with +/-
        // TODO: On startup, save url and result of GET url/stockType in an object for later display. Then change map to list for servers in application.yml
        val execTime = measureTimeMillis {
            properties.servers.map { (type, url) ->
                async(Dispatchers.IO) {
                    logger.debug("GET url: $url")
                    // TODO: Use class, not Pair
                    Pair(type, webClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
                        .awaitExchange().awaitBody<List<Stock>>())
                }
            }.forEach { deferred ->
                val pair = deferred.await()
                logger.debug("Stocks of type ${pair.first}:")
                pair.second.forEach { logger.debug(it.toString()) }
            }
        }
/*
        val execTime = measureTimeMillis {
            properties.servers.forEach { (type, url) ->
                val stocks = webClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
                    .awaitExchange().awaitBody<List<Stock>>()
                logger.info("Stocks of type $type:")
                stocks.forEach { logger.info(it.toString()) }
            }
        }
*/
        logger.debug("Exec time: $execTime ms")
    }
}