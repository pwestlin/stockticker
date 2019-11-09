package nu.westlin.ticker.model

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


internal class StockJsonTest {

    private val mapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }

    private val stock = Stock("foo", 123, Instant.now())

    @Test
    fun `to and from json`() {

        assertThat(mapper.readValue<Stock>(mapper.writeValueAsString(stock))).isEqualTo(stock)
    }

    @Test
    fun `time format`() {
        assertThat(mapper.writeValueAsString(stock)).isEqualTo("{\"name\":\"foo\",\"price\":123,\"time\":\"${formatInstant(stock.time)}\"}")
    }

    private fun formatInstant(now: Instant): String {
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
            .format(now)
    }

}