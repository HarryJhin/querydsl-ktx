package com.querydsl.ktx.autoconfigure

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.orm.jpa.SharedEntityManagerCreator

@AutoConfiguration(after = [HibernateJpaAutoConfiguration::class])
@ConditionalOnClass(JPAQueryFactory::class)
@ConditionalOnSingleCandidate(EntityManagerFactory::class)
@ImportRuntimeHints(QuerydslKtxRuntimeHints::class)
class QuerydslKtxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jpaQueryFactory(entityManagerFactory: EntityManagerFactory): JPAQueryFactory =
        JPAQueryFactory(SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory))
}
