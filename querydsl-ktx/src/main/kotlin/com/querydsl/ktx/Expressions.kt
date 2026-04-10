@file:JvmName("ExpressionsKt")

package com.querydsl.ktx

import com.querydsl.core.types.Expression
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

// ── Template functions (reified) ──

inline fun <reified T> numberTemplate(
    template: String,
    vararg args: Any?,
): NumberExpression<T> where T : Number, T : Comparable<*> =
    Expressions.numberTemplate(T::class.java, template, *args)

inline fun <reified T : Comparable<*>> comparableTemplate(
    template: String,
    vararg args: Any?,
): ComparableExpression<T> =
    Expressions.comparableTemplate(T::class.java, template, *args)

inline fun <reified T> simpleTemplate(
    template: String,
    vararg args: Any?,
): SimpleExpression<T> =
    Expressions.simpleTemplate(T::class.java, template, *args)

inline fun <reified T> template(
    template: String,
    vararg args: Any?,
): Expression<T> =
    Expressions.template(T::class.java, template, *args)

inline fun <reified T : Comparable<*>> dateTemplate(
    template: String,
    vararg args: Any?,
): DateExpression<T> =
    Expressions.dateTemplate(T::class.java, template, *args)

inline fun <reified T : Comparable<*>> dateTimeTemplate(
    template: String,
    vararg args: Any?,
): DateTimeExpression<T> =
    Expressions.dateTimeTemplate(T::class.java, template, *args)

inline fun <reified T : Comparable<*>> timeTemplate(
    template: String,
    vararg args: Any?,
): TimeExpression<T> =
    Expressions.timeTemplate(T::class.java, template, *args)

inline fun <reified T : Enum<T>> enumTemplate(
    template: String,
    vararg args: Any?,
): EnumExpression<T> =
    Expressions.enumTemplate(T::class.java, template, *args)

// ── Template functions (non-reified, fixed type) ──

fun stringTemplate(template: String, vararg args: Any?): StringExpression =
    Expressions.stringTemplate(template, *args)

fun booleanTemplate(template: String, vararg args: Any?): BooleanExpression =
    Expressions.booleanTemplate(template, *args)

// ── Value wrapping (convenience delegates) ──

fun <T> asNumber(value: T): NumberExpression<T>
    where T : Number, T : Comparable<*> =
    Expressions.asNumber(value)

fun asString(value: String): StringExpression =
    Expressions.asString(value)

fun asBoolean(value: Boolean): BooleanExpression =
    Expressions.asBoolean(value)

fun <T : Comparable<*>> asComparable(value: T): ComparableExpression<T> =
    Expressions.asComparable(value)

fun <T : Comparable<*>> asDate(value: T): DateExpression<T> =
    Expressions.asDate(value)

fun <T : Comparable<*>> asDateTime(value: T): DateTimeExpression<T> =
    Expressions.asDateTime(value)

fun <T : Comparable<*>> asTime(value: T): TimeExpression<T> =
    Expressions.asTime(value)

fun <T : Enum<T>> asEnum(value: T): EnumExpression<T> =
    Expressions.asEnum(value)

// ── Constant ──

inline fun <reified T> constant(value: T): Expression<T> =
    Expressions.constant(value)
