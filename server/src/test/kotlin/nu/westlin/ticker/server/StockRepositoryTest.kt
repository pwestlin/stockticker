package nu.westlin.ticker.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nu.westlin.ticker.model.Stock
import org.junit.jupiter.api.Test
import java.time.Instant

internal class StockRepositoryTest {

    @Test
    fun `add a Stock`() {
        val stock = Stock("foo", 123, Instant.now())
        println(jacksonObjectMapper().writeValueAsString(stock))

    }
}