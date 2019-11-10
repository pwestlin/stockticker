package nu.westlin.ticker.client

import kotlinx.coroutines.runBlocking
import nu.westlin.ticker.model.Stock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

// TODO: Kofu?

@SpringBootApplication
class ClientApplication

fun main(args: Array<String>) {
    runApplication<Client>(*args).getBean<Client>().printStocks()
}

@Component
class Client {
    companion object {
        private val logger = LoggerFactory.getLogger(Client::class.java)
    }

    private val webClient = WebClient.create()

    fun printStocks() = runBlocking {
        val stocks = webClient.get().uri("http://localhost:8080/stocks").accept(MediaType.APPLICATION_JSON)
            .awaitExchange().awaitBody<List<Stock>>()
        stocks.forEach { logger.debug(it.toString()) }

    }

}