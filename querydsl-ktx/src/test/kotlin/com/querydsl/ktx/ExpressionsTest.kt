package com.querydsl.ktx

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpression
import com.querydsl.core.types.dsl.DateExpression
import com.querydsl.core.types.dsl.DateTimeExpression
import com.querydsl.core.types.dsl.EnumExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression
import com.querydsl.core.types.dsl.TimeExpression
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExpressionsTest {

    private val numberPath: NumberExpression<Int> = Expressions.numberPath(Int::class.javaObjectType, "num")

    // ── Template functions (reified) ──

    @Test
    fun `numberTemplate returns NumberExpression`() {
        val result = numberTemplate<Float>("cast(avg({0}) as float)", numberPath)
        assertNotNull(result)
        assertTrue(result is NumberExpression<*>)
    }

    @Test
    fun `comparableTemplate returns ComparableExpression`() {
        val result = comparableTemplate<String>("upper({0})", numberPath)
        assertNotNull(result)
        assertTrue(result is ComparableExpression<*>)
    }

    @Test
    fun `simpleTemplate returns SimpleExpression`() {
        val result = simpleTemplate<String>("custom({0})", numberPath)
        assertNotNull(result)
        assertTrue(result is SimpleExpression<*>)
    }

    @Test
    fun `template returns Expression`() {
        val result = template<String>("custom({0})", numberPath)
        assertNotNull(result)
    }

    @Test
    fun `dateTemplate returns DateExpression`() {
        val result = dateTemplate<java.sql.Date>("current_date()")
        assertNotNull(result)
        assertTrue(result is DateExpression<*>)
    }

    @Test
    fun `dateTimeTemplate returns DateTimeExpression`() {
        val result = dateTimeTemplate<java.sql.Timestamp>("current_timestamp()")
        assertNotNull(result)
        assertTrue(result is DateTimeExpression<*>)
    }

    @Test
    fun `timeTemplate returns TimeExpression`() {
        val result = timeTemplate<java.sql.Time>("current_time()")
        assertNotNull(result)
        assertTrue(result is TimeExpression<*>)
    }

    @Test
    fun `enumTemplate returns EnumExpression`() {
        val result = enumTemplate<TestEnum>("cast({0} as varchar)", numberPath)
        assertNotNull(result)
        assertTrue(result is EnumExpression<*>)
    }

    // ── Template functions (non-reified) ──

    @Test
    fun `stringTemplate returns StringExpression`() {
        val result = stringTemplate("concat({0}, '_suffix')", numberPath)
        assertNotNull(result)
        assertTrue(result is StringExpression)
    }

    @Test
    fun `booleanTemplate returns BooleanExpression`() {
        val result = booleanTemplate("{0} > 0", numberPath)
        assertNotNull(result)
        assertTrue(result is BooleanExpression)
    }

    // ── Value wrapping functions ──

    @Test
    fun `asNumber wraps value as NumberExpression`() {
        val result = asNumber(42)
        assertNotNull(result)
        assertTrue(result is NumberExpression<*>)
    }

    @Test
    fun `asString wraps value as StringExpression`() {
        val result = asString("hello")
        assertNotNull(result)
        assertTrue(result is StringExpression)
    }

    @Test
    fun `asBoolean wraps value as BooleanExpression`() {
        val result = asBoolean(true)
        assertNotNull(result)
        assertTrue(result is BooleanExpression)
    }

    @Test
    fun `asComparable wraps value as ComparableExpression`() {
        val result = asComparable("hello")
        assertNotNull(result)
        assertTrue(result is ComparableExpression<*>)
    }

    @Test
    fun `asDate wraps value as DateExpression`() {
        val result = asDate(java.sql.Date(0))
        assertNotNull(result)
        assertTrue(result is DateExpression<*>)
    }

    @Test
    fun `asDateTime wraps value as DateTimeExpression`() {
        val result = asDateTime(java.sql.Timestamp(0))
        assertNotNull(result)
        assertTrue(result is DateTimeExpression<*>)
    }

    @Test
    fun `asTime wraps value as TimeExpression`() {
        val result = asTime(java.sql.Time(0))
        assertNotNull(result)
        assertTrue(result is TimeExpression<*>)
    }

    @Test
    fun `asEnum wraps value as EnumExpression`() {
        val result = asEnum(TestEnum.A)
        assertNotNull(result)
        assertTrue(result is EnumExpression<*>)
    }

    // ── Constant ──

    @Test
    fun `constant wraps value as Expression`() {
        val result = constant("hello")
        assertNotNull(result)
    }

    @Test
    fun `constant with number`() {
        val result = constant(42)
        assertNotNull(result)
    }

    private enum class TestEnum { A, B }
}
