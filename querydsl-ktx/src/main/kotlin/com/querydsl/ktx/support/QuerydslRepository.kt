package com.querydsl.ktx.support

import com.querydsl.ktx.extensions.BooleanExpressionExtensions
import com.querydsl.ktx.extensions.CollectionExpressionExtensions
import com.querydsl.ktx.extensions.ComparableExpressionExtensions
import com.querydsl.ktx.extensions.NumberExpressionExtensions
import com.querydsl.ktx.extensions.SimpleExpressionExtensions
import com.querydsl.ktx.extensions.StringExpressionExtensions
import com.querydsl.ktx.extensions.SubQueryExtensions
import com.querydsl.ktx.extensions.TemporalExpressionExtensions
import org.springframework.core.GenericTypeResolver
import org.springframework.data.repository.NoRepositoryBean

/**
 * Batteries-included base class with all QueryDSL extension interfaces.
 *
 * Extends [QuerydslSupport] and implements all eight extension interfaces.
 * The type parameter `T` is automatically resolved via [GenericTypeResolver],
 * so you do not need to specify [domainClass] manually.
 *
 * ```kotlin
 * @Repository
 * class MemberQueryRepository : QuerydslRepository<Member>() {
 *     fun findByName(name: String): List<Member> =
 *         selectFrom(qMember).where(qMember.name eq name).fetch()
 * }
 * ```
 *
 * If you only need a subset of extensions, extend [QuerydslSupport] directly
 * and implement the desired interfaces yourself.
 */
@NoRepositoryBean
abstract class QuerydslRepository<T : Any> :
    QuerydslSupport<T>(),
    BooleanExpressionExtensions,
    CollectionExpressionExtensions,
    ComparableExpressionExtensions,
    NumberExpressionExtensions,
    SimpleExpressionExtensions,
    StringExpressionExtensions,
    SubQueryExtensions,
    TemporalExpressionExtensions {

    @Suppress("UNCHECKED_CAST")
    override val domainClass: Class<T> =
        requireNotNull(GenericTypeResolver.resolveTypeArgument(this::class.java, QuerydslRepository::class.java)) {
            "Could not resolve domain type for ${this::class.simpleName}. " +
                "Ensure the class extends QuerydslRepository with a concrete type parameter."
        } as Class<T>
}
