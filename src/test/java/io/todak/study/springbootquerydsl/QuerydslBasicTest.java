package io.todak.study.springbootquerydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.todak.study.springbootquerydsl.dto.MemberDto;
import io.todak.study.springbootquerydsl.entity.Member;
import io.todak.study.springbootquerydsl.entity.QMember;
import io.todak.study.springbootquerydsl.entity.QTeam;
import io.todak.study.springbootquerydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static io.todak.study.springbootquerydsl.entity.QMember.member;
import static io.todak.study.springbootquerydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory query;

    @BeforeEach
    public void setUp() {
        query = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        Member findMember = em.createQuery(
                "select m " +
                        "from Member m " +
                        "where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        Member findMember = query
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assert findMember != null;
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assert findMember != null;
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assert findMember != null;
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {

//        List<Member> fetch = query
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = query.selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = query.selectFrom(member)
//                .fetchFirst();
//
//        QueryResults<Member> results = query.selectFrom(member)
//                .fetchResults();
//
//        List<Member> members = results.getResults();
//        results.getTotal();
//        results.getLimit();
//        results.getOffset();

        long count = query.selectFrom(member)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(),
                        member.username.asc()
                                .nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = query
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResults = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> fetch = query.select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
        ).from(member).fetch();

        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void group() {
        List<Tuple> fetch = query.select(team.name,
                member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> teamA = query.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA)
                .extracting("username")
                .containsExactly("member1", "member2");

    }

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인.
     */
    @Test
    public void join_on_filterling() {
        // select m, t from Member m left join m.team t on t.name = 'teamA'

        List<Tuple> result = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();


        for (Tuple t : result) {
            System.out.println("tuple = " + t);
        }
    }

    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = query.select(member, team)
                .from(member)
                .join(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple t : result) {
            System.out.println("tuple = " + t);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetch_join_no() {
        em.flush();
        em.clear();

        Member findMember = query
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        org.junit.jupiter.api.Assertions.assertFalse(loaded);
    }

    @Test
    public void feth_join_use() {
        em.flush();
        em.clear();

        Member findMember = query.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        org.junit.jupiter.api.Assertions.assertTrue(loaded);
    }

    /**
     * 나이가 가장 많은 회원을 조회
     */
    @Test
    public void sub_query() {

        QMember memberSub = new QMember("memberSub");

        List<Member> fetch = query.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions.select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(fetch).extracting("age").containsExactly(40);
    }

    /**
     * 나이가 평균 이상 회원을 조회
     */
    @Test
    public void sub_query_goe() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = query.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions.select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(30, 40);
    }

    @Test
    public void sub_query_in() {
        QMember memberSub = new QMember("memberSub");

//        List<Member> result = query.selectFrom(member)
//                .where(member.age.in(
//                        JPAExpressions.select(memberSub.age)
//                        .from(memberSub)
//                        .where(memberSub.age.gt(10))
//                )).fetch();

        List<Member> result = query.selectFrom(member)
                .where(member.age.in(
                        JPAExpressions.select(member.age)
                                .from(member)
                                .where(member.age.gt(10))
                )).fetch();

        assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    public void select_sub_query() {
        List<Tuple> fetch = query.select(
                member.username,
                JPAExpressions.select(member.age.avg())
                        .from(member)

        ).from(member).fetch();

        for (Tuple t : fetch) {
            System.out.println("tuple = " + t);
        }

    }


    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("select new io.todak.study.springbootquerydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }

    }

    @Test
    public void findDtoByQueryDslWithSetter() {
        List<MemberDto> fetch = query.select(Projections.bean(MemberDto.class,
                member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoByField() {
        List<MemberDto> result = query
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member).fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = query
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member).fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }


}
