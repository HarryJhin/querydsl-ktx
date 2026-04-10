package com.querydsl.ktx.autoconfigure

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ImportRuntimeHints

@AutoConfiguration(after = [HibernateJpaAutoConfiguration::class])
@ConditionalOnClass(JPAQueryFactory::class)
@ImportRuntimeHints(QuerydslKtxRuntimeHints::class)
class QuerydslKtxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jpaQueryFactory(entityManager: EntityManager) = JPAQueryFactory(entityManager)
}
