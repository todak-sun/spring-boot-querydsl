package io.todak.study.springbootquerydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.todak.study.springbootquerydsl.entity.Hello;
import io.todak.study.springbootquerydsl.entity.QHello;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class SpringBootQuerydslApplicationTests {

    @Autowired
    EntityManager em;

    @Test
    void contextLoads() {

        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory query = new JPAQueryFactory(em);
        QHello qHello = QHello.hello;

        Hello result = query.selectFrom(qHello)
                .fetchOne();
        
        //TODO : H2 데이터베이스 설치부터 다시 듣기
        
        assertEquals(hello.getId(), result.getId());
        assertEquals(hello, result);
    }

}
