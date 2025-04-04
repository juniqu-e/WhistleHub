package com.ssafy.backend.member.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.member.model.common.MemberInfo;
import com.ssafy.backend.member.model.request.RequestFollowRequestDto;
import com.ssafy.backend.member.model.request.UpdateMemberRequestDto;
import com.ssafy.backend.member.model.request.UpdatePasswordRequestDto;
import com.ssafy.backend.member.model.request.UploadProfileImageRequestDto;
import com.ssafy.backend.member.model.response.MemberDetailResponseDto;
import com.ssafy.backend.member.service.MemberService;
import com.ssafy.backend.playlist.dto.TrackInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>회원 컨트롤러</pre>
 * <p>
 * 회원 정보 조회, 수정, 탈퇴, 프로필 이미지 업로드, 비밀번호 변경, 회원 검색, 팔로워/팔로잉 목록 조회, 팔로우 신청 등의 기능을 제공한다.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */
@RequestMapping("/api/member")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;

    /**
     * 회원 정보 조회
     *
     * @param memberId 회원 아이디 없는 경우, 자기 자신의 정보 조회
     * @return 회원의 정보
     */
    @GetMapping()
    public ApiResponse<?> getMember(@RequestParam(value = "memberId") Integer memberId) {
        MemberDetailResponseDto result = memberService.getMember(memberId);

        return new ApiResponse.builder<MemberDetailResponseDto>()
                .payload(result)
                .build();
    }

    /**
     * 회원 정보 수정
     *
     * @param updateMemberRequestDto nickname, profileText 수정할 정보
     */
    @PutMapping()
    public ApiResponse<?> updateMember(@RequestBody UpdateMemberRequestDto updateMemberRequestDto) {
        memberService.updateMember(updateMemberRequestDto);

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping()
    public ApiResponse<?> deleteMember() {
        memberService.deleteMember();

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * 프로필 이미지 업로드
     *
     * @return 이미지 업로드된 링크
     * @see com.ssafy.backend.common.service.S3Service
     */
    @PostMapping("/image")
    public ApiResponse<?> uploadImage(UploadProfileImageRequestDto uploadProfileImageRequestDto) {
        String result = memberService.uploadImage(uploadProfileImageRequestDto);

        return new ApiResponse.builder<String>()
                .payload(result)
                .build();
    }

    @DeleteMapping("/image")
    public ApiResponse<?> deleteImage() {
        memberService.deleteImage();

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * 비밀번호 변경
     *
     * @return 비밀번호 변경 결과
     */
    @PutMapping("/password")
    public ApiResponse<?> updatePassword(@RequestBody UpdatePasswordRequestDto updatePasswordRequestDto) {
        log.debug("updatePasswordRequestDto: {}", updatePasswordRequestDto);
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
                                       @RequestParam(value = "page", defaultValue = "0") Integer page,
                                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        List<MemberInfo> result = memberService.searchMember(query, PageRequest.of(page, size, Sort.by(Sort.Order.asc("nickname"))));

        return new ApiResponse.builder<List<MemberInfo>>()
                .payload(result)
                .build();
    }

    /**
     * 회원 팔로우
     *
     * @param requestFollowRequestDto memberId,follow 팔로우할 상대와, 팔로우할지 여부
     * @return
     */
    @PostMapping("/follow")
    public ApiResponse<?> followMember(@RequestBody RequestFollowRequestDto requestFollowRequestDto) {
        memberService.followMember(requestFollowRequestDto);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    /**
     * 회원의 팔로워 목록 가져오기
     *
     * @param memberId 팔로워 목록을 가져올 회원 아이디 -> 없다면 자기 자신의 팔로워 목록 가져오기
     * @param page     페이지 번호
     * @return 팔로워 목록
     */
    @GetMapping("/follower")
    public ApiResponse<?> getFollower(@RequestParam(value = "memberId") Integer memberId,
                                      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                      @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<MemberInfo> result = memberService.getFollower(memberId, PageRequest.of(page, size, Sort.by(Sort.Order.asc("nickname"))));
        return new ApiResponse.builder<List<MemberInfo>>()
                .payload(result)
                .build();
    }

    /**
     * 회원의 팔로잉 목록 가져오기
     *
     * @param memberId 팔로잉 목록을 가져올 회원 아이디 -> 없다면 자기 자신의 팔로잉 목록 가져오기
     * @param page     페이지 번호
     * @param size     페이지 사이즈
     * @return 팔로잉 목록
     */
    @GetMapping("/following")
    public ApiResponse<?> getFollowing(@RequestParam(value = "memberId") Integer memberId,
                                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                       @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<MemberInfo> result = memberService.getFollowing(memberId, PageRequest.of(page, size, Sort.by(Sort.Order.asc("nickname"))));
        return new ApiResponse.builder<List<MemberInfo>>()
                .payload(result)
                .build();
    }

    /**
     * 회원의 트랙 목록 가져오기
     *
     * @param memberId 트랙 목록을 가져올 회원 아이디 -> 없다면 자기 자신의 트랙 목록 가져오기
     * @param page     페이지 번호
     * @param size     페이지 사이즈
     * @return 트랙 목록
     */
    @GetMapping("/track")
    public ApiResponse<?> getTrack(@RequestParam(value = "memberId") Integer memberId,
                                   @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                   @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<TrackInfo> result = memberService.getTrack(memberId, PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"))));
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    /**
     * 회원의 좋아요 목록 가져오기
     *
     * @param memberId 좋아요 목록을 가져올 회원 아이디 -> 없다면 자기 자신의 좋아요 목록 가져오기
     * @param page     페이지 번호
     * @param size     페이지 사이즈
     * @return 좋아요 목록
     */
    @GetMapping("/like")
    public ApiResponse<?> getLike(@RequestParam(value = "memberId") Integer memberId,
                                  @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                  @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<TrackInfo> result = memberService.getLike(memberId, PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"))));
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

}
