package com.querydsl.ktx.extensions

import com.querydsl.core.types.dsl.ComparableExpression
import com.querydsl.core.types.dsl.Expressions
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComparableExpressionExtensionsTest : ComparableExpressionExtensions {

    private val code: ComparableExpression<String> = Expressions.comparablePath(String::class.java, "code")
    private val otherCode: ComparableExpression<String> = Expressions.comparablePath(String::class.java, "otherCode")
    private val nullExpr: ComparableExpression<String>? = null

    // ── gt(T?) ──

    @Test
    fun `gt value - both non-null returns GT expression`() {
        val result = code gt "A"
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `gt value - this null returns null`() {
        val result = nullExpr gt "A"
        assertNull(result)
    }

    @Test
    fun `gt value - right null returns null`() {
        val result = code gt (null as String?)
        assertNull(result)
    }

    @Test
    fun `gt value - both null returns null`() {
        val result = nullExpr gt (null as String?)
        assertNull(result)
    }

    // ── gt(Expression) ──

    @Test
    fun `gt expression - both non-null returns GT expression`() {
        val result = code gt otherCode
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `gt expression - this null returns null`() {
        val result = nullExpr gt otherCode
        assertNull(result)
    }

    @Test
    fun `gt expression - right null returns null`() {
        val result = code gt (null as ComparableExpression<String>?)
        assertNull(result)
    }

    @Test
    fun `gt expression - both null returns null`() {
        val result = nullExpr gt (null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── goe(T?) ──

    @Test
    fun `goe value - both non-null returns GOE expression`() {
        val result = code goe "A"
        assertNotNull(result)
        assertTrue(result.toString().contains(">="))
    }

    @Test
    fun `goe value - this null returns null`() {
        val result = nullExpr goe "A"
        assertNull(result)
    }

    @Test
    fun `goe value - right null returns null`() {
        val result = code goe (null as String?)
        assertNull(result)
    }

    @Test
    fun `goe value - both null returns null`() {
        val result = nullExpr goe (null as String?)
        assertNull(result)
    }

    // ── goe(Expression) ──

    @Test
    fun `goe expression - both non-null returns GOE expression`() {
        val result = code goe otherCode
        assertNotNull(result)
        assertTrue(result.toString().contains(">="))
    }

    @Test
    fun `goe expression - this null returns null`() {
        val result = nullExpr goe otherCode
        assertNull(result)
    }

    @Test
    fun `goe expression - right null returns null`() {
        val result = code goe (null as ComparableExpression<String>?)
        assertNull(result)
    }

    @Test
    fun `goe expression - both null returns null`() {
        val result = nullExpr goe (null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── lt(T?) ──

    @Test
    fun `lt value - both non-null returns LT expression`() {
        val result = code lt "Z"
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `lt value - this null returns null`() {
        val result = nullExpr lt "Z"
        assertNull(result)
    }

    @Test
    fun `lt value - right null returns null`() {
        val result = code lt (null as String?)
        assertNull(result)
    }

    @Test
    fun `lt value - both null returns null`() {
        val result = nullExpr lt (null as String?)
        assertNull(result)
    }

    // ── lt(Expression) ──

    @Test
    fun `lt expression - both non-null returns LT expression`() {
        val result = code lt otherCode
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `lt expression - this null returns null`() {
        val result = nullExpr lt otherCode
        assertNull(result)
    }

    @Test
    fun `lt expression - right null returns null`() {
        val result = code lt (null as ComparableExpression<String>?)
        assertNull(result)
    }

    @Test
    fun `lt expression - both null returns null`() {
        val result = nullExpr lt (null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── loe(T?) ──

    @Test
    fun `loe value - both non-null returns LOE expression`() {
        val result = code loe "Z"
        assertNotNull(result)
        assertTrue(result.toString().contains("<="))
    }

    @Test
    fun `loe value - this null returns null`() {
        val result = nullExpr loe "Z"
        assertNull(result)
    }

    @Test
    fun `loe value - right null returns null`() {
        val result = code loe (null as String?)
        assertNull(result)
    }

    @Test
    fun `loe value - both null returns null`() {
        val result = nullExpr loe (null as String?)
        assertNull(result)
    }

    // ── loe(Expression) ──

    @Test
    fun `loe expression - both non-null returns LOE expression`() {
        val result = code loe otherCode
        assertNotNull(result)
        assertTrue(result.toString().contains("<="))
    }

    @Test
    fun `loe expression - this null returns null`() {
        val result = nullExpr loe otherCode
        assertNull(result)
    }

    @Test
    fun `loe expression - right null returns null`() {
        val result = code loe (null as ComparableExpression<String>?)
        assertNull(result)
    }

    @Test
    fun `loe expression - both null returns null`() {
        val result = nullExpr loe (null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── between(Pair) ──

    @Test
    fun `between Pair - this non-null, both from and to non-null returns BETWEEN`() {
        val result = code between ("A" to "Z")
        assertNotNull(result)
        assert(result.toString().contains("between"))
    }

    @Test
    fun `between Pair - this non-null, from only returns GOE`() {
        val result = code between ("A" to null)
        assertNotNull(result)
    }

    @Test
    fun `between Pair - this non-null, to only returns LOE`() {
        val result = code between (null to "Z")
        assertNotNull(result)
    }

    @Test
    fun `between Pair - this non-null, both from and to null returns null`() {
        val result = code between (null to null)
        assertNull(result)
    }

    @Test
    fun `between Pair - this null returns null regardless of range`() {
        val result = nullExpr between ("A" to "Z")
        assertNull(result)
    }

    // ── between(ClosedRange) ──

    @Test
    fun `between ClosedRange - this non-null returns BETWEEN`() {
        val result = code between ("A".."Z")
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("between"))
    }

    @Test
    fun `between ClosedRange - this null returns null`() {
        val result = nullExpr between ("A".."Z")
        assertNull(result)
    }

    // ── notBetween ──

    @Test
    fun `notBetween - this non-null returns NOT BETWEEN`() {
        val result = code notBetween ("A" to "Z")
        assertNotNull(result)
        val str = result.toString().lowercase()
        assertTrue(str.contains("not") || str.contains("!"), "Expected NOT BETWEEN expression but got: $result")
    }

    @Test
    fun `notBetween - this null returns null`() {
        val result = nullExpr notBetween ("A" to "Z")
        assertNull(result)
    }

    @Test
    fun `notBetween - only from returns less-than`() {
        val result = code notBetween ("A" to null)
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `notBetween - only to returns greater-than`() {
        val result = code notBetween (null to "Z")
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `notBetween - both bounds null returns null`() {
        val result = code notBetween (null to null)
        assertNull(result)
    }

    // ── nullif(Expression) ──

    @Test
    fun `nullif expression - both non-null returns NULLIF`() {
        val result = code nullif otherCode
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("nullif"))
    }

    @Test
    fun `nullif expression - this null returns null`() {
        val result = nullExpr nullif otherCode
        assertNull(result)
    }

    @Test
    fun `nullif expression - other null returns null`() {
        val result = code nullif (null as ComparableExpression<String>?)
        assertNull(result)
    }

    @Test
    fun `nullif expression - both null returns null`() {
        val result = nullExpr nullif (null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── nullif(T?) ──

    @Test
    fun `nullif value - both non-null returns NULLIF`() {
        val result = code nullif "DEFAULT"
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("nullif"))
    }

    @Test
    fun `nullif value - this null returns null`() {
        val result = nullExpr nullif "DEFAULT"
        assertNull(result)
    }

    @Test
    fun `nullif value - other null returns null`() {
        val result = code nullif (null as String?)
        assertNull(result)
    }

    @Test
    fun `nullif value - both null returns null`() {
        val result = nullExpr nullif (null as String?)
        assertNull(result)
    }

    // ── coalesce(Expression) ──

    @Test
    fun `coalesce expression - both non-null returns COALESCE`() {
        val result = code coalesce otherCode
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("coalesce"))
    }

    @Test
    fun `coalesce expression - this null returns null`() {
        val result = nullExpr coalesce otherCode
        assertNull(result)
    }

    @Test
    fun `coalesce expression - expr null returns null`() {
        val result = code coalesce (null as ComparableExpression<String>?)
        assertNull(result)
    }

    @Test
    fun `coalesce expression - both null returns null`() {
        val result = nullExpr coalesce (null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── coalesce(T?) ──

    @Test
    fun `coalesce value - both non-null returns COALESCE`() {
        val result = code coalesce "DEFAULT"
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("coalesce"))
    }

    @Test
    fun `coalesce value - this null returns null`() {
        val result = nullExpr coalesce "DEFAULT"
        assertNull(result)
    }

    @Test
    fun `coalesce value - arg null returns null`() {
        val result = code coalesce (null as String?)
        assertNull(result)
    }

    @Test
    fun `coalesce value - both null returns null`() {
        val result = nullExpr coalesce (null as String?)
        assertNull(result)
    }

    // ── reverse between (value between two expressions) ──

    @Test
    fun `reverse between - value non-null, both bounds non-null returns AND expression`() {
        val result = "M" between (code to otherCode)
        assertNotNull(result)
        assertTrue(result.toString().contains("&&"))
    }

    @Test
    fun `reverse between - value null returns null`() {
        val result = (null as String?) between (code to otherCode)
        assertNull(result)
    }

    @Test
    fun `reverse between - lower null returns GOE only`() {
        val result = "M" between (null as ComparableExpression<String>? to otherCode)
        assertNotNull(result)
    }

    @Test
    fun `reverse between - upper null returns LOE only`() {
        val result = "M" between (code to null as ComparableExpression<String>?)
        assertNotNull(result)
    }

    @Test
    fun `reverse between - both bounds null returns null`() {
        val result = "M" between (null as ComparableExpression<String>? to null as ComparableExpression<String>?)
        assertNull(result)
    }

    // ── rangeTo operator ──

    @Test
    fun `rangeTo creates Pair for reverse between`() {
        val result = "M" between (code..otherCode)
        assertNotNull(result)
    }
}
