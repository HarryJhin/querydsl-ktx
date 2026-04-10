package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Department
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
class DynamicQueryTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    private lateinit var engineering: Department
    private lateinit var marketing: Department

    @BeforeEach
    fun setUp() {
        engineering = em.persist(Department(name = "Engineering"))
        marketing = em.persist(Department(name = "Marketing"))

        em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP, department = engineering))
        em.persist(Member(name = "Bob", age = 30, status = MemberStatus.NORMAL, department = engineering))
        em.persist(Member(name = "Charlie", age = 35, status = MemberStatus.VIP, department = marketing))
        em.persist(Member(name = "Diana", age = 20, status = MemberStatus.INACTIVE, department = marketing))
        em.persist(Member(name = "Eve", age = 28, status = MemberStatus.NORMAL, department = null))
        em.flush()
        em.clear()
    }

    // -- eq --

    @Test
    fun `eq - filter by name`() {
        val result = memberRepository.findByCondition(name = "Alice")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `eq - null name skips filter`() {
        val result = memberRepository.findByCondition(name = null)
        assertEquals(5, result.size)
    }

    @Test
    fun `eq - filter by status`() {
        val result = memberRepository.findByCondition(status = MemberStatus.VIP)
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == MemberStatus.VIP })
    }

    // -- ne --

    @Test
    fun `ne - exclude status`() {
        val result = memberRepository.findByStatusNe(MemberStatus.INACTIVE)
        assertEquals(4, result.size)
        assertTrue(result.none { it.status == MemberStatus.INACTIVE })
    }

    @Test
    fun `ne - null skips filter`() {
        val result = memberRepository.findByStatusNe(null)
        assertEquals(5, result.size)
    }

    // -- in --

    @Test
    fun `in - filter by multiple statuses`() {
        val result = memberRepository.findByStatusIn(listOf(MemberStatus.VIP, MemberStatus.NORMAL))
        assertEquals(4, result.size)
        assertTrue(result.none { it.status == MemberStatus.INACTIVE })
    }

    @Test
    fun `in - null skips filter`() {
        val result = memberRepository.findByStatusIn(null)
        assertEquals(5, result.size)
    }

    // -- notIn --

    @Test
    fun `notIn - exclude multiple statuses`() {
        val result = memberRepository.findByStatusNotIn(listOf(MemberStatus.VIP, MemberStatus.INACTIVE))
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == MemberStatus.NORMAL })
    }

    @Test
    fun `notIn - null skips filter`() {
        val result = memberRepository.findByStatusNotIn(null)
        assertEquals(5, result.size)
    }

    // -- contains --

    @Test
    fun `contains - substring match`() {
        val result = memberRepository.findByCondition(name = null, status = null)
        // Use the specific contains method via findByCondition indirectly
        // Test via the contains extension used in findByCondition's "name contains name" pattern
        // But findByCondition uses "contains" for name - wait, looking at MemberRepository,
        // findByCondition uses: member.name contains name
        // This means eq for name won't work - it's contains! Let me verify.
        // Actually no - member.name contains name is from StringExpressionExtensions.
        // Let me test it directly with a substring.
        val result2 = memberRepository.findByCondition(name = "li")
        assertEquals(2, result2.size) // Alice, Charlie
    }

    // -- containsIgnoreCase --

    @Test
    fun `containsIgnoreCase - case insensitive match`() {
        val result = memberRepository.findByNameContainsIgnoreCase("ALICE")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `containsIgnoreCase - null skips filter`() {
        val result = memberRepository.findByNameContainsIgnoreCase(null)
        assertEquals(5, result.size)
    }

    // -- startsWith --

    @Test
    fun `startsWith - prefix match`() {
        val result = memberRepository.findByNameStartsWith("Al")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `startsWith - null skips filter`() {
        val result = memberRepository.findByNameStartsWith(null)
        assertEquals(5, result.size)
    }

    // -- endsWith --

    @Test
    fun `endsWith - suffix match`() {
        val result = memberRepository.findByNameEndsWith("ce")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `endsWith - null skips filter`() {
        val result = memberRepository.findByNameEndsWith(null)
        assertEquals(5, result.size)
    }

    // -- like --

    @Test
    fun `like - pattern match`() {
        val result = memberRepository.findByNameLike("A%")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `like - null skips filter`() {
        val result = memberRepository.findByNameLike(null)
        assertEquals(5, result.size)
    }

    // -- between (Pair) --

    @Test
    fun `between pair - both bounds`() {
        val result = memberRepository.findByCondition(minAge = 25, maxAge = 30)
        assertEquals(3, result.size) // Alice(25), Bob(30), Eve(28)
    }

    @Test
    fun `between pair - lower bound only`() {
        val result = memberRepository.findByCondition(minAge = 30)
        assertEquals(2, result.size) // Bob(30), Charlie(35)
    }

    @Test
    fun `between pair - upper bound only`() {
        val result = memberRepository.findByCondition(maxAge = 25)
        assertEquals(2, result.size) // Alice(25), Diana(20)
    }

    @Test
    fun `between pair - both null skips filter`() {
        val result = memberRepository.findByCondition(minAge = null, maxAge = null)
        assertEquals(5, result.size)
    }

    // -- between (ClosedRange) --

    @Test
    fun `between closed range`() {
        val result = memberRepository.findByAgeBetweenClosedRange(25..30)
        assertEquals(3, result.size) // Alice(25), Bob(30), Eve(28)
    }

    // -- combined filters --

    @Test
    fun `combined filters - name and status`() {
        val result = memberRepository.findByCondition(name = "li", status = MemberStatus.VIP)
        assertEquals(2, result.size) // Alice, Charlie both contain "li" and are VIP
    }

    @Test
    fun `combined filters - status and department`() {
        val result = memberRepository.findByCondition(status = MemberStatus.VIP, departmentName = "Engineering")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `combined filters - no match`() {
        val result = memberRepository.findByCondition(name = "Alice", status = MemberStatus.INACTIVE)
        assertEquals(0, result.size)
    }

    @Test
    fun `all filters null - returns all`() {
        val result = memberRepository.findByCondition()
        assertEquals(5, result.size)
    }
}
