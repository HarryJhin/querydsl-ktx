package com.querydsl.ktx.autoconfigure

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

/**
 * Registers runtime hints for GraalVM native image compatibility.
 *
 * querydsl-ktx uses [GenericTypeResolver][org.springframework.core.GenericTypeResolver]
 * in [QuerydslRepository][com.querydsl.ktx.support.QuerydslRepository] to resolve
 * the domain class type parameter at runtime. This requires reflection hints for
 * the repository base classes.
 *
 * Note: QueryDSL 5.1.0 itself does not officially support GraalVM native image.
 * Users may need additional reflection configuration for QueryDSL core classes.
 * See: https://github.com/querydsl/querydsl/issues/3646
 */
class QuerydslKtxRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection()
            .registerType(
                com.querydsl.ktx.support.QuerydslSupport::class.java,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
            )
            .registerType(
                com.querydsl.ktx.support.QuerydslRepository::class.java,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
            )
    }
}
