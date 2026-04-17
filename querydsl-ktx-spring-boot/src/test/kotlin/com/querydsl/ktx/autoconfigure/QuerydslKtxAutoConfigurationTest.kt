package com.querydsl.ktx.autoconfigure

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuerydslKtxAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(QuerydslKtxAutoConfiguration::class.java))

    private fun mockEmf(): EntityManagerFactory = Mockito.mock(EntityManagerFactory::class.java)

    @Test
    fun `registers JPAQueryFactory when EntityManagerFactory is available`() {
        contextRunner
            .withBean(EntityManagerFactory::class.java, { mockEmf() })
            .run { context ->
                assertTrue(context.containsBean("jpaQueryFactory"))
                assertNotNull(context.getBean(JPAQueryFactory::class.java))
            }
    }

    @Test
    fun `backs off when user defines JPAQueryFactory bean`() {
        contextRunner
            .withBean(EntityManagerFactory::class.java, { mockEmf() })
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
    fun `does not register when multiple EntityManagerFactory beans exist without Primary`() {
        contextRunner
            .withBean("emf1", EntityManagerFactory::class.java, { mockEmf() })
            .withBean("emf2", EntityManagerFactory::class.java, { mockEmf() })
            .run { context ->
                assertTrue(!context.containsBean("jpaQueryFactory"))
            }
    }

    @Test
    fun `registers JPAQueryFactory using Primary EntityManagerFactory in multi-datasource setup`() {
        contextRunner
            .withUserConfiguration(PrimaryEmfConfig::class.java)
            .run { context ->
                assertTrue(context.containsBean("jpaQueryFactory"))
                assertNotNull(context.getBean(JPAQueryFactory::class.java))
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

    @Configuration(proxyBeanMethods = false)
    internal class PrimaryEmfConfig {
        @Bean
        @Primary
        fun primaryEntityManagerFactory(): EntityManagerFactory =
            Mockito.mock(EntityManagerFactory::class.java)

        @Bean
        fun secondaryEntityManagerFactory(): EntityManagerFactory =
            Mockito.mock(EntityManagerFactory::class.java)
    }
}
