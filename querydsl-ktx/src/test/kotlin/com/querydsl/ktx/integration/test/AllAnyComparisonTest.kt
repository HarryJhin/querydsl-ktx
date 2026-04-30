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
class AllAnyComparisonTest {

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
    fun `gtAll subquery - greater than every Stationery price`() {
        // Stationery prices: 1000, 5000 -> max 5000
        // price > ALL (Stationery) -> price > 5000 -> Watch (200000), T-Shirt (30000)
        val stationeryPrices = JPAExpressions
            .select(product.price)
            .from(product)
            .where(product.category.eq("Stationery"))
        val result = productRepository.findGtAllSubQuery(stationeryPrices)
        assertEquals(2, result.size)
        assertTrue(result.all { it.price > 5000 })
    }

    @Test
    fun `gtAll subquery - null skips filter`() {
        val result = productRepository.findGtAllSubQuery(null)
        assertEquals(4, result.size)
    }

    @Test
    fun `gtAny subquery - greater than at least one Stationery price`() {
        // Stationery prices: 1000, 5000 -> min 1000
        // price > ANY (Stationery) -> price > 1000 -> Notebook(5000), T-Shirt(30000), Watch(200000)
        val stationeryPrices = JPAExpressions
            .select(product.price)
            .from(product)
            .where(product.category.eq("Stationery"))
        val result = productRepository.findGtAnySubQuery(stationeryPrices)
        assertEquals(3, result.size)
        assertTrue(result.all { it.price > 1000 })
    }

    @Test
    fun `gtAny subquery - null skips filter`() {
        val result = productRepository.findGtAnySubQuery(null)
        assertEquals(4, result.size)
    }

    @Test
    fun `eqAny subquery - matches any Stationery price`() {
        // Stationery prices: 1000, 5000
        // price = ANY (Stationery) -> price in {1000, 5000} -> Cheap Pen, Mid Notebook
        val stationeryPrices = JPAExpressions
            .select(product.price)
            .from(product)
            .where(product.category.eq("Stationery"))
        val result = productRepository.findEqAnySubQuery(stationeryPrices)
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "Stationery" })
    }

    @Test
    fun `eqAny subquery - null skips filter`() {
        val result = productRepository.findEqAnySubQuery(null)
        assertEquals(4, result.size)
    }

    @Test
    fun `loeAll subquery - lexicographically le every expensive category`() {
        // expensive categories (price > 5000) = distinct categories ['Accessory', 'Clothing']
        // category <= ALL ('Accessory', 'Clothing') -> category <= 'Accessory' (the smaller)
        // Only 'Accessory' satisfies <= 'Accessory' AND <= 'Clothing' -> Premium Watch (1)
        val expensiveCategories = JPAExpressions
            .selectDistinct(product.category)
            .from(product)
            .where(product.price.gt(5000))
        val result = productRepository.findLoeAllCategory(expensiveCategories)
        assertEquals(1, result.size)
        assertEquals("Premium Watch", result[0].name)
    }

    @Test
    fun `loeAll subquery - null skips filter`() {
        val result = productRepository.findLoeAllCategory(null)
        assertEquals(4, result.size)
    }
}
