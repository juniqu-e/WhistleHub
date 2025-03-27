package com.ssafy.backend.member.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.*;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.member.model.common.MemberInfo;
import com.ssafy.backend.member.model.request.RequestFollowRequestDto;
import com.ssafy.backend.member.model.request.UpdateMemberRequestDto;
import com.ssafy.backend.member.model.request.UpdatePasswordRequestDto;
import com.ssafy.backend.member.model.request.UploadProfileImageRequestDto;
import com.ssafy.backend.member.model.response.MemberDetailResponseDto;
import com.ssafy.backend.mysql.entity.Follow;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.repository.FollowRepository;
import com.ssafy.backend.mysql.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;

    public MemberDetailResponseDto getMember(Integer memberId) {
        // 회원 정보 조회
        Member member = null;
        if (memberId == null) {
            member = authService.getMember();
        } else {
            member = memberRepository.findById(memberId)
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

    public void deleteMember() {
        // 회원 탈퇴
        Member member = authService.getMember();
        member.setEnabled(false);

        memberRepository.save(member);
    }

    @Transactional
    public String uploadImage(UploadProfileImageRequestDto uploadProfileImageRequestDto) {
        // todo: 프로필 이미지 업로드
        Member member = authService.getMember();

        // 멤버 정보 조회
        if (!member.getId().equals(uploadProfileImageRequestDto.getMemberId()))
            throw new NotPermittedException();

        MultipartFile multipartFile = uploadProfileImageRequestDto.getImage();
        String memberProfileImagePath = member.getProfileImage();
        String imageUrl = null;

        // 파일을 처음 업로드하는지, 기존에 업로드했는지 구분
        if (memberProfileImagePath == null) {
            // 처음 업로드하는 경우
            imageUrl = s3Service.uploadFile(multipartFile, S3Service.IMAGE);
            member.setProfileImage(imageUrl);
        }else{
            // 기존에 업로드한 경우
            imageUrl = s3Service.updateFile(memberProfileImagePath, multipartFile, S3Service.IMAGE);
            member.setProfileImage(imageUrl);
        }

        // 멤버 정보 업데이트
        memberRepository.save(member);

        return imageUrl;
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        // 비밀번호 변경
        Member member = authService.getMember();
        String oldPassword = updatePasswordRequestDto.getOldPassword();
        if (!passwordEncoder.matches(oldPassword, member.getPassword()))
            throw new InvalidOldPasswordException();

        // todo: 새로운 비밀번호 입력 검증 로직 추가 new, old가 같다거나, new의 형식이 맞지 않거나

        String newPassword = passwordEncoder.encode(updatePasswordRequestDto.getNewPassword());
        member.setPassword(newPassword);
        memberRepository.save(member);
    }

    public List<MemberInfo> searchMember(String query, PageRequest pageRequest) {
        // 회원 검색
        List<Member> memberList = memberRepository.findByNicknameContaining(query, pageRequest);
        List<MemberInfo> memberInfoList = new LinkedList<>();
        for (Member member : memberList) {
            MemberInfo memberInfo = MemberInfo.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .profileImg(member.getProfileImage())
                    .build();

            memberInfoList.add(memberInfo);
        }

        return memberInfoList;
    }

    public List<MemberInfo> getFollower(Integer memberId){
           
    }


    public void followMember(RequestFollowRequestDto requestFollowRequestDto) {
        Member member = authService.getMember();
        boolean followRequest = requestFollowRequestDto.getFollow();

        Member targetMember = memberRepository.findById(requestFollowRequestDto.getMemberId())
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", requestFollowRequestDto.getMemberId());
                    return new NotFoundMemberException();
                });

        if(followRequest){ // 팔로우 신청 요청인경우,
            if(followRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId()).isPresent()){
                log.warn("이미 팔로우 신청한 회원입니다. memberId : {}", requestFollowRequestDto.getMemberId());
                throw new DuplicateFollowRequestException();
            }
            Follow follow = new Follow();
            follow.setFromMember(member);
            follow.setToMember(targetMember);

            followRepository.save(follow);
        }else{ // 팔로우 취소 요청인 경우,
            if(followRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId()).isEmpty()){
                log.warn("팔로우 신청하지 않은 회원입니다. memberId : {}", requestFollowRequestDto.getMemberId());
                throw new DuplicateFollowRequestException();
            }

            followRepository.deleteByFromMemberIdAndToMemberId(member.getId(), targetMember.getId());
        }
    }
}
