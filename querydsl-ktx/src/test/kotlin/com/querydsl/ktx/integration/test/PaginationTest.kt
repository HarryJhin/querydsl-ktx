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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestConfig::class, MemberRepository::class)
class PaginationTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var memberRepository: MemberRepository

    @BeforeEach
    fun setUp() {
        em.persist(Member(name = "Alice", age = 25, status = MemberStatus.VIP))
        em.persist(Member(name = "Bob", age = 30, status = MemberStatus.NORMAL))
        em.persist(Member(name = "Charlie", age = 35, status = MemberStatus.VIP))
        em.persist(Member(name = "Diana", age = 20, status = MemberStatus.INACTIVE))
        em.persist(Member(name = "Eve", age = 28, status = MemberStatus.NORMAL))
        em.flush()
        em.clear()
    }

    // -- page --

    @Test
    fun `page - first page with auto count`() {
        val pageable = PageRequest.of(0, 2, Sort.by("name"))
        val page = memberRepository.findPage(pageable)

        assertEquals(5, page.totalElements)
        assertEquals(3, page.totalPages)
        assertEquals(2, page.content.size)
        assertEquals("Alice", page.content[0].name)
        assertEquals("Bob", page.content[1].name)
        assertTrue(page.hasNext())
    }

    @Test
    fun `page - last page`() {
        val pageable = PageRequest.of(2, 2, Sort.by("name"))
        val page = memberRepository.findPage(pageable)

        assertEquals(5, page.totalElements)
        assertEquals(1, page.content.size)
        assertEquals("Eve", page.content[0].name)
        assertFalse(page.hasNext())
    }

    @Test
    fun `page - sort descending`() {
        val pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "age"))
        val page = memberRepository.findPage(pageable)

        assertEquals("Charlie", page.content[0].name) // age 35
        assertEquals("Bob", page.content[1].name) // age 30
        assertEquals("Eve", page.content[2].name) // age 28
    }

    // -- slice --

    @Test
    fun `slice - first slice has next`() {
        val pageable = PageRequest.of(0, 3, Sort.by("name"))
        val slice = memberRepository.findSlice(pageable)

        assertEquals(3, slice.content.size)
        assertTrue(slice.hasNext())
        assertEquals("Alice", slice.content[0].name)
    }

    @Test
    fun `slice - last slice has no next`() {
        val pageable = PageRequest.of(1, 3, Sort.by("name"))
        val slice = memberRepository.findSlice(pageable)

        assertEquals(2, slice.content.size)
        assertFalse(slice.hasNext())
    }

    // -- SortSpec --

    @Test
    fun `sortSpec - unknown property ignored`() {
        val pageable = PageRequest.of(0, 5, Sort.by("unknownField"))
        val page = memberRepository.findPage(pageable)

        assertEquals(5, page.totalElements)
        assertEquals(5, page.content.size)
    }

    @Test
    fun `sortSpec - multiple sort properties`() {
        // Insert members with same age to test secondary sort
        em.persist(Member(name = "Zara", age = 25, status = MemberStatus.NORMAL))
        em.flush()
        em.clear()

        val pageable = PageRequest.of(0, 10, Sort.by(
            Sort.Order.asc("age"),
            Sort.Order.asc("name"),
        ))
        val page = memberRepository.findPage(pageable)

        // age 20: Diana, age 25: Alice then Zara, age 28: Eve, age 30: Bob, age 35: Charlie
        assertEquals("Diana", page.content[0].name)
        assertEquals("Alice", page.content[1].name)
        assertEquals("Zara", page.content[2].name)
    }

    // -- page with custom count query --

    // -- Sort order verification (non-SortSpec path) --

    @Test
    fun `slice with Sort - results are ordered correctly`() {
        val pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "age"))
        val result = memberRepository.findSliceDirect(pageable)
        // Should be ordered by age DESC: Charlie(35), Bob(30), Eve(28)
        assertEquals("Charlie", result.content[0].name)
        assertEquals("Bob", result.content[1].name)
        assertEquals("Eve", result.content[2].name)
    }

    @Test
    fun `page with Sort - results are ordered correctly`() {
        val pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "age"))
        val result = memberRepository.findPageDirect(pageable)
        // Should be ordered by age ASC: Diana(20), Alice(25), Eve(28)
        assertEquals("Diana", result.content[0].name)
        assertEquals("Alice", result.content[1].name)
        assertEquals("Eve", result.content[2].name)
    }

    @Test
    fun `slice without Sort - still returns results`() {
        val pageable = PageRequest.of(0, 10)
        val result = memberRepository.findSliceDirect(pageable)
        assertEquals(5, result.content.size)
    }

    // -- page with custom count query --

    @Test
    fun `page with custom count query - filters correctly`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val page = memberRepository.findPageWithCountQuery(pageable, MemberStatus.VIP)

        assertEquals(2, page.totalElements)
        assertEquals(2, page.content.size)
        assertTrue(page.content.all { it.status == MemberStatus.VIP })
    }
}
