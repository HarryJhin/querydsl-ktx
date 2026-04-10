package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Member
import com.querydsl.ktx.integration.domain.MemberStatus
import com.querydsl.ktx.integration.domain.QMember
import com.querydsl.ktx.numberTemplate
import com.querydsl.ktx.stringTemplate
import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestConfig::class)
class ExpressionsTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var queryFactory: JPAQueryFactory

    private val member = QMember.member

    @BeforeEach
    fun setUp() {
        em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP))
        em.persist(Member(name = "Bob", age = 30, status = MemberStatus.NORMAL))
        em.persist(Member(name = "Charlie", age = 35, status = MemberStatus.VIP))
        em.flush()
        em.clear()
    }

    @Test
    fun `numberTemplate - used in select`() {
        val ageDoubled = numberTemplate<Int>("({0} * 2)", member.age)

        val result = queryFactory
            .select(member.name, ageDoubled)
            .from(member)
            .where(member.name.eq("Alice"))
            .fetchOne()!!

        assertEquals("Alice", result.get(member.name))
        assertEquals(50, result.get(ageDoubled))
    }

    @Test
    fun `stringTemplate - used in where clause`() {
        val upperName = stringTemplate("upper({0})", member.name)

        val result = queryFactory
            .select(member)
            .from(member)
            .where(upperName.eq("ALICE"))
            .fetch()

        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `numberTemplate - used in where clause for aggregation`() {
        val ageExpr = numberTemplate<Int>("({0} + {1})", member.age, 10)

        val result = queryFactory
            .select(member.name)
            .from(member)
            .where(ageExpr.gt(40))
            .fetch()

        // Alice 25+10=35 (no), Bob 30+10=40 (no, gt not goe), Charlie 35+10=45 (yes)
        assertEquals(1, result.size)
        assertEquals("Charlie", result[0])
    }
}
