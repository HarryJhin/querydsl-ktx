package com.querydsl.ktx.support

import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.core.types.dsl.Wildcard
import com.querydsl.jpa.JPQLQuery
import com.querydsl.jpa.impl.JPADeleteClause
import com.querydsl.jpa.impl.JPAInsertClause
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.jpa.impl.JPAUpdateClause
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.data.querydsl.SimpleEntityPathResolver
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.support.PageableExecutionUtils

/**
 * QueryDSL 쿼리 팩토리 및 페이지네이션 헬퍼를 제공하는 베이스 클래스.
 *
 * DI, 쿼리 팩토리 래퍼, 페이지네이션 유틸리티만 포함합니다.
 * Extensions 인터페이스는 포함하지 않으므로 사용자가 원하는 것만 선택적으로 implement할 수 있습니다.
 *
 * 풀 패키지(모든 Extensions 포함)가 필요하면 [QuerydslRepository]를 사용하세요.
 *
 * 서브클래스는 반드시 [domainClass]를 override해야 합니다.
 *
 * ```kotlin
 * class MyRepository : QuerydslSupport<MyEntity>(), BooleanExpressionExtensions {
 *     override val domainClass = MyEntity::class.java
 * }
 * ```
 */
@NoRepositoryBean
abstract class QuerydslSupport<T : Any> {

    protected abstract val domainClass: Class<T>

    protected lateinit var jpaQueryFactory: JPAQueryFactory
        private set

    protected lateinit var entityManager: EntityManager
        private set

    private lateinit var querydsl: Querydsl

    protected val path: EntityPath<T> by lazy {
        SimpleEntityPathResolver.INSTANCE.createPath(domainClass)
    }

    @PersistenceContext
    private fun setEntityManager(entityManager: EntityManager) {
        this.entityManager = entityManager
    }

    @Autowired(required = false)
    private fun setJpaQueryFactory(jpaQueryFactory: JPAQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory
    }

    @PostConstruct
    private fun init() {
        if (!::jpaQueryFactory.isInitialized) {
            jpaQueryFactory = JPAQueryFactory(entityManager)
        }
        querydsl = Querydsl(entityManager, PathBuilder(path.type, path.metadata))
    }

    // ========================================
    // 쿼리 팩토리 래퍼
    // ========================================

    protected fun delete(path: EntityPath<*>): JPADeleteClause =
        jpaQueryFactory.delete(path)

    protected fun <R> select(expr: Expression<R>): JPAQuery<R> =
        jpaQueryFactory.select(expr)

    protected fun select(vararg exprs: Expression<*>): JPAQuery<Tuple> =
        jpaQueryFactory.select(*exprs)

    protected fun <R> selectDistinct(expr: Expression<R>): JPAQuery<R> =
        jpaQueryFactory.selectDistinct(expr)

    protected fun selectDistinct(vararg exprs: Expression<*>): JPAQuery<Tuple> =
        jpaQueryFactory.selectDistinct(*exprs)

    protected fun selectOne(): JPAQuery<Int> =
        jpaQueryFactory.selectOne()

    protected fun selectZero(): JPAQuery<Int> =
        jpaQueryFactory.selectZero()

    protected fun <R> selectFrom(from: EntityPath<R>): JPAQuery<R> =
        jpaQueryFactory.selectFrom(from)

    protected fun <R> Expression<R>.from(arg: EntityPath<*>): JPAQuery<R> =
        jpaQueryFactory.select(this).from(arg)

    protected fun update(path: EntityPath<*>): JPAUpdateClause =
        jpaQueryFactory.update(path)

    protected fun insert(path: EntityPath<*>): JPAInsertClause =
        jpaQueryFactory.insert(path)

    // ========================================
    // 조건 헬퍼
    // ========================================

    protected fun where(vararg conditions: BooleanExpression?): Array<BooleanExpression> =
        conditions.filterNotNull().toTypedArray()

    protected fun <V : Any> ifNotNull(value: V?, block: (V) -> BooleanExpression?): BooleanExpression? =
        value?.let(block)

    // ========================================
    // 페이지네이션 헬퍼
    // ========================================

    /**
     * Slice 기반 페이지네이션을 수행합니다.
     * pageSize + 1개를 fetch하여 다음 페이지 존재 여부를 정확히 판정합니다.
     *
     * @param pageable 페이지 정보
     * @return hasNext 값을 포함한 Slice 결과
     */
    protected fun <R> JPAQuery<R>.slicing(pageable: Pageable): Slice<R> {
        val limit = pageable.pageSize
        val content: List<R> = this
            .offset(pageable.offset)
            .limit(limit.toLong() + 1)
            .fetch()
        val hasNext = content.size > limit
        return SliceImpl(if (hasNext) content.subList(0, limit) else content, pageable, hasNext)
    }

    /**
     * 자동 카운트 쿼리를 사용한 페이지네이션을 수행합니다.
     *
     * **주의: fetch join이 포함된 쿼리에서는 사용할 수 없습니다!**
     * fetch join이 있는 경우 반드시 countQuery overload를 사용하세요:
     * ```kotlin
     * query.paging(pageable) {
     *     jpaQueryFactory.select(entity.count()).from(entity).where(condition).fetchOne() ?: 0L
     * }
     * ```
     *
     * @param pageable 페이지 정보
     * @return 전체 개수와 현재 페이지 데이터를 포함한 Page 결과
     */
    protected fun <R> JPAQuery<R>.paging(pageable: Pageable): Page<R> {
        val countQuery: JPAQuery<R> = this.clone()
        val content: List<R> = this.fetching(pageable)
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.select(Wildcard.count).fetchOne() ?: 0L
        }
    }

    /**
     * 별도 카운트 쿼리를 사용한 페이지네이션을 수행합니다.
     *
     * @param pageable 페이지 정보
     * @param countQuery 전체 개수를 반환하는 함수
     * @return 전체 개수와 현재 페이지 데이터를 포함한 Page 결과
     */
    protected fun <R> JPQLQuery<R>.paging(
        pageable: Pageable,
        countQuery: () -> Long?,
    ): Page<R> {
        val content: List<R> = this.fetching(pageable)
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery() ?: 0L
        }
    }

    protected fun <R> JPQLQuery<R>.fetching(pageable: Pageable): List<R> =
        querydsl.applyPagination(pageable, this).fetch()

    protected fun <R> JPQLQuery<R>.sort(sort: Sort, fallbackOrder: () -> OrderSpecifier<*>? = { null }): JPQLQuery<R> {
        if (sort.isUnsorted) {
            val defaultOrder = fallbackOrder() ?: return this
            return this.orderBy(defaultOrder)
        }
        return querydsl.applySorting(sort, this)
    }

    protected fun <R> JPQLQuery<R>.fetching(sort: Sort): List<R> =
        querydsl.applySorting(sort, this).fetch()

    protected fun <R> List<R>.paging(
        pageable: Pageable,
        countQuery: () -> Long,
    ): Page<R> = PageImpl(this, pageable, countQuery())

    // ========================================
    // modifying
    // ========================================

    protected fun <R> modifying(
        flushAutomatically: Boolean = true,
        clearAutomatically: Boolean = true,
        block: () -> R,
    ): R {
        if (flushAutomatically) entityManager.flush()
        return try {
            block()
        } finally {
            if (clearAutomatically) entityManager.clear()
        }
    }
}
