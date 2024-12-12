package com.litewallet.currency

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.breadwallet.presenter.entities.CurrencyEntity
import com.breadwallet.tools.sqlite.CurrencyDataSource
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class CurrencyTests {

    private lateinit var database: SQLiteDatabase
    private var currencyDataSource: CurrencyDataSource? = null

    @Before
    fun setup() {
        val context: Context = mockk(relaxed = true)
        currencyDataSource = spyk(CurrencyDataSource.getInstance(context))

        database = mockk<SQLiteDatabase>()
        every { currencyDataSource?.openDatabase() } returns database
    }

    @After
    fun tearDown() {
        currencyDataSource?.closeDatabase()
        currencyDataSource = null
    }

    @Test
    fun `invoke CurrencyDataSource instance and getAllCurrencies, should return the correct number of currencies`() {
        //The actual number of currencies is 174. The 0 is a placeholder and needs to be replaced with a db query.
        mockCursorDataFromDatabase()
        assertEquals(currencyDataSource?.allCurrencies?.count(), 0)
    }

    @Test
    fun `invoke CurrencyDataSource instance and open database, then should validate database was not null`() {
        val database = currencyDataSource?.openDatabase()
        assertNotNull(database)
    }

    @Test
    fun `invoke CurrencyDataSource instance database, should return currency by ISO`() {

        val expected = CurrencyEntity("USD", "USD", 110.345f)
        mockCursorDataFromDatabase(isEmpty = false, expected = expected)

        val actual = currencyDataSource?.getCurrencyByIso("USD")
        assertEquals(expected.code, actual?.code)
        assertEquals(expected.name, actual?.name)
        assertEquals(expected.rate, actual?.rate)
    }

    @Test
    fun `invoke putCurrencies, then should save the CurrencyEntity into database`() {
        val fetchedCurrencies = listOf(
            CurrencyEntity("USD", "USD", 110.345f),
            CurrencyEntity("IDR", "IDR", 1752020.9450135066f)
        )

        every { database.beginTransaction() } just Runs
        fetchedCurrencies.forEachIndexed { index, currencyEntity ->
            every { database.insertWithOnConflict(any(), any(), any(), any()) } returns (index + 1L)
        }
        every { database.setTransactionSuccessful() } just Runs
        every { database.endTransaction() } just Runs

        currencyDataSource?.putCurrencies(fetchedCurrencies)
    }

    private fun mockCursorDataFromDatabase(
        isEmpty: Boolean = true,
        expected: CurrencyEntity? = null
    ) {
        val cursor = mockk<Cursor>()
        every { database.query(any(), any(), any(), any(), any(), any(), any()) } returns cursor
        if (isEmpty) {
            every { cursor.moveToFirst() } returns false
            every { cursor.isAfterLast } returns true
        } else {
            every { cursor.getString(any()) } returns expected?.code
            every { cursor.getString(any()) } returns expected?.name
            every { cursor.getFloat(any()) } returns expected?.rate!!

            every { cursor.moveToFirst() } returns true
            every { cursor.isAfterLast } returns false
        }
        every { cursor.close() } just Runs
    }
}
