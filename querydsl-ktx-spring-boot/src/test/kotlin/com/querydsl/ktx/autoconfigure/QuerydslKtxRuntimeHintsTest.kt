package com.querydsl.ktx.autoconfigure

import com.querydsl.ktx.support.QuerydslRepository
import com.querydsl.ktx.support.QuerydslSupport
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates
import kotlin.test.Test
import kotlin.test.assertTrue

class QuerydslKtxRuntimeHintsTest {

    @Test
    fun `registers reflection hints for QuerydslSupport`() {
        val hints = RuntimeHints()
        QuerydslKtxRuntimeHints().registerHints(hints, javaClass.classLoader)
        assertTrue(RuntimeHintsPredicates.reflection().onType(QuerydslSupport::class.java).test(hints))
    }

    @Test
    fun `registers reflection hints for QuerydslRepository`() {
        val hints = RuntimeHints()
        QuerydslKtxRuntimeHints().registerHints(hints, javaClass.classLoader)
        assertTrue(RuntimeHintsPredicates.reflection().onType(QuerydslRepository::class.java).test(hints))
    }

    @Test
    fun `registers INVOKE_DECLARED_METHODS for QuerydslSupport`() {
        val hints = RuntimeHints()
        QuerydslKtxRuntimeHints().registerHints(hints, javaClass.classLoader)
        assertTrue(
            RuntimeHintsPredicates.reflection()
                .onType(QuerydslSupport::class.java)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS)
                .test(hints)
        )
    }

    @Test
    fun `registers INVOKE_DECLARED_METHODS for QuerydslRepository`() {
        val hints = RuntimeHints()
        QuerydslKtxRuntimeHints().registerHints(hints, javaClass.classLoader)
        assertTrue(
            RuntimeHintsPredicates.reflection()
                .onType(QuerydslRepository::class.java)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS)
                .test(hints)
        )
    }
}
