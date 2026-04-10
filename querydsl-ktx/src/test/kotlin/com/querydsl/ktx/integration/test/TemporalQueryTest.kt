package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Member
import com.querydsl.ktx.integration.domain.MemberStatus
import com.querydsl.ktx.integration.domain.Order
import com.querydsl.ktx.integration.domain.OrderStatus
import com.querydsl.ktx.integration.repository.OrderRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestConfig::class, OrderRepository::class)
class TemporalQueryTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var orderRepository: OrderRepository

    private val jan15 = LocalDateTime.of(2025, 1, 15, 10, 0)
    private val feb10 = LocalDateTime.of(2025, 2, 10, 9, 0)
    private val mar01 = LocalDateTime.of(2025, 3, 1, 14, 0)

    @BeforeEach
    fun setUp() {
        val alice = em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP))

        em.persist(Order(member = alice, productName = "Laptop", amount = 150000, orderStatus = OrderStatus.CONFIRMED, orderedAt = jan15))
        em.persist(Order(member = alice, productName = "Keyboard", amount = 80000, orderStatus = OrderStatus.CONFIRMED, orderedAt = feb10))
        em.persist(Order(member = alice, productName = "Mouse", amount = 30000, orderStatus = OrderStatus.PENDING, orderedAt = mar01))
        em.flush()
        em.clear()
    }

    // -- after --

    @Test
    fun `after - orders after date`() {
        val result = orderRepository.findOrdersAfter(LocalDateTime.of(2025, 2, 1, 0, 0))
        assertEquals(2, result.size) // feb10, mar01
    }

    @Test
    fun `after - null skips filter`() {
        val result = orderRepository.findOrdersAfter(null)
        assertEquals(3, result.size)
    }

    // -- before --

    @Test
    fun `before - orders before date`() {
        val result = orderRepository.findOrdersBefore(LocalDateTime.of(2025, 2, 1, 0, 0))
        assertEquals(1, result.size) // jan15
        assertEquals("Laptop", result[0].productName)
    }

    @Test
    fun `before - null skips filter`() {
        val result = orderRepository.findOrdersBefore(null)
        assertEquals(3, result.size)
    }

    // -- between (temporal) --

    @Test
    fun `between - both bounds`() {
        val from = LocalDateTime.of(2025, 1, 1, 0, 0)
        val to = LocalDateTime.of(2025, 2, 28, 23, 59)
        val result = orderRepository.findOrdersBetween(from, to)
        assertEquals(2, result.size) // jan15, feb10
    }

    @Test
    fun `between - lower bound only`() {
        val result = orderRepository.findOrdersBetween(from = LocalDateTime.of(2025, 2, 1, 0, 0))
        assertEquals(2, result.size) // feb10, mar01
    }

    @Test
    fun `between - upper bound only`() {
        val result = orderRepository.findOrdersBetween(to = LocalDateTime.of(2025, 2, 28, 23, 59))
        assertEquals(2, result.size) // jan15, feb10
    }

    @Test
    fun `between - both null skips filter`() {
        val result = orderRepository.findOrdersBetween(null, null)
        assertEquals(3, result.size)
    }
}
