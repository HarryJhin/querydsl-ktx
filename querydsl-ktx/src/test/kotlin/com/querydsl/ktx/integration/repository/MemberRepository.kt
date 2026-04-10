package com.querydsl.ktx.integration.repository

import com.querydsl.ktx.integration.domain.Member
import com.querydsl.ktx.integration.domain.MemberStatus
import com.querydsl.ktx.integration.domain.QDepartment
import com.querydsl.ktx.integration.domain.QMember
import com.querydsl.ktx.integration.domain.QOrder
import com.querydsl.ktx.support.QuerydslRepository
import com.querydsl.ktx.support.sortSpec
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository

@Repository
class MemberRepository : QuerydslRepository<Member>() {
    private val member = QMember.member
    private val department = QDepartment.department

    fun findByCondition(
        name: String? = null,
        status: MemberStatus? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        departmentName: String? = null,
    ): List<Member> =
        selectFrom(member)
            .leftJoin(member.department, department)
            .where(
                member.name contains name,
                member.status eq status,
                member.age between (minAge to maxAge),
                department.name eq departmentName,
            )
            .fetch()

    fun findByNameStartsWith(prefix: String? = null): List<Member> =
        selectFrom(member).where(member.name startsWith prefix).fetch()

    fun findByNameEndsWith(suffix: String? = null): List<Member> =
        selectFrom(member).where(member.name endsWith suffix).fetch()

    fun findByNameLike(pattern: String? = null): List<Member> =
        selectFrom(member).where(member.name like pattern).fetch()

    fun findByNameContainsIgnoreCase(keyword: String? = null): List<Member> =
        selectFrom(member).where(member.name containsIgnoreCase keyword).fetch()

    fun findByStatusNe(status: MemberStatus? = null): List<Member> =
        selectFrom(member).where(member.status ne status).fetch()

    fun findByStatusIn(statuses: Collection<MemberStatus>? = null): List<Member> =
        selectFrom(member).where(member.status `in` statuses).fetch()

    fun findByStatusNotIn(statuses: Collection<MemberStatus>? = null): List<Member> =
        selectFrom(member).where(member.status notIn statuses).fetch()

    fun findByAgeBetweenClosedRange(range: ClosedRange<Int>): List<Member> =
        selectFrom(member).where(member.age between range).fetch()

    private val memberSort = sortSpec {
        "name" by member.name
        "age" by member.age
        "createdAt" by member.createdAt
    }

    fun findPage(pageable: Pageable): Page<Member> =
        selectFrom(member).page(pageable, memberSort)

    fun findSlice(pageable: Pageable): Slice<Member> =
        selectFrom(member).slice(pageable, memberSort)

    fun findPageWithCountQuery(pageable: Pageable, status: MemberStatus? = null): Page<Member> {
        val countQuery: () -> Long? = {
            select(member.count())
                .from(member)
                .where(member.status eq status)
                .fetchOne()
        }
        return selectFrom(member)
            .where(member.status eq status)
            .page(pageable, memberSort, countQuery = countQuery)
    }

    fun findMembersWithOrders(): List<Member> =
        selectFrom(member)
            .where(QOrder.order.exists(QOrder.order.member.eq(member)))
            .fetch()

    fun findMembersWithoutOrders(): List<Member> =
        selectFrom(member)
            .where(QOrder.order.notExists(QOrder.order.member.eq(member)))
            .fetch()

    fun deactivateByStatus(status: MemberStatus): Long =
        modifying {
            update(member)
                .set(member.status, MemberStatus.INACTIVE)
                .where(member.status.eq(status))
                .execute()
        }

    fun findByIds(ids: Collection<Long>): List<Member> =
        selectFrom(member)
            .where(member.id inChunked ids)
            .fetch()
}
