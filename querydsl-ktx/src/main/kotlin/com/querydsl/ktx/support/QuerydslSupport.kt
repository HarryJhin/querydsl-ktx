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
import org.springframework.data.domain.PageRequest
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
     * Use this instead of [page] when you only need forward navigation without a total count.
     *
     * @param pageable the page request
     * @return a [Slice] with accurate hasNext information
     */
    protected fun <R> JPQLQuery<R>.slice(pageable: Pageable): Slice<R> {
        val limit = pageable.pageSize
        val content: List<R> = this
            .offset(pageable.offset)
            .limit(limit.toLong() + 1)
            .fetch()
        val hasNext = content.size > limit
        return SliceImpl(if (hasNext) content.subList(0, limit) else content, pageable, hasNext)
    }

    /**
     * Performs slice-based pagination with [SortSpec]-based sorting.
     *
     * Sorting is resolved through [spec] instead of the [Pageable]'s own sort,
     * so client-supplied property names are validated against the explicit mapping.
     *
     * ```kotlin
     * selectFrom(qMember)
     *     .where(condition)
     *     .slice(pageable, memberSort)
     * ```
     *
     * @param pageable the page request (sort is ignored; use [spec] instead)
     * @param spec the [SortSpec] mapping property names to expressions
     * @param fallback optional default order when sort resolves to nothing
     * @return a [Slice] with accurate hasNext information
     */
    protected fun <R> JPQLQuery<R>.slice(
        pageable: Pageable,
        spec: SortSpec,
        fallback: (() -> OrderSpecifier<*>?)? = null,
    ): Slice<R> =
        this.applySort(pageable.sort, spec, fallback).slice(pageable.withoutSort)

    /**
     * Performs pagination with an auto-generated count query.
     *
     * **Warning: Do not use with fetch joins!**
     * When fetch joins are present, use the overload that accepts a separate count query:
     * ```kotlin
     * query.page(pageable) {
     *     jpaQueryFactory.select(entity.count()).from(entity).where(condition).fetchOne() ?: 0L
     * }
     * ```
     *
     * @param pageable the page request
     * @return a [Page] containing the current page data and total count
     */
    protected fun <R> JPAQuery<R>.page(pageable: Pageable): Page<R> {
        val countQuery: JPAQuery<R> = this.clone()
        val content: List<R> = this.fetch(pageable)
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.select(Wildcard.count).fetchOne() ?: 0L
        }
    }

    /**
     * Performs pagination with [SortSpec]-based sorting and an auto-generated count query.
     *
     * ```kotlin
     * selectFrom(qMember)
     *     .where(condition)
     *     .page(pageable, memberSort)
     * ```
     *
     * @param pageable the page request (sort is ignored; use [spec] instead)
     * @param spec the [SortSpec] mapping property names to expressions
     * @param fallback optional default order when sort resolves to nothing
     * @return a [Page] containing the current page data and total count
     */
    protected fun <R> JPAQuery<R>.page(
        pageable: Pageable,
        spec: SortSpec,
        fallback: (() -> OrderSpecifier<*>?)? = null,
    ): Page<R> {
        val countQuery: JPAQuery<R> = this.clone()
        val unsorted = pageable.withoutSort
        val content: List<R> = this.applySort(pageable.sort, spec, fallback).fetch(unsorted)
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
    protected fun <R> JPQLQuery<R>.page(
        pageable: Pageable,
        countQuery: () -> Long?,
    ): Page<R> {
        val content: List<R> = this.fetch(pageable)
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery() ?: 0L
        }
    }

    /**
     * Performs pagination with [SortSpec]-based sorting and a separate count query.
     *
     * ```kotlin
     * selectFrom(qMember)
     *     .where(condition)
     *     .page(pageable, memberSort) {
     *         jpaQueryFactory.select(qMember.count()).from(qMember).where(condition).fetchOne() ?: 0L
     *     }
     * ```
     *
     * @param pageable the page request (sort is ignored; use [spec] instead)
     * @param spec the [SortSpec] mapping property names to expressions
     * @param fallback optional default order when sort resolves to nothing
     * @param countQuery a lambda returning the total row count
     * @return a [Page] containing the current page data and total count
     */
    protected fun <R> JPQLQuery<R>.page(
        pageable: Pageable,
        spec: SortSpec,
        fallback: (() -> OrderSpecifier<*>?)? = null,
        countQuery: () -> Long?,
    ): Page<R> {
        val unsorted = pageable.withoutSort
        val content: List<R> = this.applySort(pageable.sort, spec, fallback).fetch(unsorted)
        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery() ?: 0L
        }
    }

    /**
     * Performs slice-based pagination using page number and page size.
     *
     * Convenience overload that accepts raw values instead of [Pageable].
     *
     * @param page zero-based page number
     * @param size number of items per page
     * @return a [Slice] with accurate hasNext information
     */
    protected fun <R> JPQLQuery<R>.slice(page: Int, size: Int): Slice<R> =
        slice(PageRequest.of(page, size))

    /**
     * Performs pagination with an auto-generated count query using page number and page size.
     *
     * Convenience overload that accepts raw values instead of [Pageable].
     *
     * @param page zero-based page number
     * @param size number of items per page
     * @return a [Page] containing the current page data and total count
     */
    protected fun <R> JPAQuery<R>.page(page: Int, size: Int): Page<R> =
        page(PageRequest.of(page, size))

    /**
     * Performs pagination with a separate count query using page number and page size.
     *
     * Convenience overload that accepts raw values instead of [Pageable].
     *
     * @param page zero-based page number
     * @param size number of items per page
     * @param countQuery a lambda returning the total row count
     * @return a [Page] containing the current page data and total count
     */
    protected fun <R> JPQLQuery<R>.page(page: Int, size: Int, countQuery: () -> Long?): Page<R> =
        page(PageRequest.of(page, size), countQuery)

    protected fun <R> JPQLQuery<R>.fetch(pageable: Pageable): List<R> =
        querydsl.applyPagination(pageable, this).fetch()

    /**
     * Fetches a page of results using offset and limit.
     *
     * Convenience overload that accepts raw values instead of [Pageable].
     *
     * @param offset number of rows to skip
     * @param limit maximum number of rows to return
     * @return list of results
     */
    protected fun <R> JPQLQuery<R>.fetch(offset: Long, limit: Int): List<R> =
        this.offset(offset).limit(limit.toLong()).fetch()

    protected fun <R> JPQLQuery<R>.applySort(sort: Sort, fallbackOrder: () -> OrderSpecifier<*>? = { null }): JPQLQuery<R> {
        if (sort.isUnsorted) {
            val defaultOrder = fallbackOrder() ?: return this
            return this.orderBy(defaultOrder)
        }
        return querydsl.applySorting(sort, this)
    }

    /**
     * Applies sorting using a [SortSpec] that maps property names to QueryDSL expressions.
     *
     * Unlike the [Querydsl.applySorting]-based overload, this resolves sort properties
     * through an explicit mapping, giving you full control over which properties are
     * sortable and how they map to expressions (including cross-entity paths).
     *
     * ```kotlin
     * private val memberSort = sortSpec {
     *     "name"       by qMember.name
     *     "createdAt"  by qMember.createdAt
     *     "department" by qDepartment.name   // join column — PathBuilder can't resolve this
     * }
     *
     * fun findAll(pageable: Pageable): Page<Member> =
     *     selectFrom(qMember)
     *         .join(qMember.department, qDepartment)
     *         .page(pageable, memberSort) { qMember.createdAt.desc() }
     * ```
     *
     * @param sort the Spring Data [Sort] from the client
     * @param spec the [SortSpec] mapping property names to expressions
     * @param fallback optional default order when [sort] is unsorted or resolves to nothing
     * @return the query with ordering applied
     */
    protected fun <R> JPQLQuery<R>.applySort(
        sort: Sort,
        spec: SortSpec,
        fallback: (() -> OrderSpecifier<*>?)? = null,
    ): JPQLQuery<R> {
        val orders = spec.resolve(sort)
        if (orders.isNotEmpty()) {
            return this.orderBy(*orders.toTypedArray())
        }
        val fallbackOrder = fallback?.invoke() ?: return this
        return this.orderBy(fallbackOrder)
    }

    protected fun <R> JPQLQuery<R>.fetch(sort: Sort): List<R> =
        querydsl.applySorting(sort, this).fetch()

    protected fun <R> List<R>.page(
        pageable: Pageable,
        countQuery: () -> Long,
    ): Page<R> = PageImpl(this, pageable, countQuery())

    protected val Pageable.withoutSort: Pageable
        get() = PageRequest.of(pageNumber, pageSize)

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
