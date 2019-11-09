package nu.westlin.ticker.server

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nu.westlin.ticker.model.Stock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant

internal class StockRepositoryTest {

    private val stockProperties = StockProperties(prefix = "foo", initSize = 5, random = StockProperties.Random(1, 2))
    private val repository = StockRepository(stockProperties)

    @Test
    fun `add a stock`() {
        runBlocking {
            val stock = Stock("foo", 123, Instant.now())
            repository.add(stock)

            assertThat(repository.currentStocks().toList()).containsExactly(stock)
        }
    }

    @Test
    fun `current stocks`() {
        runBlocking {
            val foo1 = Stock("foo", 123, Instant.now()).also { repository.add(it) }
            val foo2 = foo1.copy(time = foo1.time.plusMillis(2)).also { repository.add(it) }
            val foo3 = foo2.copy(time = foo2.time.plusMillis(2)).also { repository.add(it) }

            val bar1 = Stock("bar", 123, Instant.now()).also { repository.add(it) }
            val bar2 = bar1.copy(time = bar1.time.plusMillis(2)).also { repository.add(it) }
            val bar3 = bar2.copy(time = bar2.time.plusMillis(2)).also { repository.add(it) }

            assertThat(repository.currentStocks().toList()).containsExactlyInAnyOrder(foo3, bar3)
        }
    }

    @Test
    fun `stock history`() {
        runBlocking {
            val foo1 = Stock("foo", 123, Instant.now()).also { repository.add(it) }
            val foo2 = foo1.copy(time = foo1.time.plusSeconds(2)).also { repository.add(it) }
            val foo3 = foo2.copy(time = foo2.time.minusSeconds(2)).also { repository.add(it) }

            assertThat(repository.stockHistory(foo1.name).toList()).containsExactly(foo2, foo1, foo3)
        }
    }

    @Test
    fun `init stock`() {
        repository.init()

        val stocks = repository.stocks().values.flatten()
        assertThat(stocks.size).isEqualTo(stockProperties.initSize)
        assertThat(stocks).filteredOn { it.price < stockProperties.random.lowerPrice || it.price > stockProperties.random.upperPrice }.isEmpty()
        assertThat(stocks).allMatch { it.name.startsWith("${stockProperties.prefix} - ") }
    }
}

@Suppress("UNCHECKED_CAST")
private fun StockRepository.stocks(): Map<String, ArrayList<Stock>> = ReflectionTestUtils.getField(this, "stocks") as Map<String, ArrayList<Stock>>