package com.querydsl.ktx.integration.test

import com.querydsl.jpa.JPAExpressions
import com.querydsl.ktx.integration.TestConfig
import com.querydsl.ktx.integration.domain.Product
import com.querydsl.ktx.integration.domain.QProduct
import com.querydsl.ktx.integration.repository.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestConfig::class, ProductRepository::class)
class SubQueryComparisonTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var productRepository: ProductRepository

    private val product = QProduct.product

    @BeforeEach
    fun setUp() {
        em.persist(Product(name = "Cheap Pen", price = 1000, category = "Stationery"))
        em.persist(Product(name = "Mid Notebook", price = 5000, category = "Stationery"))
        em.persist(Product(name = "Premium Watch", price = 200000, category = "Accessory"))
        em.persist(Product(name = "Mid T-Shirt", price = 30000, category = "Clothing"))
        em.flush()
        em.clear()
    }

    @Test
    fun `eq subquery - matches product with max price`() {
        val maxPriceQuery = JPAExpressions.select(product.price.max()).from(product)
        val result = productRepository.findByPriceEq(maxPriceQuery)

        assertEquals(1, result.size)
        assertEquals("Premium Watch", result[0].name)
    }

    @Test
    fun `eq subquery - null skips filter and returns all`() {
        val result = productRepository.findByPriceEq(null)
        assertEquals(4, result.size)
    }

    @Test
    fun `in subquery - matches products in categories with cheap items`() {
        // Categories with at least one product priced under 10000: Stationery only
        val cheapCategoriesQuery = JPAExpressions
            .selectDistinct(product.category)
            .from(product)
            .where(product.price.lt(10000))
        val result = productRepository.findByCategoryIn(cheapCategoriesQuery)

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "Stationery" })
    }

    @Test
    fun `in subquery - null skips filter and returns all`() {
        val result = productRepository.findByCategoryIn(null)
        assertEquals(4, result.size)
    }

    @Test
    fun `notIn subquery - excludes products in cheap categories`() {
        val cheapCategoriesQuery = JPAExpressions
            .selectDistinct(product.category)
            .from(product)
            .where(product.price.lt(10000))
        val result = productRepository.findByCategoryNotIn(cheapCategoriesQuery)

        assertEquals(2, result.size)
        assertTrue(result.none { it.category == "Stationery" })
    }

    @Test
    fun `notIn subquery - null skips filter and returns all`() {
        val result = productRepository.findByCategoryNotIn(null)
        assertEquals(4, result.size)
    }
}
