package com.querydsl.ktx.autoconfigure

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuerydslKtxAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(QuerydslKtxAutoConfiguration::class.java))

    @Test
    fun `registers JPAQueryFactory when EntityManager is available`() {
        contextRunner
            .withBean(EntityManager::class.java, { Mockito.mock(EntityManager::class.java) })
            .run { context ->
                assertTrue(context.containsBean("jpaQueryFactory"))
                assertNotNull(context.getBean(JPAQueryFactory::class.java))
            }
    }

    @Test
    fun `backs off when user defines JPAQueryFactory bean`() {
        contextRunner
            .withUserConfiguration(CustomJpaQueryFactoryConfig::class.java)
            .run { context ->
                assertTrue(context.containsBean("customJpaQueryFactory"))
                val beans = context.getBeansOfType(JPAQueryFactory::class.java)
                assertTrue(beans.size == 1)
                assertTrue(beans.containsKey("customJpaQueryFactory"))
            }
    }

    @Test
    fun `does not register when JPAQueryFactory class is missing`() {
        contextRunner
            .withClassLoader(FilteredClassLoader(JPAQueryFactory::class.java))
            .run { context ->
                assertTrue(!context.containsBean("jpaQueryFactory"))
            }
    }

    @Test
    fun `does not register when multiple EntityManager beans exist`() {
        contextRunner
            .withBean("em1", EntityManager::class.java, { Mockito.mock(EntityManager::class.java) })
            .withBean("em2", EntityManager::class.java, { Mockito.mock(EntityManager::class.java) })
            .run { context ->
                assertTrue(!context.containsBean("jpaQueryFactory"))
            }
    }

    @Configuration(proxyBeanMethods = false)
    internal class CustomJpaQueryFactoryConfig {
        @Bean
        fun customJpaQueryFactory(): JPAQueryFactory {
            val em: EntityManager = Mockito.mock(EntityManager::class.java)
            return JPAQueryFactory(Supplier { em })
        }
    }
}
