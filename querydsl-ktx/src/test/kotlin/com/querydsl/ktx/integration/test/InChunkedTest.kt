package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Member
import com.querydsl.ktx.integration.domain.MemberStatus
import com.querydsl.ktx.integration.repository.MemberRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestConfig::class, MemberRepository::class)
class InChunkedTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    private val memberIds = mutableListOf<Long>()

    @BeforeEach
    fun setUp() {
        for (i in 1..20) {
            val m = em.persist(Member(name = "Member$i", age = 20 + i, status = MemberStatus.NORMAL))
            memberIds.add(m.id)
        }
        em.flush()
        em.clear()
    }

    @Test
    fun `inChunked - normal case within single chunk`() {
        val targetIds = memberIds.take(5)
        val result = memberRepository.findByIds(targetIds)

        assertEquals(5, result.size)
        assertTrue(result.all { it.id in targetIds })
    }

    @Test
    fun `inChunked - empty collection`() {
        val result = memberRepository.findByIds(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `inChunked - all ids`() {
        val result = memberRepository.findByIds(memberIds)
        assertEquals(20, result.size)
    }

    @Test
    fun `inChunked - large collection triggers chunking`() {
        // Insert enough members to exceed chunk size
        // We test with a smaller chunk by using findByIds which uses default 1000 chunk.
        // For a practical test, we create a list > 1000 synthetic IDs.
        // Only the 20 existing IDs will match, but the chunking logic should still work.
        val largeIds = (1L..1500L).toList()
        val result = memberRepository.findByIds(largeIds)

        // Only the 20 persisted members should match
        assertEquals(20, result.size)
    }
}
