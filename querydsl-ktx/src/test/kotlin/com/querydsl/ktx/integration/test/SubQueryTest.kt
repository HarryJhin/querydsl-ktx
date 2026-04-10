package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Member
import com.querydsl.ktx.integration.domain.MemberStatus
import com.querydsl.ktx.integration.domain.Order
import com.querydsl.ktx.integration.domain.OrderStatus
import com.querydsl.ktx.integration.repository.MemberRepository
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
@Import(TestConfig::class, MemberRepository::class)
class SubQueryTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    private lateinit var alice: Member
    private lateinit var bob: Member
    private lateinit var charlie: Member

    @BeforeEach
    fun setUp() {
        alice = em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP))
        bob = em.persist(Member(name = "Bob", age = 30, status = MemberStatus.NORMAL))
        charlie = em.persist(Member(name = "Charlie", age = 35, status = MemberStatus.VIP))

        em.persist(Order(member = alice, productName = "Laptop", amount = 150000, orderStatus = OrderStatus.CONFIRMED, orderedAt = LocalDateTime.of(2025, 1, 15, 10, 0)))
        em.persist(Order(member = alice, productName = "Mouse", amount = 30000, orderStatus = OrderStatus.PENDING, orderedAt = LocalDateTime.of(2025, 3, 1, 14, 0)))
        em.persist(Order(member = bob, productName = "Keyboard", amount = 80000, orderStatus = OrderStatus.CONFIRMED, orderedAt = LocalDateTime.of(2025, 2, 10, 9, 0)))
        // Charlie has no orders

        em.flush()
        em.clear()
    }

    @Test
    fun `exists - members with orders`() {
        val result = memberRepository.findMembersWithOrders()

        assertEquals(2, result.size)
        val names = result.map { it.name }.toSet()
        assertTrue(names.contains("Alice"))
        assertTrue(names.contains("Bob"))
    }

    @Test
    fun `notExists - members without orders`() {
        val result = memberRepository.findMembersWithoutOrders()

        assertEquals(1, result.size)
        assertEquals("Charlie", result[0].name)
    }
}
