package com.querydsl.ktx.extensions

import com.querydsl.core.DefaultQueryMetadata
import com.querydsl.core.types.CollectionExpression
import com.querydsl.core.types.SubQueryExpression
import com.querydsl.core.types.Visitor
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

    @Test
    fun `notBetween - only from returns less-than`() {
        val result = price notBetween (10000 to null)
        assertNotNull(result)
        assertTrue(result.toString().contains("<"))
    }

    @Test
    fun `notBetween - only to returns greater-than`() {
        val result = price notBetween (null to 50000)
        assertNotNull(result)
        assertTrue(result.toString().contains(">"))
    }

    @Test
    fun `notBetween - both bounds null returns null`() {
        val result = price notBetween (null to null)
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

    // ── add ──

    @Test
    fun `add value - both non-null returns ADD expression`() {
        val result = price add 1000
        assertNotNull(result)
    }

    @Test
    fun `add value - this null returns null`() {
        val result = nullExpr add 1000
        assertNull(result)
    }

    @Test
    fun `add value - right null returns null`() {
        val result = price add (null as Int?)
        assertNull(result)
    }

    @Test
    fun `add value - both null returns null`() {
        val result = nullExpr add (null as Int?)
        assertNull(result)
    }

    @Test
    fun `add expression - both non-null returns ADD expression`() {
        val result = price add minPrice
        assertNotNull(result)
    }

    @Test
    fun `add expression - this null returns null`() {
        val result = nullExpr add minPrice
        assertNull(result)
    }

    @Test
    fun `add expression - right null returns null`() {
        val result = price add (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `add expression - both null returns null`() {
        val result = nullExpr add (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── subtract ──

    @Test
    fun `subtract value - both non-null returns SUB expression`() {
        val result = price subtract 100
        assertNotNull(result)
    }

    @Test
    fun `subtract value - this null returns null`() {
        val result = nullExpr subtract 100
        assertNull(result)
    }

    @Test
    fun `subtract value - right null returns null`() {
        val result = price subtract (null as Int?)
        assertNull(result)
    }

    @Test
    fun `subtract value - both null returns null`() {
        val result = nullExpr subtract (null as Int?)
        assertNull(result)
    }

    @Test
    fun `subtract expression - both non-null returns SUB expression`() {
        val result = price subtract minPrice
        assertNotNull(result)
    }

    @Test
    fun `subtract expression - this null returns null`() {
        val result = nullExpr subtract minPrice
        assertNull(result)
    }

    @Test
    fun `subtract expression - right null returns null`() {
        val result = price subtract (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `subtract expression - both null returns null`() {
        val result = nullExpr subtract (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── multiply ──

    @Test
    fun `multiply value - both non-null returns MUL expression`() {
        val result = price multiply 2
        assertNotNull(result)
    }

    @Test
    fun `multiply value - this null returns null`() {
        val result = nullExpr multiply 2
        assertNull(result)
    }

    @Test
    fun `multiply value - right null returns null`() {
        val result = price multiply (null as Int?)
        assertNull(result)
    }

    @Test
    fun `multiply value - both null returns null`() {
        val result = nullExpr multiply (null as Int?)
        assertNull(result)
    }

    @Test
    fun `multiply expression - both non-null returns MUL expression`() {
        val result = price multiply minPrice
        assertNotNull(result)
    }

    @Test
    fun `multiply expression - this null returns null`() {
        val result = nullExpr multiply minPrice
        assertNull(result)
    }

    @Test
    fun `multiply expression - right null returns null`() {
        val result = price multiply (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `multiply expression - both null returns null`() {
        val result = nullExpr multiply (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── divide ──

    @Test
    fun `divide value - both non-null returns DIV expression`() {
        val result = price divide 2
        assertNotNull(result)
    }

    @Test
    fun `divide value - this null returns null`() {
        val result = nullExpr divide 2
        assertNull(result)
    }

    @Test
    fun `divide value - right null returns null`() {
        val result = price divide (null as Int?)
        assertNull(result)
    }

    @Test
    fun `divide value - both null returns null`() {
        val result = nullExpr divide (null as Int?)
        assertNull(result)
    }

    @Test
    fun `divide expression - both non-null returns DIV expression`() {
        val result = price divide minPrice
        assertNotNull(result)
    }

    @Test
    fun `divide expression - this null returns null`() {
        val result = nullExpr divide minPrice
        assertNull(result)
    }

    @Test
    fun `divide expression - right null returns null`() {
        val result = price divide (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `divide expression - both null returns null`() {
        val result = nullExpr divide (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── mod ──

    @Test
    fun `mod value - both non-null returns MOD expression`() {
        val result = price mod 10
        assertNotNull(result)
    }

    @Test
    fun `mod value - this null returns null`() {
        val result = nullExpr mod 10
        assertNull(result)
    }

    @Test
    fun `mod value - right null returns null`() {
        val result = price mod (null as Int?)
        assertNull(result)
    }

    @Test
    fun `mod value - both null returns null`() {
        val result = nullExpr mod (null as Int?)
        assertNull(result)
    }

    @Test
    fun `mod expression - both non-null returns MOD expression`() {
        val result = price mod minPrice
        assertNotNull(result)
    }

    @Test
    fun `mod expression - this null returns null`() {
        val result = nullExpr mod minPrice
        assertNull(result)
    }

    @Test
    fun `mod expression - right null returns null`() {
        val result = price mod (null as NumberExpression<Int>?)
        assertNull(result)
    }

    @Test
    fun `mod expression - both null returns null`() {
        val result = nullExpr mod (null as NumberExpression<Int>?)
        assertNull(result)
    }

    // ── Kotlin operator overloads (#105) ──

    @Test
    fun `plus operator - value`() {
        val result = price + 1000
        assertNotNull(result)
        assertTrue(result.toString().contains("+"))
    }

    @Test
    fun `plus operator - expression`() {
        val result = price + minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("+"))
    }

    @Test
    fun `minus operator - value`() {
        val result = price - 100
        assertNotNull(result)
        assertTrue(result.toString().contains("-"))
    }

    @Test
    fun `minus operator - expression`() {
        val result = price - minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("-"))
    }

    @Test
    fun `times operator - value`() {
        val result = price * 2
        assertNotNull(result)
        assertTrue(result.toString().contains("*"))
    }

    @Test
    fun `times operator - expression`() {
        val result = price * minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("*"))
    }

    @Test
    fun `div operator - value`() {
        val result = price / 2
        assertNotNull(result)
        assertTrue(result.toString().contains("/"))
    }

    @Test
    fun `div operator - expression`() {
        val result = price / minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("/"))
    }

    @Test
    fun `rem operator - value`() {
        val result = price % 10
        assertNotNull(result)
        assertTrue(result.toString().contains("%"))
    }

    @Test
    fun `rem operator - expression`() {
        val result = price % minPrice
        assertNotNull(result)
        assertTrue(result.toString().contains("%"))
    }

    @Test
    fun `unaryMinus operator`() {
        val result = -price
        assertNotNull(result)
    }

    @Test
    fun `arithmetic operators compose with parentheses`() {
        val result = (price + minPrice) * 2
        assertNotNull(result)
    }

    @Test
    fun `rangeTo still creates Pair when arithmetic operators in scope`() {
        // Ensures `..` still resolves to rangeTo (Pair builder), not interpreted otherwise.
        val result = 30000 between (price..minPrice)
        assertNotNull(result)
    }

    // ── ALL/Any variants (#90) ──

    private val collectionExpr: CollectionExpression<List<Int>, Int> = object : CollectionExpression<List<Int>, Int> {
        @Suppress("UNCHECKED_CAST")
        override fun getType(): Class<out List<Int>> = List::class.java as Class<out List<Int>>
        override fun <R, C> accept(v: Visitor<R, C>, context: C?): R = throw UnsupportedOperationException()
        override fun getParameter(index: Int): Class<*> = Int::class.javaObjectType
    }
    private val nullCollectionExpr: CollectionExpression<List<Int>, Int>? = null
    private val subQuery: SubQueryExpression<Int> = object : SubQueryExpression<Int> {
        override fun getMetadata() = DefaultQueryMetadata()
        override fun <R, C> accept(v: Visitor<R, C>, context: C?): R = throw UnsupportedOperationException()
        override fun getType(): Class<out Int> = Int::class.javaObjectType
    }
    private val nullSubQuery: SubQueryExpression<Int>? = null

    // gtAll(Collection)
    @Test fun `gtAll collection - both non-null`() = assertNotNull(price gtAll collectionExpr)
    @Test fun `gtAll collection - this null`() = assertNull(nullExpr gtAll collectionExpr)
    @Test fun `gtAll collection - right null`() = assertNull(price gtAll nullCollectionExpr)
    @Test fun `gtAll collection - both null`() = assertNull(nullExpr gtAll nullCollectionExpr)

    // gtAll(SubQuery)
    @Test fun `gtAll subquery - both non-null`() = assertNotNull(price gtAll subQuery)
    @Test fun `gtAll subquery - this null`() = assertNull(nullExpr gtAll subQuery)
    @Test fun `gtAll subquery - right null`() = assertNull(price gtAll nullSubQuery)
    @Test fun `gtAll subquery - both null`() = assertNull(nullExpr gtAll nullSubQuery)

    // gtAny(Collection)
    @Test fun `gtAny collection - both non-null`() = assertNotNull(price gtAny collectionExpr)
    @Test fun `gtAny collection - this null`() = assertNull(nullExpr gtAny collectionExpr)
    @Test fun `gtAny collection - right null`() = assertNull(price gtAny nullCollectionExpr)
    @Test fun `gtAny collection - both null`() = assertNull(nullExpr gtAny nullCollectionExpr)

    // gtAny(SubQuery)
    @Test fun `gtAny subquery - both non-null`() = assertNotNull(price gtAny subQuery)
    @Test fun `gtAny subquery - this null`() = assertNull(nullExpr gtAny subQuery)
    @Test fun `gtAny subquery - right null`() = assertNull(price gtAny nullSubQuery)
    @Test fun `gtAny subquery - both null`() = assertNull(nullExpr gtAny nullSubQuery)

    // goeAll(Collection) — SubQuery 변형은 QueryDSL 5.1.0 멤버 부재로 미러링 안 함
    @Test fun `goeAll collection - both non-null`() = assertNotNull(price goeAll collectionExpr)
    @Test fun `goeAll collection - this null`() = assertNull(nullExpr goeAll collectionExpr)
    @Test fun `goeAll collection - right null`() = assertNull(price goeAll nullCollectionExpr)
    @Test fun `goeAll collection - both null`() = assertNull(nullExpr goeAll nullCollectionExpr)

    // goeAny(Collection)
    @Test fun `goeAny collection - both non-null`() = assertNotNull(price goeAny collectionExpr)
    @Test fun `goeAny collection - this null`() = assertNull(nullExpr goeAny collectionExpr)
    @Test fun `goeAny collection - right null`() = assertNull(price goeAny nullCollectionExpr)
    @Test fun `goeAny collection - both null`() = assertNull(nullExpr goeAny nullCollectionExpr)

    // ltAll(Collection)
    @Test fun `ltAll collection - both non-null`() = assertNotNull(price ltAll collectionExpr)
    @Test fun `ltAll collection - this null`() = assertNull(nullExpr ltAll collectionExpr)
    @Test fun `ltAll collection - right null`() = assertNull(price ltAll nullCollectionExpr)
    @Test fun `ltAll collection - both null`() = assertNull(nullExpr ltAll nullCollectionExpr)

    // ltAny(Collection)
    @Test fun `ltAny collection - both non-null`() = assertNotNull(price ltAny collectionExpr)
    @Test fun `ltAny collection - this null`() = assertNull(nullExpr ltAny collectionExpr)
    @Test fun `ltAny collection - right null`() = assertNull(price ltAny nullCollectionExpr)
    @Test fun `ltAny collection - both null`() = assertNull(nullExpr ltAny nullCollectionExpr)

    // loeAll(Collection)
    @Test fun `loeAll collection - both non-null`() = assertNotNull(price loeAll collectionExpr)
    @Test fun `loeAll collection - this null`() = assertNull(nullExpr loeAll collectionExpr)
    @Test fun `loeAll collection - right null`() = assertNull(price loeAll nullCollectionExpr)
    @Test fun `loeAll collection - both null`() = assertNull(nullExpr loeAll nullCollectionExpr)

    // loeAny(Collection)
    @Test fun `loeAny collection - both non-null`() = assertNotNull(price loeAny collectionExpr)
    @Test fun `loeAny collection - this null`() = assertNull(nullExpr loeAny collectionExpr)
    @Test fun `loeAny collection - right null`() = assertNull(price loeAny nullCollectionExpr)
    @Test fun `loeAny collection - both null`() = assertNull(nullExpr loeAny nullCollectionExpr)
}
