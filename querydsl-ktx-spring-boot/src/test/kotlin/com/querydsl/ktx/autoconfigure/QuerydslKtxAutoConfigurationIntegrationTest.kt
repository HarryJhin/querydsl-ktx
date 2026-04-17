package com.querydsl.ktx.autoconfigure

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 실 Spring Boot JPA 환경에서 AutoConfig가 활성화되고 JPAQueryFactory 빈이
 * 주입 가능한지 검증. ApplicationContextRunner 기반 단위 테스트는 EntityManager
 * proxy 빈 등록 타이밍 차이를 잡지 못하므로 이 테스트로 회귀 방지.
 */
@SpringBootTest(classes = [QuerydslKtxAutoConfigurationIntegrationTest.TestApp::class])
class QuerydslKtxAutoConfigurationIntegrationTest {

    @Autowired
    lateinit var context: ApplicationContext

    @Autowired
    lateinit var jpaQueryFactory: JPAQueryFactory

    @Test
    fun `AutoConfig registers JPAQueryFactory in real Spring Boot JPA context`() {
        assertTrue(
            context.containsBean("jpaQueryFactory"),
            "QuerydslKtxAutoConfiguration should register 'jpaQueryFactory' bean",
        )
        assertNotNull(jpaQueryFactory)
    }

    @Test
    fun `JPAQueryFactory builds query objects from the shared EntityManager proxy`() {
        val query = jpaQueryFactory.selectOne()
        assertNotNull(query)
    }

    @SpringBootApplication
    open class TestApp
}
