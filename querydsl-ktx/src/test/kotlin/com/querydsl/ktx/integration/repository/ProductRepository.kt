package com.querydsl.ktx.integration.repository

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
}
