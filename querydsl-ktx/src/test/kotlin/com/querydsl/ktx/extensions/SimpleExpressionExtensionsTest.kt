package com.querydsl.ktx.extensions

import com.querydsl.core.DefaultQueryMetadata
import com.querydsl.core.types.CollectionExpression
import com.querydsl.core.types.ExpressionException
import com.querydsl.core.types.SubQueryExpression
import com.querydsl.core.types.Visitor
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SimpleExpressionExtensionsTest : SimpleExpressionExtensions {

    private val status: StringExpression = Expressions.stringPath("status")
    private val defaultStatus: StringExpression = Expressions.stringPath("defaultStatus")
    private val nullExpr: SimpleExpression<String>? = null

    // ── eq(T?) ──

    @Test
    fun `eq value - both non-null returns EQ expression`() {
        val result = status eq "ACTIVE"
        assertNotNull(result)
        assertTrue(result.toString().contains("="))
    }

    @Test
    fun `eq value - this null returns null`() {
        val result = nullExpr eq "ACTIVE"
        assertNull(result)
    }

    @Test
    fun `eq value - right null returns null`() {
        val result = status eq (null as String?)
        assertNull(result)
    }

    @Test
    fun `eq value - both null returns null`() {
        val result = nullExpr eq (null as String?)
        assertNull(result)
    }

    // ── eq(Expression) ──

    @Test
    fun `eq expression - both non-null returns EQ expression`() {
        val result = status eq defaultStatus
        assertNotNull(result)
        assertTrue(result.toString().contains("="))
    }

    @Test
    fun `eq expression - this null returns null`() {
        val result = nullExpr eq defaultStatus
        assertNull(result)
    }

    @Test
    fun `eq expression - right null returns null`() {
        val result = status eq (null as StringExpression?)
        assertNull(result)
    }

    @Test
    fun `eq expression - both null returns null`() {
        val result = nullExpr eq (null as StringExpression?)
        assertNull(result)
    }

    // ── ne(T?) ──

    @Test
    fun `ne value - both non-null returns NE expression`() {
        val result = status ne "DELETED"
        assertNotNull(result)
        assertTrue(result.toString().contains("!="))
    }

    @Test
    fun `ne value - this null returns null`() {
        val result = nullExpr ne "DELETED"
        assertNull(result)
    }

    @Test
    fun `ne value - right null returns null`() {
        val result = status ne (null as String?)
        assertNull(result)
    }

    @Test
    fun `ne value - both null returns null`() {
        val result = nullExpr ne (null as String?)
        assertNull(result)
    }

    // ── ne(Expression) ──

    @Test
    fun `ne expression - both non-null returns NE expression`() {
        val result = status ne defaultStatus
        assertNotNull(result)
        assertTrue(result.toString().contains("!="))
    }

    @Test
    fun `ne expression - this null returns null`() {
        val result = nullExpr ne defaultStatus
        assertNull(result)
    }

    @Test
    fun `ne expression - right null returns null`() {
        val result = status ne (null as StringExpression?)
        assertNull(result)
    }

    @Test
    fun `ne expression - both null returns null`() {
        val result = nullExpr ne (null as StringExpression?)
        assertNull(result)
    }

    // ── in ──

    @Test
    fun `in - both non-null returns IN expression`() {
        val result = status `in` listOf("ACTIVE", "PENDING")
        assertNotNull(result)
        assertTrue(result.toString().contains(" in "))
    }

    @Test
    fun `in - this null returns null`() {
        val result = nullExpr `in` listOf("ACTIVE", "PENDING")
        assertNull(result)
    }

    @Test
    fun `in - right null returns null`() {
        val result = status `in` (null as Collection<String>?)
        assertNull(result)
    }

    @Test
    fun `in - both null returns null`() {
        val result = nullExpr `in` (null as Collection<String>?)
        assertNull(result)
    }

    @Test
    fun `in - empty collection still generates expression`() {
        val result = status `in` emptyList<String>()
        assertNotNull(result)
    }

    // ── notIn ──

    @Test
    fun `notIn - both non-null returns NOT IN expression`() {
        val result = status notIn listOf("DELETED", "BLOCKED")
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("not in"))
    }

    @Test
    fun `notIn - this null returns null`() {
        val result = nullExpr notIn listOf("DELETED", "BLOCKED")
        assertNull(result)
    }

    @Test
    fun `notIn - right null returns null`() {
        val result = status notIn (null as Collection<String>?)
        assertNull(result)
    }

    @Test
    fun `notIn - both null returns null`() {
        val result = nullExpr notIn (null as Collection<String>?)
        assertNull(result)
    }

    @Test
    fun `notIn - empty collection still generates expression`() {
        val result = status notIn emptyList<String>()
        assertNotNull(result)
    }

    // ── inChunked (infix) ──

    @Test
    fun `inChunked - both non-null with small list returns IN expression`() {
        val result = status inChunked listOf("A", "B", "C")
        assertNotNull(result)
        assertTrue(result.toString().contains(" in "))
    }

    @Test
    fun `inChunked - this null returns null`() {
        val result = nullExpr inChunked listOf("A", "B")
        assertNull(result)
    }

    @Test
    fun `inChunked - right null returns null`() {
        val result = status inChunked (null as Collection<String>?)
        assertNull(result)
    }

    @Test
    fun `inChunked - both null returns null`() {
        val result = nullExpr inChunked (null as Collection<String>?)
        assertNull(result)
    }

    @Test
    fun `inChunked - empty collection still generates expression`() {
        val result = status inChunked emptyList<String>()
        assertNotNull(result)
    }

    @Test
    fun `inChunked - large list is split into OR-ed chunks`() {
        val largeList = (1..1001).map { it.toString() }
        val result = status inChunked largeList
        assertNotNull(result)
        // 1001 items with default chunk size 1000 -> 2 chunks joined by OR
        assert(result.toString().contains("||")) { "Expected OR expression for chunked IN, got: $result" }
    }

    @Test
    fun `inChunked - exactly chunk size uses single IN`() {
        val exactList = (1..1000).map { it.toString() }
        val result = status inChunked exactList
        assertNotNull(result)
        // Should be a single IN, no OR
        assert(!result.toString().contains("||")) { "Expected single IN without OR, got: $result" }
    }

    // ── inChunked (custom chunk size) ──

    @Test
    fun `inChunked with custom chunk size - splits correctly`() {
        val list = (1..10).map { it.toString() }
        val result = status.inChunked(list, chunkSize = 3)
        assertNotNull(result)
        // 10 items / 3 = 4 chunks -> 3 OR operators
        assert(result.toString().contains("||")) { "Expected OR expression for chunked IN, got: $result" }
    }

    @Test
    fun `inChunked with custom chunk size - small list uses single IN`() {
        val list = listOf("A", "B")
        val result = status.inChunked(list, chunkSize = 5)
        assertNotNull(result)
        assert(!result.toString().contains("||")) { "Expected single IN without OR, got: $result" }
    }

    @Test
    fun `inChunked - chunkSize zero throws ExpressionException`() {
        assertFailsWith<ExpressionException> {
            status.inChunked(listOf("A", "B"), chunkSize = 0)
        }
    }

    @Test
    fun `inChunked - negative chunkSize throws ExpressionException`() {
        assertFailsWith<ExpressionException> {
            status.inChunked(listOf("A", "B"), chunkSize = -1)
        }
    }

    @Test
    fun `inChunked - single element returns simple IN`() {
        val result = status.inChunked(listOf("A"))
        assertNotNull(result)
        // single element fits within default chunk size, so no OR
        assertTrue(!result.toString().contains("||"))
    }

    @Test
    fun `inChunked - chunkSize 1 creates OR per element`() {
        val result = status.inChunked(listOf("A", "B", "C"), chunkSize = 1)
        assertNotNull(result)
        assertTrue(result.toString().contains(" || "))
    }

    // ── eq(SubQueryExpression) / in(SubQueryExpression) / notIn(SubQueryExpression) ──

    private val subQuery: SubQueryExpression<String> = object : SubQueryExpression<String> {
        override fun getMetadata() = DefaultQueryMetadata()
        override fun <R, C> accept(v: Visitor<R, C>, context: C?): R = throw UnsupportedOperationException()
        override fun getType(): Class<out String> = String::class.java
    }
    private val nullSubQuery: SubQueryExpression<String>? = null

    @Test
    fun `eq subquery - both non-null returns expression`() {
        val result = status eq subQuery
        assertNotNull(result)
    }

    @Test
    fun `eq subquery - this null returns null`() {
        val result = nullExpr eq subQuery
        assertNull(result)
    }

    @Test
    fun `eq subquery - right null returns null`() {
        val result = status eq nullSubQuery
        assertNull(result)
    }

    @Test
    fun `eq subquery - both null returns null`() {
        val result = nullExpr eq nullSubQuery
        assertNull(result)
    }

    @Test
    fun `in subquery - both non-null returns expression`() {
        val result = status `in` subQuery
        assertNotNull(result)
    }

    @Test
    fun `in subquery - this null returns null`() {
        val result = nullExpr `in` subQuery
        assertNull(result)
    }

    @Test
    fun `in subquery - right null returns null`() {
        val result = status `in` nullSubQuery
        assertNull(result)
    }

    @Test
    fun `in subquery - both null returns null`() {
        val result = nullExpr `in` nullSubQuery
        assertNull(result)
    }

    @Test
    fun `notIn subquery - both non-null returns expression`() {
        val result = status notIn subQuery
        assertNotNull(result)
    }

    @Test
    fun `notIn subquery - this null returns null`() {
        val result = nullExpr notIn subQuery
        assertNull(result)
    }

    @Test
    fun `notIn subquery - right null returns null`() {
        val result = status notIn nullSubQuery
        assertNull(result)
    }

    @Test
    fun `notIn subquery - both null returns null`() {
        val result = nullExpr notIn nullSubQuery
        assertNull(result)
    }

    // ── ALL/Any variants (#90) ──

    private val collectionExpr: CollectionExpression<List<String>, String> = object : CollectionExpression<List<String>, String> {
        @Suppress("UNCHECKED_CAST")
        override fun getType(): Class<out List<String>> = List::class.java as Class<out List<String>>
        override fun <R, C> accept(v: Visitor<R, C>, context: C?): R = throw UnsupportedOperationException()
        override fun getParameter(index: Int): Class<*> = String::class.java
    }
    private val nullCollectionExpr: CollectionExpression<List<String>, String>? = null

    // eqAll(Collection)
    @Test fun `eqAll collection - both non-null`() = assertNotNull(status eqAll collectionExpr)
    @Test fun `eqAll collection - this null`() = assertNull(nullExpr eqAll collectionExpr)
    @Test fun `eqAll collection - right null`() = assertNull(status eqAll nullCollectionExpr)
    @Test fun `eqAll collection - both null`() = assertNull(nullExpr eqAll nullCollectionExpr)

    // eqAll(SubQuery)
    @Test fun `eqAll subquery - both non-null`() = assertNotNull(status eqAll subQuery)
    @Test fun `eqAll subquery - this null`() = assertNull(nullExpr eqAll subQuery)
    @Test fun `eqAll subquery - right null`() = assertNull(status eqAll nullSubQuery)
    @Test fun `eqAll subquery - both null`() = assertNull(nullExpr eqAll nullSubQuery)

    // eqAny(Collection)
    @Test fun `eqAny collection - both non-null`() = assertNotNull(status eqAny collectionExpr)
    @Test fun `eqAny collection - this null`() = assertNull(nullExpr eqAny collectionExpr)
    @Test fun `eqAny collection - right null`() = assertNull(status eqAny nullCollectionExpr)
    @Test fun `eqAny collection - both null`() = assertNull(nullExpr eqAny nullCollectionExpr)

    // eqAny(SubQuery)
    @Test fun `eqAny subquery - both non-null`() = assertNotNull(status eqAny subQuery)
    @Test fun `eqAny subquery - this null`() = assertNull(nullExpr eqAny subQuery)
    @Test fun `eqAny subquery - right null`() = assertNull(status eqAny nullSubQuery)
    @Test fun `eqAny subquery - both null`() = assertNull(nullExpr eqAny nullSubQuery)

    // neAll(Collection)
    @Test fun `neAll collection - both non-null`() = assertNotNull(status neAll collectionExpr)
    @Test fun `neAll collection - this null`() = assertNull(nullExpr neAll collectionExpr)
    @Test fun `neAll collection - right null`() = assertNull(status neAll nullCollectionExpr)
    @Test fun `neAll collection - both null`() = assertNull(nullExpr neAll nullCollectionExpr)

    // neAny(Collection)
    @Test fun `neAny collection - both non-null`() = assertNotNull(status neAny collectionExpr)
    @Test fun `neAny collection - this null`() = assertNull(nullExpr neAny collectionExpr)
    @Test fun `neAny collection - right null`() = assertNull(status neAny nullCollectionExpr)
    @Test fun `neAny collection - both null`() = assertNull(nullExpr neAny nullCollectionExpr)
}
