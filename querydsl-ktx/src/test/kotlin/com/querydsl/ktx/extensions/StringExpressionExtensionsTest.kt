package com.querydsl.ktx.extensions

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.StringExpression
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StringExpressionExtensionsTest : StringExpressionExtensions {

    private val name: StringExpression = Expressions.stringPath("name")
    private val keyword: StringExpression = Expressions.stringPath("keyword")
    private val nullExpr: StringExpression? = null

    // ── contains(String?) ──

    @Test
    fun `contains string - both non-null returns LIKE expression`() {
        val result = name contains "hong"
        assertNotNull(result)
    }

    @Test
    fun `contains string - this null returns null`() {
        val result = nullExpr contains "hong"
        assertNull(result)
    }

    @Test
    fun `contains string - right null returns null`() {
        val result = name contains (null as String?)
        assertNull(result)
    }

    @Test
    fun `contains string - both null returns null`() {
        val result = nullExpr contains (null as String?)
        assertNull(result)
    }

    // ── contains(Expression) ──

    @Test
    fun `contains expression - both non-null returns LIKE expression`() {
        val result = name contains keyword
        assertNotNull(result)
    }

    @Test
    fun `contains expression - this null returns null`() {
        val result = nullExpr contains keyword
        assertNull(result)
    }

    @Test
    fun `contains expression - right null returns null`() {
        val result = name contains (null as StringExpression?)
        assertNull(result)
    }

    @Test
    fun `contains expression - both null returns null`() {
        val result = nullExpr contains (null as StringExpression?)
        assertNull(result)
    }

    // ── containsIgnoreCase ──

    @Test
    fun `containsIgnoreCase - both non-null returns expression`() {
        val result = name containsIgnoreCase "HONG"
        assertNotNull(result)
    }

    @Test
    fun `containsIgnoreCase - this null returns null`() {
        val result = nullExpr containsIgnoreCase "HONG"
        assertNull(result)
    }

    @Test
    fun `containsIgnoreCase - right null returns null`() {
        val result = name containsIgnoreCase (null as String?)
        assertNull(result)
    }

    @Test
    fun `containsIgnoreCase - both null returns null`() {
        val result = nullExpr containsIgnoreCase (null as String?)
        assertNull(result)
    }

    // ── startsWith ──

    @Test
    fun `startsWith - both non-null returns expression`() {
        val result = name startsWith "hong"
        assertNotNull(result)
    }

    @Test
    fun `startsWith - this null returns null`() {
        val result = nullExpr startsWith "hong"
        assertNull(result)
    }

    @Test
    fun `startsWith - right null returns null`() {
        val result = name startsWith (null as String?)
        assertNull(result)
    }

    @Test
    fun `startsWith - both null returns null`() {
        val result = nullExpr startsWith (null as String?)
        assertNull(result)
    }

    // ── startsWith(Expression) ──

    @Test
    fun `startsWith expression - both non-null returns expression`() {
        val result = name startsWith keyword
        assertNotNull(result)
    }

    @Test
    fun `startsWith expression - this null returns null`() {
        val result = nullExpr startsWith keyword
        assertNull(result)
    }

    @Test
    fun `startsWith expression - right null returns null`() {
        val result = name startsWith (null as StringExpression?)
        assertNull(result)
    }

    @Test
    fun `startsWith expression - both null returns null`() {
        val result = nullExpr startsWith (null as StringExpression?)
        assertNull(result)
    }

    // ── startsWithIgnoreCase ──

    @Test
    fun `startsWithIgnoreCase - both non-null returns expression`() {
        val result = name startsWithIgnoreCase "HONG"
        assertNotNull(result)
    }

    @Test
    fun `startsWithIgnoreCase - this null returns null`() {
        val result = nullExpr startsWithIgnoreCase "HONG"
        assertNull(result)
    }

    @Test
    fun `startsWithIgnoreCase - right null returns null`() {
        val result = name startsWithIgnoreCase (null as String?)
        assertNull(result)
    }

    @Test
    fun `startsWithIgnoreCase - both null returns null`() {
        val result = nullExpr startsWithIgnoreCase (null as String?)
        assertNull(result)
    }

    // ── endsWith ──

    @Test
    fun `endsWith - both non-null returns expression`() {
        val result = name endsWith "@gmail.com"
        assertNotNull(result)
    }

    @Test
    fun `endsWith - this null returns null`() {
        val result = nullExpr endsWith "@gmail.com"
        assertNull(result)
    }

    @Test
    fun `endsWith - right null returns null`() {
        val result = name endsWith (null as String?)
        assertNull(result)
    }

    @Test
    fun `endsWith - both null returns null`() {
        val result = nullExpr endsWith (null as String?)
        assertNull(result)
    }

    // ── endsWith(Expression) ──

    @Test
    fun `endsWith expression - both non-null returns expression`() {
        val result = name endsWith keyword
        assertNotNull(result)
    }

    @Test
    fun `endsWith expression - this null returns null`() {
        val result = nullExpr endsWith keyword
        assertNull(result)
    }

    @Test
    fun `endsWith expression - right null returns null`() {
        val result = name endsWith (null as StringExpression?)
        assertNull(result)
    }

    @Test
    fun `endsWith expression - both null returns null`() {
        val result = nullExpr endsWith (null as StringExpression?)
        assertNull(result)
    }

    // ── endsWithIgnoreCase ──

    @Test
    fun `endsWithIgnoreCase - both non-null returns expression`() {
        val result = name endsWithIgnoreCase "@GMAIL.COM"
        assertNotNull(result)
    }

    @Test
    fun `endsWithIgnoreCase - this null returns null`() {
        val result = nullExpr endsWithIgnoreCase "@GMAIL.COM"
        assertNull(result)
    }

    @Test
    fun `endsWithIgnoreCase - right null returns null`() {
        val result = name endsWithIgnoreCase (null as String?)
        assertNull(result)
    }

    @Test
    fun `endsWithIgnoreCase - both null returns null`() {
        val result = nullExpr endsWithIgnoreCase (null as String?)
        assertNull(result)
    }

    // ── equalsIgnoreCase ──

    @Test
    fun `equalsIgnoreCase - both non-null returns expression`() {
        val result = name equalsIgnoreCase "admin"
        assertNotNull(result)
    }

    @Test
    fun `equalsIgnoreCase - this null returns null`() {
        val result = nullExpr equalsIgnoreCase "admin"
        assertNull(result)
    }

    @Test
    fun `equalsIgnoreCase - right null returns null`() {
        val result = name equalsIgnoreCase (null as String?)
        assertNull(result)
    }

    @Test
    fun `equalsIgnoreCase - both null returns null`() {
        val result = nullExpr equalsIgnoreCase (null as String?)
        assertNull(result)
    }

    // ── notEqualsIgnoreCase ──

    @Test
    fun `notEqualsIgnoreCase - both non-null returns expression`() {
        val result = name notEqualsIgnoreCase "admin"
        assertNotNull(result)
    }

    @Test
    fun `notEqualsIgnoreCase - this null returns null`() {
        val result = nullExpr notEqualsIgnoreCase "admin"
        assertNull(result)
    }

    @Test
    fun `notEqualsIgnoreCase - right null returns null`() {
        val result = name notEqualsIgnoreCase (null as String?)
        assertNull(result)
    }

    @Test
    fun `notEqualsIgnoreCase - both null returns null`() {
        val result = nullExpr notEqualsIgnoreCase (null as String?)
        assertNull(result)
    }

    // ── like ──

    @Test
    fun `like - both non-null returns expression`() {
        val result = name like "hong%"
        assertNotNull(result)
    }

    @Test
    fun `like - this null returns null`() {
        val result = nullExpr like "hong%"
        assertNull(result)
    }

    @Test
    fun `like - right null returns null`() {
        val result = name like (null as String?)
        assertNull(result)
    }

    @Test
    fun `like - both null returns null`() {
        val result = nullExpr like (null as String?)
        assertNull(result)
    }

    // ── likeIgnoreCase ──

    @Test
    fun `likeIgnoreCase - both non-null returns expression`() {
        val result = name likeIgnoreCase "HONG%"
        assertNotNull(result)
    }

    @Test
    fun `likeIgnoreCase - this null returns null`() {
        val result = nullExpr likeIgnoreCase "HONG%"
        assertNull(result)
    }

    @Test
    fun `likeIgnoreCase - right null returns null`() {
        val result = name likeIgnoreCase (null as String?)
        assertNull(result)
    }

    @Test
    fun `likeIgnoreCase - both null returns null`() {
        val result = nullExpr likeIgnoreCase (null as String?)
        assertNull(result)
    }

    // ── notLike ──

    @Test
    fun `notLike - both non-null returns expression`() {
        val result = name notLike "test%"
        assertNotNull(result)
    }

    @Test
    fun `notLike - this null returns null`() {
        val result = nullExpr notLike "test%"
        assertNull(result)
    }

    @Test
    fun `notLike - right null returns null`() {
        val result = name notLike (null as String?)
        assertNull(result)
    }

    @Test
    fun `notLike - both null returns null`() {
        val result = nullExpr notLike (null as String?)
        assertNull(result)
    }

    // ── matches ──

    @Test
    fun `matches - both non-null returns expression`() {
        val result = name matches "^[a-z]+$"
        assertNotNull(result)
    }

    @Test
    fun `matches - this null returns null`() {
        val result = nullExpr matches "^[a-z]+$"
        assertNull(result)
    }

    @Test
    fun `matches - right null returns null`() {
        val result = name matches (null as String?)
        assertNull(result)
    }

    @Test
    fun `matches - both null returns null`() {
        val result = nullExpr matches (null as String?)
        assertNull(result)
    }

    // ── nullif(Expression) ──

    @Test
    fun `nullif expression - both non-null returns NULLIF`() {
        val result = name nullif keyword
        assertNotNull(result)
    }

    @Test
    fun `nullif expression - this null returns null`() {
        val result = nullExpr nullif keyword
        assertNull(result)
    }

    @Test
    fun `nullif expression - other null returns null`() {
        val result = name nullif (null as Expression<String>?)
        assertNull(result)
    }

    @Test
    fun `nullif expression - both null returns null`() {
        val result = nullExpr nullif (null as Expression<String>?)
        assertNull(result)
    }

    // ── nullif(String?) ──

    @Test
    fun `nullif value - both non-null returns NULLIF`() {
        val result = name nullif "default"
        assertNotNull(result)
    }

    @Test
    fun `nullif value - this null returns null`() {
        val result = nullExpr nullif "default"
        assertNull(result)
    }

    @Test
    fun `nullif value - other null returns null`() {
        val result = name nullif (null as String?)
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
        val result = name coalesce keyword
        assertNotNull(result)
    }

    @Test
    fun `coalesce expression - this null returns null`() {
        val result = nullExpr coalesce keyword
        assertNull(result)
    }

    @Test
    fun `coalesce expression - expr null returns null`() {
        val result = name coalesce (null as Expression<String>?)
        assertNull(result)
    }

    @Test
    fun `coalesce expression - both null returns null`() {
        val result = nullExpr coalesce (null as Expression<String>?)
        assertNull(result)
    }

    // ── coalesce(String?) ──

    @Test
    fun `coalesce value - both non-null returns COALESCE`() {
        val result = name coalesce "unknown"
        assertNotNull(result)
    }

    @Test
    fun `coalesce value - this null returns null`() {
        val result = nullExpr coalesce "unknown"
        assertNull(result)
    }

    @Test
    fun `coalesce value - arg null returns null`() {
        val result = name coalesce (null as String?)
        assertNull(result)
    }

    @Test
    fun `coalesce value - both null returns null`() {
        val result = nullExpr coalesce (null as String?)
        assertNull(result)
    }
}
