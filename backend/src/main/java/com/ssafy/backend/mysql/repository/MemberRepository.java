package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

}
