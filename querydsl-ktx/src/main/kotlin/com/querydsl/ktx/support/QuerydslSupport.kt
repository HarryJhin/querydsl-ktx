package com.querydsl.ktx.support

import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.OrderSpecifier
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
 * Base class providing a QueryDSL query factory and pagination helpers.
 *
 * Contains only DI wiring, query factory wrappers, and pagination utilities.
 * Extension interfaces are intentionally excluded so you can selectively implement
 * only the ones you need.
 *
 * If you want all extensions included out of the box, use [QuerydslRepository] instead.
 *
 * Subclasses must override [domainClass].
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
    // Query factory wrappers
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
    // Pagination helpers
    // ========================================

    /**
     * Performs slice-based pagination by fetching pageSize + 1 rows to determine hasNext.
     *
     * Use this instead of [paging] when you only need forward navigation without a total count.
     *
     * @param pageable the page request
     * @return a [Slice] with accurate hasNext information
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
     * Performs pagination with an auto-generated count query.
     *
     * **Warning: Do not use with fetch joins!**
     * When fetch joins are present, use the overload that accepts a separate count query:
     * ```kotlin
     * query.paging(pageable) {
     *     jpaQueryFactory.select(entity.count()).from(entity).where(condition).fetchOne() ?: 0L
     * }
     * ```
     *
     * @param pageable the page request
     * @return a [Page] containing the current page data and total count
     */
    protected fun <R> JPAQuery<R>.paging(pageable: Pageable): Page<R> {
        val countQuery: JPAQuery<R> = this.clone()
        val content: List<R> = this.fetching(pageable)
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.select(Wildcard.count).fetchOne() ?: 0L
        }
    }

    /**
     * Performs pagination with a separate count query you provide.
     *
     * Use this overload when the main query contains fetch joins or other constructs
     * that prevent automatic count derivation.
     *
     * @param pageable the page request
     * @param countQuery a lambda returning the total row count
     * @return a [Page] containing the current page data and total count
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
