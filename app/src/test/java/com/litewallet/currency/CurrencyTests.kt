package com.litewallet.currency
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.breadwallet.tools.sqlite.CurrencyDataSource
import com.breadwallet.tools.util.LocaleHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CurrencyTests {

    @Test
    fun `invoke CurrencyDataSource instance and getAllCurrencies, should return the correct number of currencies`() {
        val context: Context = mockk(relaxed = true)
        val currencyDataSource = CurrencyDataSource.getInstance(context)
        assertEquals(currencyDataSource.allCurrencies.count(), 20)
    }

    @Test
    fun `invoke CurrencyDataSource instance and open database, then should validate database was not null`() {
        val context: Context = mockk(relaxed = true)
        val currencyDataSource = CurrencyDataSource.getInstance(context)
        val database: SQLiteDatabase = currencyDataSource.openDatabase()
        assertNotNull(database)
    }

    @Test
    fun `invoke CurrencyDataSource instance database, should return currency by ISO`() {
        val context: Context = mockk(relaxed = true)
        val currencyDataSource = CurrencyDataSource.getInstance(context)
        val database: SQLiteDatabase = currencyDataSource.openDatabase()

        val usdString = "USD"
        assertEquals("USD", currencyDataSource.getCurrencyByIso(usdString))

        val eurString = "EUR"
        assertEquals("EUR", currencyDataSource.getCurrencyByIso(usdString))

        val gbpString = "GBP"
        assertEquals("GBP", currencyDataSource.getCurrencyByIso(usdString))

        val rubString = "RUB"
        assertEquals("RUB", currencyDataSource.getCurrencyByIso(usdString))

        val uahString = "UAH"
        assertEquals("UAH", currencyDataSource.getCurrencyByIso(usdString))

        val idrString = "IDR"
        assertEquals("IDR", currencyDataSource.getCurrencyByIso(usdString))

        val jpyString = "JPY"
        assertEquals("JPY", currencyDataSource.getCurrencyByIso(usdString))

        val brlString = "BRL"
        assertEquals("BRL", currencyDataSource.getCurrencyByIso(usdString))

        val tryString = "TRY"
        assertEquals("TRY", currencyDataSource.getCurrencyByIso(usdString))
    }

    @Test
    fun `add invoked with valid mocked value for fiatCurrency, should return correct value as expected`() {

        val amountOne = mockk<Amount>()
        val fiatCurrency = mockk<FiatCurrency>()
        val context: Context = mockk(relaxed = true)

        every { amountOne.value } returns 232432.42234
        every { fiatCurrency.code } returns "USD"
        every { fiatCurrency.valuePerLitecoin } returns 0.9899

        val system = CurrencySystemUnderTest(fiatCurrency, amountOne)

        val expected = 7
        val inversionResult = system.invert()
        assertEquals(expected, inversionResult)
        verify {
            amountOne.value
            fiatCurrency.code
            fiatCurrency.valuePerLitecoin
        }
    }
}

class FiatCurrency(val code: String, val valuePerLitecoin: Double)
class Amount(val value: Double)

class CurrencySystemUnderTest(
    private val fiatCurrency: FiatCurrency,
    private val amount: Amount
) {
    fun invert(): Double = 1 / fiatCurrency.valuePerLitecoin

    suspend fun fetch(): List<Double> {
        delay(2000)
        return listOf(fiatCurrency.valuePerLitecoin, invert())
    }
}
