package com.querydsl.ktx.extensions

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NumberExpressionExtensionsTest : NumberExpressionExtensions {

    private val price: NumberExpression<Int> = Expressions.numberPath(Int::class.javaObjectType, "price")
    private val minPrice: NumberExpression<Int> = Expressions.numberPath(Int::class.javaObjectType, "minPrice")
    private val nullExpr: NumberExpression<Int>? = null

    // ── gt(T?) ──

    @Test
    fun `gt value - both non-null returns GT expression`() {
        val result = price gt 10000
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `gt value - this null returns null`() {
        val result = nullExpr gt 10000
        assertNull(result)
    }

    @Test
    fun `gt value - right null returns null`() {
        val result = price gt (null as Int?)
        assertNull(result)
    }

    @Test
    fun `gt value - both null returns null`() {
        val result = nullExpr gt (null as Int?)
        assertNull(result)
    }

    // ── gt(Expression) ──

    @Test
    fun `gt expression - both non-null returns GT expression`() {
        val result = price gt minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `gt expression - this null returns null`() {
        val result = nullExpr gt minPrice
        assertNull(result)
    }

    @Test
    fun `gt expression - right null returns null`() {
        val result = price gt (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `gt expression - both null returns null`() {
        val result = nullExpr gt (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── goe(T?) ──

    @Test
    fun `goe value - both non-null returns GOE expression`() {
        val result = price goe 10000
        assertNotNull(result)
        assertTrue(result.toString().contains(">="))
    }

    @Test
    fun `goe value - this null returns null`() {
        val result = nullExpr goe 10000
        assertNull(result)
    }

    @Test
    fun `goe value - right null returns null`() {
        val result = price goe (null as Int?)
        assertNull(result)
    }

    @Test
    fun `goe value - both null returns null`() {
        val result = nullExpr goe (null as Int?)
        assertNull(result)
    }

    // ── goe(Expression) ──

    @Test
    fun `goe expression - both non-null returns GOE expression`() {
        val result = price goe minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains(">="))
    }

    @Test
    fun `goe expression - this null returns null`() {
        val result = nullExpr goe minPrice
        assertNull(result)
    }

    @Test
    fun `goe expression - right null returns null`() {
        val result = price goe (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `goe expression - both null returns null`() {
        val result = nullExpr goe (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── lt(T?) ──

    @Test
    fun `lt value - both non-null returns LT expression`() {
        val result = price lt 50000
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `lt value - this null returns null`() {
        val result = nullExpr lt 50000
        assertNull(result)
    }

    @Test
    fun `lt value - right null returns null`() {
        val result = price lt (null as Int?)
        assertNull(result)
    }

    @Test
    fun `lt value - both null returns null`() {
        val result = nullExpr lt (null as Int?)
        assertNull(result)
    }

    // ── lt(Expression) ──

    @Test
    fun `lt expression - both non-null returns LT expression`() {
        val result = price lt minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `lt expression - this null returns null`() {
        val result = nullExpr lt minPrice
        assertNull(result)
    }

    @Test
    fun `lt expression - right null returns null`() {
        val result = price lt (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `lt expression - both null returns null`() {
        val result = nullExpr lt (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── loe(T?) ──

    @Test
    fun `loe value - both non-null returns LOE expression`() {
        val result = price loe 50000
        assertNotNull(result)
        assertTrue(result.toString().contains("<="))
    }

    @Test
    fun `loe value - this null returns null`() {
        val result = nullExpr loe 50000
        assertNull(result)
    }

    @Test
    fun `loe value - right null returns null`() {
        val result = price loe (null as Int?)
        assertNull(result)
    }

    @Test
    fun `loe value - both null returns null`() {
        val result = nullExpr loe (null as Int?)
        assertNull(result)
    }

    // ── loe(Expression) ──

    @Test
    fun `loe expression - both non-null returns LOE expression`() {
        val result = price loe minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("<="))
    }

    @Test
    fun `loe expression - this null returns null`() {
        val result = nullExpr loe minPrice
        assertNull(result)
    }

    @Test
    fun `loe expression - right null returns null`() {
        val result = price loe (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `loe expression - both null returns null`() {
        val result = nullExpr loe (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── between(Pair) ──

    @Test
    fun `between Pair - both from and to non-null returns BETWEEN`() {
        val result = price between (10000 to 50000)
        assertNotNull(result)
    }

    @Test
    fun `between Pair - from only returns GOE`() {
        val result = price between (10000 to null)
        assertNotNull(result)
    }

    @Test
    fun `between Pair - to only returns LOE`() {
        val result = price between (null to 50000)
        assertNotNull(result)
    }

    @Test
    fun `between Pair - both from and to null returns null`() {
        val result = price between (null to null)
        assertNull(result)
    }

    @Test
    fun `between Pair - this null returns null`() {
        val result = nullExpr between (10000 to 50000)
        assertNull(result)
    }

    // ── between(ClosedRange) ──

    @Test
    fun `between ClosedRange - this non-null returns BETWEEN`() {
        val result = price between (10000..50000)
        assertNotNull(result)
        assertTrue(result.toString().lowercase().contains("between"))
    }

    @Test
    fun `between ClosedRange - this null returns null`() {
        val result = nullExpr between (10000..50000)
        assertNull(result)
    }

    // ── notBetween ──

    @Test
    fun `notBetween - this non-null returns NOT BETWEEN`() {
        val result = price notBetween (10000 to 50000)
        assertNotNull(result)
        val str = result.toString().lowercase()
        assertTrue(str.contains("not") || str.contains("!"))
    }

    @Test
    fun `notBetween - this null returns null`() {
        val result = nullExpr notBetween (10000 to 50000)
        assertNull(result)
    }

    // ── nullif(Expression) ──

    @Test
    fun `nullif expression - both non-null returns NULLIF`() {
        val result = price nullif minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("nullif("))
    }

    @Test
    fun `nullif expression - this null returns null`() {
        val result = nullExpr nullif minPrice
        assertNull(result)
    }

    @Test
    fun `nullif expression - other null returns null`() {
        val result = price nullif (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `nullif expression - both null returns null`() {
        val result = nullExpr nullif (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── nullif(T?) ──

    @Test
    fun `nullif value - both non-null returns NULLIF`() {
        val result = price nullif 0
        assertNotNull(result)
        assertTrue(result.toString().contains("nullif("))
    }

    @Test
    fun `nullif value - this null returns null`() {
        val result = nullExpr nullif 0
        assertNull(result)
    }

    @Test
    fun `nullif value - other null returns null`() {
        val result = price nullif (null as Int?)
        assertNull(result)
    }

    @Test
    fun `nullif value - both null returns null`() {
        val result = nullExpr nullif (null as Int?)
        assertNull(result)
    }

    // ── coalesce(Expression) ──

    @Test
    fun `coalesce expression - both non-null returns COALESCE`() {
        val result = price coalesce minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("coalesce("))
    }

    @Test
    fun `coalesce expression - this null returns null`() {
        val result = nullExpr coalesce minPrice
        assertNull(result)
    }

    @Test
    fun `coalesce expression - expr null returns null`() {
        val result = price coalesce (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `coalesce expression - both null returns null`() {
        val result = nullExpr coalesce (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── coalesce(T?) ──

    @Test
    fun `coalesce value - both non-null returns COALESCE`() {
        val result = price coalesce 0
        assertNotNull(result)
        assertTrue(result.toString().contains("coalesce("))
    }

    @Test
    fun `coalesce value - this null returns null`() {
        val result = nullExpr coalesce 0
        assertNull(result)
    }

    @Test
    fun `coalesce value - arg null returns null`() {
        val result = price coalesce (null as Int?)
        assertNull(result)
    }

    @Test
    fun `coalesce value - both null returns null`() {
        val result = nullExpr coalesce (null as Int?)
        assertNull(result)
    }

    // ── reverse between (value between two expressions) ──

    @Test
    fun `reverse between - value non-null, both bounds non-null returns AND expression`() {
        val result = 30000 between (price to minPrice)
        assertNotNull(result)
        assertTrue(result.toString().contains("&&"))
    }

    @Test
    fun `reverse between - value null returns null`() {
        val result = (null as Int?) between (price to minPrice)
        assertNull(result)
    }

    @Test
    fun `reverse between - lower null returns GOE only`() {
        val result = 30000 between (null as NumberExpression<Int>? to minPrice)
        assertNotNull(result)
    }

    @Test
    fun `reverse between - upper null returns LOE only`() {
        val result = 30000 between (price to null as NumberExpression<Int>?)
        assertNotNull(result)
    }

    @Test
    fun `reverse between - both bounds null returns null`() {
        val result = 30000 between (null as NumberExpression<Int>? to null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── rangeTo operator ──

    @Test
    fun `rangeTo creates Pair for reverse between`() {
        val result = 30000 between (price..minPrice)
        assertNotNull(result)
    }
}
