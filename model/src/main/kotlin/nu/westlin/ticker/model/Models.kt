package nu.westlin.ticker.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

data class Stock(val name: String, val price: Int, @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "CET") val time: Instant) {

    companion object
}