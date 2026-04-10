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
class CaseDslTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var orderRepository: OrderRepository

    @BeforeEach
    fun setUp() {
        val alice = em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP))

        em.persist(Order(member = alice, productName = "Laptop", amount = 150000, orderStatus = OrderStatus.CONFIRMED, orderedAt = LocalDateTime.of(2025, 1, 15, 10, 0)))
        em.persist(Order(member = alice, productName = "Keyboard", amount = 80000, orderStatus = OrderStatus.CONFIRMED, orderedAt = LocalDateTime.of(2025, 2, 10, 9, 0)))
        em.persist(Order(member = alice, productName = "Mouse", amount = 30000, orderStatus = OrderStatus.PENDING, orderedAt = LocalDateTime.of(2025, 3, 1, 14, 0)))
        em.flush()
        em.clear()
    }

    @Test
    fun `case when - searched case with multiple branches`() {
        val result = orderRepository.findOrdersWithPriorityLabel()

        assertEquals(3, result.size)

        val tupleMap = result.associate { tuple ->
            tuple.get(0, String::class.java)!! to tuple.get(1, String::class.java)!!
        }

        assertEquals("HIGH", tupleMap["Laptop"]) // 150000 >= 100000
        assertEquals("MEDIUM", tupleMap["Keyboard"]) // 80000 >= 50000
        assertEquals("LOW", tupleMap["Mouse"]) // 30000 < 50000
    }
}
