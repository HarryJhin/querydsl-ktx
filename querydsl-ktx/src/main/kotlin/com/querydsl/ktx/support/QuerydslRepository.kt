package com.querydsl.ktx.support

import com.querydsl.ktx.extensions.BooleanExpressionExtensions
import com.querydsl.ktx.extensions.CollectionExpressionExtensions
import com.querydsl.ktx.extensions.ComparableExpressionExtensions
import com.querydsl.ktx.extensions.NumberExpressionExtensions
import com.querydsl.ktx.extensions.SimpleExpressionExtensions
import com.querydsl.ktx.extensions.StringExpressionExtensions
import com.querydsl.ktx.extensions.TemporalExpressionExtensions
import org.springframework.core.GenericTypeResolver
import org.springframework.data.repository.NoRepositoryBean

/**
 * 모든 QueryDSL Extensions를 포함하는 풀 베이스 클래스.
 *
 * [QuerydslSupport]를 상속하며 7개의 Extensions 인터페이스를 모두 구현합니다.
 * 타입 파라미터 `T`는 [GenericTypeResolver]로 자동 해석되므로 [domainClass]를 직접 지정할 필요가 없습니다.
 *
 * ```kotlin
 * @Repository
 * class MemberQueryRepository : QuerydslRepository<Member>() {
 *     fun findByName(name: String): List<Member> =
 *         selectFrom(qMember).where(qMember.name eq name).fetch()
 * }
 * ```
 *
 * 일부 Extensions만 필요하면 [QuerydslSupport]를 직접 상속하고 원하는 인터페이스만 implement하세요.
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
    TemporalExpressionExtensions {

    @Suppress("UNCHECKED_CAST")
    override val domainClass: Class<T> =
        requireNotNull(GenericTypeResolver.resolveTypeArgument(this::class.java, QuerydslRepository::class.java)) {
            "Could not resolve domain type for ${this::class.simpleName}. " +
                "Ensure the class extends QuerydslRepository with a concrete type parameter."
        } as Class<T>
}
