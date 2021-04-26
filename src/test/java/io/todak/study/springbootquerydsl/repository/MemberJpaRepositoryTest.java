package io.todak.study.springbootquerydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.todak.study.springbootquerydsl.dto.MemberSearchCondition;
import io.todak.study.springbootquerydsl.dto.MemberTeamDto;
import io.todak.study.springbootquerydsl.entity.Member;
import io.todak.study.springbootquerydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(member).isEqualTo(findMember);

        List<Member> list1 = memberJpaRepository.findAllQueryDsl();
        assertThat(list1).containsExactly(member);

        List<Member> list2 = memberJpaRepository.findByUsernameQueryDsl("member1");
        assertThat(list2).containsExactly(member);
    }

    @Test
    public void searchTest() {
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member4");
    }



}