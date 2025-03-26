package com.ssafy.backend.member.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.InvalidOldPasswordException;
import com.ssafy.backend.common.error.exception.NotFoundMemberException;
import com.ssafy.backend.member.model.request.UpdateMemberRequestDto;
import com.ssafy.backend.member.model.request.UpdatePasswordRequestDto;
import com.ssafy.backend.member.model.response.MemberDetailResponseDto;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public MemberDetailResponseDto getMember(Integer memberId) {
        // 회원 정보 조회
         Member member = null;
         if (memberId == null) {
            member = authService.getMember();
         }else {
             memberRepository.findById(memberId)
                     .orElseThrow(() -> {
                         log.warn("해당하는 회원이 없습니다. memberId : {}", memberId);
                         return new NotFoundMemberException();
                     });
         }

            return MemberDetailResponseDto.builder()
                    .nickname(member.getNickname())
                    .profileImage(member.getProfileImage())
                    .profileText(member.getProfileText())
                    .build();
    }

    @Transactional
    public void updateMember(UpdateMemberRequestDto updateMemberRequestDto) {
        // 회원 정보 수정
        Member member = authService.getMember();
        member.setNickname(updateMemberRequestDto.getNickname());
        member.setProfileText(updateMemberRequestDto.getProfileText());
        memberRepository.save(member);
    }

    public void deleteMember(){
        // 회원 탈퇴
        Member member = authService.getMember();
        member.setEnabled(false);

        memberRepository.save(member);
    }

    public void uploadImage() {
        // todo: 프로필 이미지 업로드
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        // 비밀번호 변경
        Member member = authService.getMember();
        String oldPassword = passwordEncoder.encode(updatePasswordRequestDto.getOldPassword());
        if(!member.getPassword().equals(oldPassword))
            throw new InvalidOldPasswordException();

        // todo: 새로운 비밀번호 입력 검증 로직 추가

        String newPassword = passwordEncoder.encode(updatePasswordRequestDto.getNewPassword());
        member.setPassword(newPassword);
        memberRepository.save(member);
    }

    
}
