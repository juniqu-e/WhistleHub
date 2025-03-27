package com.ssafy.backend.member.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.member.model.common.MemberInfo;
import com.ssafy.backend.member.model.request.UpdateMemberRequestDto;
import com.ssafy.backend.member.model.request.UpdatePasswordRequestDto;
import com.ssafy.backend.member.model.request.UploadProfileImageRequestDto;
import com.ssafy.backend.member.model.response.MemberDetailResponseDto;
import com.ssafy.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/member")
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    /**
     *  회원 정보 조회
     *
     * @param memberId 회원 아이디 없는 경우, 자기 자신의 정보 조회
     * @return 회원의 정보
     */
    @GetMapping("/")
    public ApiResponse<?> getMember(@RequestParam(value = "memberId", required = false) Integer memberId) {
        MemberDetailResponseDto result = memberService.getMember(memberId);

        return new ApiResponse.builder<Object>()
                .payload(result)
                .build();
    }

    /**
     *  회원 정보 수정
     *
     * @param updateMemberRequestDto nickname, profileText 수정할 정보
     */
    @PutMapping("/")
    public ApiResponse<?> updateMember(UpdateMemberRequestDto updateMemberRequestDto) {
        memberService.updateMember(updateMemberRequestDto);

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     *  회원 탈퇴
     *
     */
    @DeleteMapping("/")
    public ApiResponse<?> deleteMember() {
        memberService.deleteMember();

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 프로필 이미지 업로드
     * @see com.ssafy.backend.common.service.S3Service
     * @return 이미지 업로드된 링크
     */
    @PostMapping("/image")
    public ApiResponse<?> uploadImage(UploadProfileImageRequestDto uploadProfileImageRequestDto) {
        String result = memberService.uploadImage(uploadProfileImageRequestDto);

        return new ApiResponse.builder<Object>()
                .payload(result)
                .build();
    }

    /**
     * 비밀번호 변경
     *
     * @return 비밀번호 변경 결과
     */
    @PutMapping("/password")
    public ApiResponse<?> updatePassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        memberService.updatePassword(updatePasswordRequestDto);

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * 회원 검색
     *
     * @param query 검색어 -> 닉네임 검색
     * @param page  페이지 번호
     * @param size  페이지 사이즈
     * @return 검색 결과
     */
    @GetMapping("/search")
    public ApiResponse<?> searchMember(@RequestParam(value = "query") String query,
                                       @RequestParam(value = "page") Integer page,
                                       @RequestParam(value = "size") Integer size) {
        List<MemberInfo> result = memberService.searchMember(query, PageRequest.of(page,size, Sort.by(Sort.Order.asc("nickname"))));
        
        return new ApiResponse.builder<Object>()
                .payload(result)
                .build();
    }

    /**
     * todo: 회원 팔로우
     *
     * @param memberId,follow 팔로우할 상대와, 팔로우할지 여부
     * @return
     */
    @PostMapping("/follow")
    public ApiResponse<?> followMember() {

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 회원의 팔로워 목록 가져오기
     *
     * @param memberId 팔로워 목록을 가져올 회원 아이디 -> 없다면 자기 자신의 팔로워 목록 가져오기
     * @param page     페이지 번호
     * @return 팔로워 목록
     */
    @GetMapping("/follower")
    public ApiResponse<?> getFollower(@RequestParam(value = "memberId", required = false) Integer memberId,
                                      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 회원의 팔로잉 목록 가져오기
     *
     * @param memberId 팔로잉 목록을 가져올 회원 아이디 -> 없다면 자기 자신의 팔로잉 목록 가져오기
     * @param page     페이지 번호
     * @return 팔로잉 목록
     */
    @GetMapping("/following")
    public ApiResponse<?> getFollowing(@RequestParam(value = "memberId", required = false) Integer memberId,
                                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }
}
