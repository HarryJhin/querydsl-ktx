package com.querydsl.ktx.integration.repository

import com.querydsl.core.types.SubQueryExpression
import com.querydsl.ktx.integration.domain.Product
import com.querydsl.ktx.integration.domain.QProduct
import com.querydsl.ktx.support.QuerydslRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ProductRepository : QuerydslRepository<Product>() {
    private val product = QProduct.product

    fun findActiveSales(now: LocalDateTime? = null): List<Product> =
        selectFrom(product)
            .where(now between (product.saleStartAt to product.saleEndAt))
            .fetch()

    fun findActiveSalesWithRangeTo(now: LocalDateTime? = null): List<Product> =
        selectFrom(product)
            .where(now between (product.saleStartAt..product.saleEndAt))
            .fetch()

    fun findByPriceEq(priceSubQuery: SubQueryExpression<Int>?): List<Product> =
        selectFrom(product)
            .where(product.price eq priceSubQuery)
            .fetch()

    fun findByCategoryIn(categorySubQuery: SubQueryExpression<String>?): List<Product> =
        selectFrom(product)
            .where(product.category `in` categorySubQuery)
            .fetch()

    fun findByCategoryNotIn(categorySubQuery: SubQueryExpression<String>?): List<Product> =
        selectFrom(product)
            .where(product.category notIn categorySubQuery)
            .fetch()
}
