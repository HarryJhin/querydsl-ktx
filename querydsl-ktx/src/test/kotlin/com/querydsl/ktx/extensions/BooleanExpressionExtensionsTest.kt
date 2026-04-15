package com.querydsl.ktx.extensions

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BooleanExpressionExtensionsTest : BooleanExpressionExtensions {

    private val active: BooleanExpression = Expressions.booleanPath("active")
    private val visible: BooleanExpression = Expressions.booleanPath("visible")
    private val deleted: BooleanExpression = Expressions.booleanPath("deleted")
    private val nullExpr: BooleanExpression? = null

    // ── and ──

    @Test
    fun `and - both non-null returns AND expression`() {
        val result = active and visible
        assertNotNull(result)
        assert(result.toString().contains("active && visible"))
    }

    @Test
    fun `and - this null, right non-null returns right`() {
        val result = nullExpr and visible
        assertNotNull(result)
        assert(result === visible)
    }

    @Test
    fun `and - this non-null, right null returns this`() {
        val result = active and nullExpr
        assertNotNull(result)
        assert(result === active)
    }

    @Test
    fun `and - both null returns null`() {
        val result = nullExpr and nullExpr
        assertNull(result)
    }

    // ── or ──

    @Test
    fun `or - both non-null returns OR expression`() {
        val result = active or visible
        assertNotNull(result)
        assert(result.toString().contains("active || visible"))
    }

    @Test
    fun `or - this null, right non-null returns right`() {
        val result = nullExpr or visible
        assertNotNull(result)
        assert(result === visible)
    }

    @Test
    fun `or - this non-null, right null returns this`() {
        val result = active or nullExpr
        assertNotNull(result)
        assert(result === active)
    }

    @Test
    fun `or - both null returns null`() {
        val result = nullExpr or nullExpr
        assertNull(result)
    }

    // ── andAnyOf ──

    @Test
    fun `andAnyOf - this non-null, predicates non-null returns AND(OR) expression`() {
        val result = active andAnyOf listOf(visible, deleted)
        assertNotNull(result)
        assertTrue(result.toString().contains("||"))
    }

    @Test
    fun `andAnyOf - this null, predicates non-null returns OR of predicates`() {
        val result = nullExpr andAnyOf listOf(visible, deleted)
        assertNotNull(result)
    }

    @Test
    fun `andAnyOf - this non-null, predicates all null returns this`() {
        val result = active andAnyOf listOf(null, null)
        assertNotNull(result)
        assert(result === active)
    }

    @Test
    fun `andAnyOf - both null returns null`() {
        val result = nullExpr andAnyOf listOf(null, null)
        assertNull(result)
    }

    @Test
    fun `andAnyOf - empty predicates list returns this`() {
        val result = active andAnyOf emptyList()
        assertNotNull(result)
        assert(result === active)
    }

    // ── orAllOf ──

    @Test
    fun `orAllOf - this non-null, predicates non-null returns OR(AND) expression`() {
        val result = active orAllOf listOf(visible, deleted)
        assertNotNull(result)
        assertTrue(result.toString().contains("&&"))
    }

    @Test
    fun `orAllOf - this null, predicates non-null returns AND of predicates`() {
        val result = nullExpr orAllOf listOf(visible, deleted)
        assertNotNull(result)
    }

    @Test
    fun `orAllOf - this non-null, predicates all null returns this`() {
        val result = active orAllOf listOf(null, null)
        assertNotNull(result)
        assert(result === active)
    }

    @Test
    fun `orAllOf - both null returns null`() {
        val result = nullExpr orAllOf listOf(null, null)
        assertNull(result)
    }

    @Test
    fun `orAllOf - empty predicates list returns this`() {
        val result = active orAllOf emptyList()
        assertNotNull(result)
        assert(result === active)
    }

    // ── eq ──

    @Test
    fun `eq - both non-null returns EQ expression`() {
        val result = active eq true
        assertNotNull(result)
        assertTrue(result.toString().contains("="))
    }

    @Test
    fun `eq - this null returns null`() {
        val result = nullExpr eq true
        assertNull(result)
    }

    @Test
    fun `eq - right null returns null`() {
        val result = active eq null
        assertNull(result)
    }

    @Test
    fun `eq - both null returns null`() {
        val result = nullExpr eq null
        assertNull(result)
    }

    // ── nullif(Expression) ──

    @Test
    fun `nullif Expression - both non-null returns NULLIF expression`() {
        val result = active nullif visible
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("nullif"))
    }

    @Test
    fun `nullif Expression - this null returns null`() {
        val result = nullExpr nullif visible
        assertNull(result)
    }

    @Test
    fun `nullif Expression - other null returns null`() {
        val result = active nullif (null as BooleanExpression?)
        assertNull(result)
    }

    @Test
    fun `nullif Expression - both null returns null`() {
        val result = nullExpr nullif (null as BooleanExpression?)
        assertNull(result)
    }

    // ── nullif(Boolean) ──

    @Test
    fun `nullif Boolean - both non-null returns NULLIF expression`() {
        val result = active nullif true
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("nullif"))
    }

    @Test
    fun `nullif Boolean - this null returns null`() {
        val result = nullExpr nullif true
        assertNull(result)
    }

    @Test
    fun `nullif Boolean - other null returns null`() {
        val result = active nullif (null as Boolean?)
        assertNull(result)
    }

    @Test
    fun `nullif Boolean - both null returns null`() {
        val result = nullExpr nullif (null as Boolean?)
        assertNull(result)
    }

    // ── coalesce(Expression) ──

    @Test
    fun `coalesce Expression - both non-null returns COALESCE expression`() {
        val result = active coalesce visible
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("coalesce"))
    }

    @Test
    fun `coalesce Expression - this null returns null`() {
        val result = nullExpr coalesce visible
        assertNull(result)
    }

    @Test
    fun `coalesce Expression - expr null returns null`() {
        val result = active coalesce (null as BooleanExpression?)
        assertNull(result)
    }

    @Test
    fun `coalesce Expression - both null returns null`() {
        val result = nullExpr coalesce (null as BooleanExpression?)
        assertNull(result)
    }

    // ── coalesce(Boolean) ──

    @Test
    fun `coalesce Boolean - both non-null returns COALESCE expression`() {
        val result = active coalesce true
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("coalesce"))
    }

    @Test
    fun `coalesce Boolean - this null returns null`() {
        val result = nullExpr coalesce true
        assertNull(result)
    }

    @Test
    fun `coalesce Boolean - arg null returns null`() {
        val result = active coalesce (null as Boolean?)
        assertNull(result)
    }

    @Test
    fun `coalesce Boolean - both null returns null`() {
        val result = nullExpr coalesce (null as Boolean?)
        assertNull(result)
    }
}
