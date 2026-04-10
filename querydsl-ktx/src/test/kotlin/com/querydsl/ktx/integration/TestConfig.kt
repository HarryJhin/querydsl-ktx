package com.querydsl.ktx.integration

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class TestConfig {
    @Bean
    open fun jpaQueryFactory(entityManager: EntityManager) = JPAQueryFactory(entityManager)
}
