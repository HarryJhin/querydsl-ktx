package com.querydsl.ktx.integration.test

import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Product
import com.querydsl.ktx.integration.repository.ProductRepository
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
@Import(TestConfig::class, ProductRepository::class)
class ReverseBetweenTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var productRepository: ProductRepository

    @BeforeEach
    fun setUp() {
        // Active sale: Jan 1 ~ Mar 31
        em.persist(Product(
            name = "Winter Jacket",
            price = 120000,
            category = "Clothing",
            saleStartAt = LocalDateTime.of(2025, 1, 1, 0, 0),
            saleEndAt = LocalDateTime.of(2025, 3, 31, 23, 59),
        ))
        // Active sale: Jun 1 ~ Aug 31
        em.persist(Product(
            name = "Summer T-Shirt",
            price = 35000,
            category = "Clothing",
            saleStartAt = LocalDateTime.of(2025, 6, 1, 0, 0),
            saleEndAt = LocalDateTime.of(2025, 8, 31, 23, 59),
        ))
        // No sale dates (null)
        em.persist(Product(
            name = "Basic Socks",
            price = 5000,
            category = "Clothing",
            saleStartAt = null,
            saleEndAt = null,
        ))
        // Partial: start only
        em.persist(Product(
            name = "New Arrival Shoes",
            price = 80000,
            category = "Shoes",
            saleStartAt = LocalDateTime.of(2025, 2, 1, 0, 0),
            saleEndAt = null,
        ))
        em.flush()
        em.clear()
    }

    @Test
    fun `reverse between - finds products with active sale at given date`() {
        val now = LocalDateTime.of(2025, 2, 15, 12, 0)
        val result = productRepository.findActiveSales(now)

        // Both expression paths (saleStartAt, saleEndAt) are always non-null QueryDSL paths.
        // The reverse between generates: saleStartAt <= now AND saleEndAt >= now
        // Winter Jacket: 2025-01-01 <= 2025-02-15 AND 2025-03-31 >= 2025-02-15 -> true
        // Summer T-Shirt: 2025-06-01 <= 2025-02-15 -> false
        // Basic Socks: saleStartAt is DB null -> SQL evaluates to NULL, condition fails
        // New Arrival Shoes: saleStartAt(Feb 1) <= Feb 15 -> true, saleEndAt is DB null -> NULL, fails
        assertEquals(1, result.size)
        assertEquals("Winter Jacket", result[0].name)
    }

    @Test
    fun `reverse between - no match when outside all ranges`() {
        val now = LocalDateTime.of(2025, 5, 1, 12, 0)
        val result = productRepository.findActiveSales(now)

        // Winter Jacket: 2025-01-01 <= 2025-05-01 -> true, 2025-03-31 >= 2025-05-01 -> false
        // Summer T-Shirt: 2025-06-01 <= 2025-05-01 -> false
        // Basic Socks: DB nulls -> false
        // New Arrival Shoes: saleStartAt(Feb 1) <= May 1 -> true, saleEndAt DB null -> false
        assertEquals(0, result.size)
    }

    @Test
    fun `reverse between - null value returns all`() {
        val result = productRepository.findActiveSales(null)
        assertEquals(4, result.size)
    }
}
