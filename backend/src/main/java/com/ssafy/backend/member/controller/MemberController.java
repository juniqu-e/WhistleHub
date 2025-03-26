package com.ssafy.backend.member.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.member.model.MemberDetailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/member")
@RestController
@RequiredArgsConstructor
public class MemberController {
    /**
     * todo: 회원 정보 조회
     *
     * @param memberId 회원 아이디 없는 경우, 자기 자신의 정보 조회
     * @return 회원의 정보
     */
    @GetMapping("/")
    public ApiResponse<?> getMember(@RequestParam(value = "memberId", required = false) Integer memberId) {
        MemberDetailResponseDto result;
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 회원 정보 수정
     *
     * @param nickname, profileText
     * @return
     */
    @PutMapping("/")
    public ApiResponse<?> updateMember() {

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 회원 탈퇴
     *
     * @return
     */
    @DeleteMapping("/")
    public ApiResponse<?> deleteMember() {

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
    public ApiResponse<?> uploadImage() {

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 비밀번호 변경
     *
     * @return
     */
    @PutMapping("/password")
    public ApiResponse<?> updatePassword() {

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * todo: 회원 검색
     *
     * @param query 검색어 -> 닉네임 검색
     * @param page  페이지 번호
     * @param orderBy 정렬 기준
     * @return 검색 결과
     */
    @GetMapping("/search")
    public ApiResponse<?> searchMember(@RequestParam(value = "query") String query,
                                       @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                       @RequestParam(value = "orderBy", required = false, defaultValue = "id") String orderBy) {

        return new ApiResponse.builder<Object>()
                .payload(null)
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
