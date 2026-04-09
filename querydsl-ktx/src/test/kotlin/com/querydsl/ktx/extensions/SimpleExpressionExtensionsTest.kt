package com.querydsl.ktx.extensions

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SimpleExpressionExtensionsTest : SimpleExpressionExtensions {

    private val status: StringExpression = Expressions.stringPath("status")
    private val defaultStatus: StringExpression = Expressions.stringPath("defaultStatus")
    private val nullExpr: SimpleExpression<String>? = null

    // ── eq(T?) ──

    @Test
    fun `eq value - both non-null returns EQ expression`() {
        val result = status eq "ACTIVE"
        assertNotNull(result)
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
}
