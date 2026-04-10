package com.querydsl.ktx.integration

import com.querydsl.ktx.integration.QTestMember.testMember
import com.querydsl.ktx.support.QuerydslRepository
import org.springframework.stereotype.Repository

@Repository
class TestMemberRepository : QuerydslRepository<TestMember>() {

    fun findByCondition(
        name: String? = null,
        status: TestStatus? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
    ): List<TestMember> =
        selectFrom(testMember)
            .where(
                testMember.name eq name,
                testMember.status eq status,
                testMember.age between (minAge to maxAge),
            )
            .fetch()

    fun findByNameContains(keyword: String? = null): List<TestMember> =
        selectFrom(testMember)
            .where(testMember.name contains keyword)
            .fetch()
}
