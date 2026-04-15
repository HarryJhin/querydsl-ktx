package com.querydsl.ktx

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.StringExpression
import com.querydsl.ktx.extensions.SimpleExpressionExtensions
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CaseDslTest : SimpleExpressionExtensions {

    private val status: StringExpression = Expressions.stringPath("status")
    private val name: StringExpression = Expressions.stringPath("name")
    private val active: BooleanExpression = Expressions.booleanPath("active")

    // ── Searched CASE ──

    @Test
    fun `searched case - single when with otherwise`() {
        val result = case<Int> {
            `when`(status.eq("VIP")) then 1
            otherwise(0)
        }
        assertNotNull(result)
    }

    @Test
    fun `searched case - multiple when branches`() {
        val result = case<Int> {
            `when`(status.eq("VIP")) then 1
            `when`(status.eq("NORMAL")) then 2
            otherwise(0)
        }
        assertNotNull(result)
    }

    @Test
    fun `searched case - null predicate skips branch`() {
        val nullPred = status eq (null as String?)  // returns null BooleanExpression
        val result = case<Int> {
            `when`(nullPred) then 1
            `when`(status.eq("NORMAL")) then 2
            otherwise(0)
        }
        assertNotNull(result)
    }

    @Test
    fun `searched case - all predicates null returns null`() {
        val nullPred = status eq (null as String?)
        val result = case<Int> {
            `when`(nullPred) then 1
            otherwise(0)
        }
        assertNull(result)
    }

    @Test
    fun `searched case - expression result`() {
        val result = case<String> {
            `when`(status.eq("VIP")) then name
            otherwise(Expressions.constant("unknown"))
        }
        assertNotNull(result)
    }

    // ── Simple CASE ──

    @Test
    fun `simple case - single when with otherwise`() {
        val result = case<String, Int>(status) {
            `when`("VIP") then 1
            otherwise(0)
        }
        assertNotNull(result)
    }

    @Test
    fun `simple case - multiple when branches`() {
        val result = case<String, Int>(status) {
            `when`("VIP") then 1
            `when`("NORMAL") then 2
            otherwise(0)
        }
        assertNotNull(result)
    }

    @Test
    fun `searched case - no otherwise called returns null`() {
        val result = case<String> {
            `when`(active) then "yes"
            // no otherwise
        }
        assertNull(result)
    }

    @Test
    fun `searched case - otherwise with expression`() {
        val result = case<String> {
            `when`(active) then "active"
            otherwise(Expressions.constant("default"))
        }
        assertNotNull(result)
    }

    @Test
    fun `simple case - otherwise with expression`() {
        val result = case<String, String>(status) {
            `when`("VIP") then "premium"
            otherwise(Expressions.constant("standard"))
        }
        assertNotNull(result)
    }
}
