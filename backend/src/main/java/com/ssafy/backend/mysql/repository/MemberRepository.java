package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    Member findByLoginId(String loginId);

    List<Member> findByNicknameContaining(String nickname, PageRequest pageRequest);
}
