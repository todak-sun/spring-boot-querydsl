package io.todak.study.springbootquerydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.todak.study.springbootquerydsl.entity.Member;
import io.todak.study.springbootquerydsl.entity.QMember;
import io.todak.study.springbootquerydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    @BeforeEach
    public void setUp() {
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
        JPAQueryFactory query = new JPAQueryFactory(em);

        QMember m = QMember.member;

        Member findMember = query.select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assert findMember != null;
        assertThat(findMember.getUsername()).isEqualTo("member1");


    }

}
