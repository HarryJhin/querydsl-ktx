package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Member
import com.querydsl.ktx.integration.domain.MemberStatus
import com.querydsl.ktx.integration.domain.Order
import com.querydsl.ktx.integration.domain.OrderStatus
import com.querydsl.ktx.integration.repository.MemberRepository
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
@Import(TestConfig::class, MemberRepository::class, OrderRepository::class)
class BulkDmlTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @BeforeEach
    fun setUp() {
        val alice = em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP))
        val bob = em.persist(Member(name = "Bob", age = 30, status = MemberStatus.NORMAL))
        em.persist(Member(name = "Charlie", age = 35, status = MemberStatus.NORMAL))

        em.persist(Order(member = alice, productName = "Laptop", amount = 150000, orderStatus = OrderStatus.CONFIRMED, orderedAt = LocalDateTime.of(2025, 1, 15, 10, 0)))
        em.persist(Order(member = bob, productName = "Mouse", amount = 30000, orderStatus = OrderStatus.CANCELLED, orderedAt = LocalDateTime.of(2025, 2, 10, 9, 0)))
        em.persist(Order(member = alice, productName = "Keyboard", amount = 80000, orderStatus = OrderStatus.CANCELLED, orderedAt = LocalDateTime.of(2025, 3, 1, 14, 0)))
        em.flush()
        em.clear()
    }

    // -- bulk update --

    @Test
    fun `bulk update - deactivate by status`() {
        val count = memberRepository.deactivateByStatus(MemberStatus.NORMAL)
        assertEquals(2L, count)

        // After modifying, entity manager is cleared, so fresh query
        val result = memberRepository.findByCondition(status = MemberStatus.INACTIVE)
        assertEquals(2, result.size)
        assertTrue(result.all { it.name in listOf("Bob", "Charlie") })
    }

    @Test
    fun `bulk update - no match returns zero`() {
        val count = memberRepository.deactivateByStatus(MemberStatus.INACTIVE)
        assertEquals(0L, count)
    }

    // -- bulk delete --

    @Test
    fun `bulk delete - delete cancelled orders`() {
        val count = orderRepository.deleteByStatus(OrderStatus.CANCELLED)
        assertEquals(2L, count)

        // Verify remaining orders
        val remaining = orderRepository.findOrdersAfter(null)
        assertEquals(1, remaining.size)
        assertEquals("Laptop", remaining[0].productName)
    }

    @Test
    fun `bulk delete - no match returns zero`() {
        val count = orderRepository.deleteByStatus(OrderStatus.PENDING)
        assertEquals(0L, count)
    }
}
