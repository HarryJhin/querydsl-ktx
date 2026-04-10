package com.querydsl.ktx.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestConfig::class, TestMemberRepository::class)
class QuerydslKtxIntegrationTest {

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var repository: TestMemberRepository

    private fun insertMembers() {
        entityManager.persist(TestMember(name = "Alice", age = 25, status = TestStatus.VIP))
        entityManager.persist(TestMember(name = "Bob", age = 30, status = TestStatus.NORMAL))
        entityManager.persist(TestMember(name = "Charlie", age = 35, status = TestStatus.VIP))
        entityManager.persist(TestMember(name = "Diana", age = 20, status = TestStatus.INACTIVE))
        entityManager.flush()
        entityManager.clear()
    }

    // ── Null-safe dynamic queries ──

    @Test
    fun `all filters null returns all members`() {
        insertMembers()
        val result = repository.findByCondition()
        assertEquals(4, result.size)
    }

    @Test
    fun `filter by name only`() {
        insertMembers()
        val result = repository.findByCondition(name = "Alice")
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].name)
    }

    @Test
    fun `filter by status only`() {
        insertMembers()
        val result = repository.findByCondition(status = TestStatus.VIP)
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == TestStatus.VIP })
    }

    @Test
    fun `filter by age range - both bounds`() {
        insertMembers()
        val result = repository.findByCondition(minAge = 25, maxAge = 30)
        assertEquals(2, result.size) // Alice(25), Bob(30)
    }

    @Test
    fun `filter by age range - lower bound only`() {
        insertMembers()
        val result = repository.findByCondition(minAge = 30)
        assertEquals(2, result.size) // Bob(30), Charlie(35)
    }

    @Test
    fun `filter by age range - upper bound only`() {
        insertMembers()
        val result = repository.findByCondition(maxAge = 25)
        assertEquals(2, result.size) // Alice(25), Diana(20)
    }

    @Test
    fun `combined filters - name and status`() {
        insertMembers()
        val result = repository.findByCondition(name = "Alice", status = TestStatus.VIP)
        assertEquals(1, result.size)
    }

    @Test
    fun `combined filters - no match`() {
        insertMembers()
        val result = repository.findByCondition(name = "Alice", status = TestStatus.INACTIVE)
        assertEquals(0, result.size)
    }

    // ── String operations ──

    @Test
    fun `contains with value`() {
        insertMembers()
        val result = repository.findByNameContains("li")
        assertEquals(2, result.size) // Alice, Charlie
    }

    @Test
    fun `contains with null skips filter`() {
        insertMembers()
        val result = repository.findByNameContains(null)
        assertEquals(4, result.size)
    }
}
