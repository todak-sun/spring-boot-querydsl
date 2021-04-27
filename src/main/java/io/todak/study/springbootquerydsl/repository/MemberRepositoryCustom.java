package io.todak.study.springbootquerydsl.repository;

import io.todak.study.springbootquerydsl.dto.MemberSearchCondition;
import io.todak.study.springbootquerydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
List<MemberTeamDto> search(MemberSearchCondition condition);
}
