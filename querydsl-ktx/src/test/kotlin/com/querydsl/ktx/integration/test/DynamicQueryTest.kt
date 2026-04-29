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
        // findByCondition uses "member.name contains name" (substring match)
        val result = memberRepository.findByCondition(name = "li")
        assertEquals(2, result.size) // Alice, Charlie
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

    // -- notBetween (partial range) --

    @Test
    fun `notBetween pair - both bounds excludes range`() {
        val result = memberRepository.findByAgeNotBetween(minAge = 25, maxAge = 30)
        assertEquals(2, result.size) // Charlie(35), Diana(20)
        assertTrue(result.none { it.age in 25..30 })
    }

    @Test
    fun `notBetween pair - lower bound only uses less-than`() {
        val result = memberRepository.findByAgeNotBetween(minAge = 25)
        assertEquals(1, result.size) // Diana(20)
        assertTrue(result.all { it.age < 25 })
    }

    @Test
    fun `notBetween pair - upper bound only uses greater-than`() {
        val result = memberRepository.findByAgeNotBetween(maxAge = 30)
        assertEquals(1, result.size) // Charlie(35)
        assertTrue(result.all { it.age > 30 })
    }

    @Test
    fun `notBetween pair - both null skips filter`() {
        val result = memberRepository.findByAgeNotBetween(minAge = null, maxAge = null)
        assertEquals(5, result.size)
    }

    // -- andAnyOf vararg --

    @Test
    fun `andAnyOf vararg - VIP in any of given departments`() {
        val result = memberRepository.findVipInAnyOf("Engineering", "Marketing")
        // Alice (VIP, Engineering), Charlie (VIP, Marketing)
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == MemberStatus.VIP })
    }

    @Test
    fun `andAnyOf vararg - empty vararg keeps base predicate only`() {
        val result = memberRepository.findVipInAnyOf()
        // VIP regardless of department: Alice, Charlie
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == MemberStatus.VIP })
    }

    // -- orAllOf vararg --

    @Test
    fun `orAllOf vararg - VIP OR (minAge AND department)`() {
        val result = memberRepository.findActiveOrAllOf(minAge = 30, departmentName = "Marketing")
        // VIP (Alice, Charlie) OR (age >= 30 AND dept = Marketing -> Charlie)
        // Union: Alice, Charlie
        assertEquals(2, result.size)
    }

    @Test
    fun `orAllOf vararg - all predicates null returns VIP only`() {
        val result = memberRepository.findActiveOrAllOf(minAge = null, departmentName = null)
        // null args -> all predicates null -> orAllOf reduces to base (VIP only)
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == MemberStatus.VIP })
    }

    // -- like with escape --

    @Test
    fun `like with escape - matches literal % character`() {
        em.persist(Member(name = "10% Discount", age = 0, status = MemberStatus.NORMAL))
        em.flush()
        em.clear()

        val result = memberRepository.findByNameLikeWithEscape("10\\%%", '\\')
        assertEquals(1, result.size)
        assertEquals("10% Discount", result[0].name)
    }

    @Test
    fun `like with escape - null pattern skips filter`() {
        val result = memberRepository.findByNameLikeWithEscape(null, '\\')
        assertEquals(5, result.size)
    }
}
