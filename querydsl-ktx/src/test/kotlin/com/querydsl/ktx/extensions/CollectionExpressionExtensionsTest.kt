package com.querydsl.ktx.extensions

import com.querydsl.core.types.PathMetadataFactory
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.ListPath
import com.querydsl.core.types.dsl.StringPath
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CollectionExpressionExtensionsTest : CollectionExpressionExtensions {

    @Suppress("UNCHECKED_CAST")
    private val roles = Expressions.listPath(
        String::class.java,
        StringPath::class.java,
        PathMetadataFactory.forVariable("roles"),
    ) as ListPath<String, StringPath>
    private val nullExpr: ListPath<String, StringPath>? = null

    private val childExpr = Expressions.stringPath("defaultRole")

    // ── contains(E?) ──

    @Test
    fun `contains value - both non-null returns expression`() {
        val result = roles contains "ADMIN"
        assertNotNull(result)
    }

    @Test
    fun `contains value - this null returns null`() {
        val result = nullExpr contains "ADMIN"
        assertNull(result)
    }

    @Test
    fun `contains value - child null returns null`() {
        val result = roles contains (null as String?)
        assertNull(result)
    }

    @Test
    fun `contains value - both null returns null`() {
        val result = nullExpr contains (null as String?)
        assertNull(result)
    }

    // ── contains(Expression) ──

    @Test
    fun `contains expression - both non-null returns expression`() {
        val result = roles contains childExpr
        assertNotNull(result)
    }

    @Test
    fun `contains expression - this null returns null`() {
        val result = nullExpr contains childExpr
        assertNull(result)
    }

    @Test
    fun `contains expression - child null returns null`() {
        val result = roles contains (null as StringPath?)
        assertNull(result)
    }

    @Test
    fun `contains expression - both null returns null`() {
        val result = nullExpr contains (null as StringPath?)
        assertNull(result)
    }
}
