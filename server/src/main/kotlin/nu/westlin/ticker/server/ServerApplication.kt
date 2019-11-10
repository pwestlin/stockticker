package nu.westlin.ticker.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import nu.westlin.ticker.model.Stock
import org.slf4j.LoggerFactory
import org.springframework.boot.WebApplicationType
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.logging.LogLevel
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.webflux.webFlux
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.time.Instant
import java.util.Collections
import kotlin.concurrent.thread
import kotlin.random.Random

val loggingConfig = configuration {
    logging {
        level = LogLevel.WARN
        level("nu.westlin.ticker", LogLevel.DEBUG)
    }
}

data class StockProperties(val prefix: String = "foo", val initSize: Int = 30, val random: Random) {
    data class Random(val lowerTime: Long = 1000, val upperTime: Long = 3000, val lowerPrice: Int = 10, val upperPrice: Int = 999)
}

class StockRepository(private val stockProperties: StockProperties) {
    private val stocks = Collections.synchronizedMap(mutableMapOf<String, ArrayList<Stock>>())

    companion object {
        private val logger = LoggerFactory.getLogger(StockRepository::class.java)
    }


    internal fun add(stock: Stock) {
        if (stocks.containsKey(stock.name)) {
            stocks[stock.name]!!.add(stock)
        } else {
            stocks[stock.name] = listOf(stock).toMutableList() as ArrayList<Stock>
        }
    }

    fun currentStocks(): Flow<Stock> {
        return currentStockList().sortedByDescending { it.time }.asFlow()
    }

    fun stockHistory(name: String): Flow<Stock> {
        return stocks.filter { it.key == name }.flatMap { it.value }.sortedByDescending { it.time }.asFlow()
    }

    private fun currentStockList(): List<Stock> {
        return stocks.map { entry -> entry.value.maxBy { it.time } }.filterNotNull()
    }

    // TODO: Move this to a separate class?
    fun init() {
        val stockNames = listOf("Microsoft", "Red Hat", "Saab", "Volvo", "Stora Enso", "Sogeti")
        repeat(stockProperties.initSize) {
            add(Stock("${stockProperties.prefix} - ${stockNames.random()}", Random.nextInt(stockProperties.random.lowerPrice, stockProperties.random.upperPrice + 1), Instant.now()))
        }
    }

    // TODO: Move this to a separate class?
    fun randomCreate() {
        fun newPrice(stock: Stock): Int {
            val value = Random.nextInt(0, 10).let {
                if (Random.nextBoolean()) (it * -1) else it
            }
            logger.debug("value = $value")
            return stock.price + value
        }
        thread {
            while (true) {
                currentStockList().forEach { stock ->
                    if (Random.nextBoolean()) {
                        add(stock.copy(price = newPrice(stock), time = Instant.now()))
                    }
                }
                Thread.sleep(Random.nextLong(stockProperties.random.lowerTime, stockProperties.random.upperTime + 1))
            }
        }
    }
}

val dataConfig = configuration {

    configurationProperties<StockProperties>(prefix = "stocks")

    beans {
        bean<StockRepository>()
    }

    listener<ApplicationReadyEvent> {
        ref<StockRepository>().init()

        ref<StockRepository>().randomCreate()
    }
}


@Suppress("UNUSED_PARAMETER")
class RestHandler(private val stockRepository: StockRepository) {

    suspend fun stocks(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().bodyAndAwait(stockRepository.currentStocks())
    }

    suspend fun history(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().bodyAndAwait(stockRepository.stockHistory(request.pathVariable("name")))
    }
}

fun routes(restHandler: RestHandler) = coRouter {
    accept(MediaType.APPLICATION_JSON).nest {
        GET("/stocks", restHandler::stocks)
        GET("/stocks/{name}", restHandler::history)
    }
}

val webConfig = configuration {
    beans {
        bean<RestHandler>()
        bean(::routes)
    }

    webFlux {
        codecs {
            string()
            jackson()
        }
    }
}

val app = application(WebApplicationType.REACTIVE) {
    enable(loggingConfig)
    enable(dataConfig)
    enable(webConfig)
}

fun main() {
    app.run()
}
