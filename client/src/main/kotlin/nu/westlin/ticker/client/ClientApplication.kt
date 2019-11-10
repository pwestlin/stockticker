package nu.westlin.ticker.client

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

    // TODO petves: Coroutines and async (map to new list)
    fun printStocks() = runBlocking {
        // TODO petves: Only show stocks that have changed it's price and show the diff with +/-
        properties.servers.forEach { (type, url) ->
            val stocks = webClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
                .awaitExchange().awaitBody<List<Stock>>()
            logger.debug("Stocks of type $type:")
            stocks.forEach { logger.debug(it.toString()) }
        }
    }
}