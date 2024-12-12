package com.litewallet.example

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleTest {

    @Test
    fun `add invoked with valid mocked values , should return correct value as expected`() {
        val dependencyOne = mockk<DependencyOne>()
        val dependencyTwo = mockk<DependencyTwo>()

        every { dependencyOne.value } returns 10
        every { dependencyTwo.value } returns 3

        val system = SystemUnderTest(dependencyOne, dependencyTwo)

        val expected = 13
        val result = system.add()
        assertEquals(expected, result)
        verify {
            dependencyOne.value
            dependencyTwo.value
        }
    }

    @Test
    fun `add invoked with valid mocked value for dependencyOne, should return correct value as expected`() {
        val dependencyOne = mockk<DependencyOne>()
        val dependencyTwo = mockk<DependencyTwo>(relaxed = true)

        every { dependencyOne.value } returns 7

        val system = SystemUnderTest(dependencyOne, dependencyTwo)

        val expected = 7
        val result = system.add()
        assertEquals(expected, result)
        verify {
            dependencyOne.value
            dependencyTwo.value
        }
    }

    @Test
    fun `add invoked with valid mocked value for dependencyOne and actual value for dependencyTwo , should return correct value as expected`() {
        val dependencyOne = mockk<DependencyOne>()
        val dependencyTwo = spyk(DependencyTwo(3))

        every { dependencyOne.value } returns 7

        val system = SystemUnderTest(dependencyOne, dependencyTwo)

        val expected = 10
        val result = system.add()
        assertEquals(expected, result)
        verify {
            dependencyOne.value
            dependencyTwo.value
        }
    }


    @Test
    fun `calc invoked with valid mocked values, should return correct value as expected and verify by order`() {
        val dependencyOne = mockk<DependencyOne>()
        val dependencyTwo = mockk<DependencyTwo>()

        every { dependencyOne.value } returns 7
        every { dependencyTwo.value } returns 4

        val system = SystemUnderTest(dependencyOne, dependencyTwo)

        val expected = 17
        val result = system.calc()
        assertEquals(expected, result)
        verifyOrder {
            system.multiply()
            system.add()
        }
    }

    @Test
    fun `basic example coroutines`() = runBlocking {
        val dependencyOne = mockk<DependencyOne>()
        val dependencyTwo = mockk<DependencyTwo>()

        every { dependencyOne.value } returns 9
        every { dependencyTwo.value } returns 11

        val system = SystemUnderTest(dependencyOne, dependencyTwo)

        val expected = listOf(9, 11)
        val result = system.fetch()
        assertEquals(expected, result)
        coVerify { system.fetch() }
    }

}

class DependencyOne(val value: Int)
class DependencyTwo(val value: Int)

class SystemUnderTest(
    private val dependencyOne: DependencyOne,
    private val dependencyTwo: DependencyTwo
) {
    fun add(): Int = dependencyOne.value + dependencyTwo.value
    fun multiply(): Int = dependencyOne.value * dependencyTwo.value

    fun calc(): Int = multiply() - add()

    suspend fun fetch(): List<Int> {
        delay(3000)
        return listOf(dependencyOne.value, dependencyTwo.value)
    }
}