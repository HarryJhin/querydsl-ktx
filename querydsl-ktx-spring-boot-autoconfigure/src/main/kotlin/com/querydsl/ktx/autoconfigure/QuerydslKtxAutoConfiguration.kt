package com.querydsl.ktx.autoconfigure

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(JPAQueryFactory::class)
@ConditionalOnBean(EntityManager::class)
class QuerydslKtxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jpaQueryFactory(entityManager: EntityManager) = JPAQueryFactory(entityManager)
}
