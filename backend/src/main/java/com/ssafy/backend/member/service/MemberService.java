package com.ssafy.backend.member.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.*;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.graph.service.RelationshipService;
import com.ssafy.backend.member.model.common.MemberInfo;
import com.ssafy.backend.member.model.request.RequestFollowRequestDto;
import com.ssafy.backend.member.model.request.UpdateMemberRequestDto;
import com.ssafy.backend.member.model.request.UpdatePasswordRequestDto;
import com.ssafy.backend.member.model.request.UploadProfileImageRequestDto;
import com.ssafy.backend.member.model.response.MemberDetailResponseDto;
import com.ssafy.backend.mysql.entity.Follow;
import com.ssafy.backend.mysql.entity.Like;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.entity.Track;
import com.ssafy.backend.mysql.repository.FollowRepository;
import com.ssafy.backend.mysql.repository.LikeRepository;
import com.ssafy.backend.mysql.repository.MemberRepository;
import com.ssafy.backend.mysql.repository.TrackRepository;
import com.ssafy.backend.playlist.dto.TrackInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

/**
 * <pre>회원 서비스</pre>
 * <p>
 * 회원 정보 조회, 수정, 탈퇴, 프로필 이미지 업로드, 비밀번호 변경, 회원 검색, 팔로워/팔로잉 목록 조회, 팔로우 신청 등의 기능을 제공한다.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final TrackRepository trackRepository;
    private final LikeRepository likeRepository;
    private final RelationshipService relationshipService;

    /**
     * 회원 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 정보
     */
    public MemberDetailResponseDto getMember(Integer memberId) {
        // 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", memberId);
                    return new NotFoundMemberException();
                });

        // 팔로워 수, 팔로잉 수, 트랙 수 조회
        int followerCount = followRepository.countByToMemberId(memberId);
        int followingCount = followRepository.countByFromMemberId(memberId);
        int trackCount = trackRepository.countByMemberId(memberId);

        return MemberDetailResponseDto.builder()
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .profileText(member.getProfileText())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .trackCount(trackCount)
                .build();
    }

    /**
     * 회원 정보 수정
     *
     * @param updateMemberRequestDto 회원 정보 수정 요청 DTO
     */
    @Transactional
    public void updateMember(UpdateMemberRequestDto updateMemberRequestDto) {
        // 회원 정보 수정
        Member member = authService.getMember();
        member.setNickname(updateMemberRequestDto.getNickname());
        member.setProfileText(updateMemberRequestDto.getProfileText());
        memberRepository.save(member);
    }

    /**
     * 회원 탈퇴
     */
    public void deleteMember() {
        // 회원 탈퇴
        Member member = authService.getMember();
        member.setEnabled(false);

        memberRepository.save(member);
    }

    /**
     * 프로필 이미지 업로드
     *
     * @param uploadProfileImageRequestDto 프로필 이미지 업로드 요청 DTO
     * @return 업로드된 이미지 URL
     */
    @Transactional
    public String uploadImage(UploadProfileImageRequestDto uploadProfileImageRequestDto) {
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
        } else {
            // 기존에 업로드한 경우
            imageUrl = s3Service.updateFile(memberProfileImagePath, multipartFile, S3Service.IMAGE);
            member.setProfileImage(imageUrl);
        }

        // 멤버 정보 업데이트
        memberRepository.save(member);

        return imageUrl;
    }

    public void deleteImage() {
        Member member = authService.getMember();
        String memberProfileImageUrl = member.getProfileImage();

        // 프로필 이미지 삭제
        if (memberProfileImageUrl != null) {
            s3Service.deleteFile(memberProfileImageUrl);
            member.setProfileImage(null);
            memberRepository.save(member);
        }
    }

    /**
     * 비밀번호 변경
     *
     * @param updatePasswordRequestDto 비밀번호 변경 요청 DTO
     */
    @Transactional
    public void updatePassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        // 비밀번호 변경
        Member member = authService.getMember();
        String oldPassword = updatePasswordRequestDto.getOldPassword();
        String newPassword = updatePasswordRequestDto.getNewPassword();

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(oldPassword, member.getPassword()))
            throw new InvalidOldPasswordException();

        // 새로운 비밀번호 입력 검증
        if (!authService.validatePasswordFormat(newPassword)) // 새로운 비밀번호가 형식이 맞지 않을 경우
            throw new InvalidNewPasswordException();
        if (oldPassword.equals(newPassword)) // 기존 비밀번호와 새로운 비밀번호가 같을 경우
            throw new InvalidNewPasswordException();

        newPassword = passwordEncoder.encode(newPassword);
        member.setPassword(newPassword);
        memberRepository.save(member);
    }

    /**
     * 회원 검색
     *
     * @param query       검색어
     * @param pageRequest 페이지 요청
     * @return 회원 정보 리스트
     */
    public List<MemberInfo> searchMember(String query, PageRequest pageRequest) {
        Member myself = authService.getMember();
        // 회원 검색
        List<Member> memberList = memberRepository.findByNicknameContainingAndIdNot(query, myself.getId(), pageRequest);
        List<MemberInfo> memberInfoList = new LinkedList<>();
        for (Member member : memberList) {
            MemberInfo memberInfo = MemberInfo.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .profileImg(member.getProfileImage())
                    .build();

            memberInfoList.add(memberInfo);
        }

        if(memberInfoList.isEmpty()){
            throw new NotFoundPageException();
        }

        return memberInfoList;
    }

    /**
     * 회원의 팔로워 목록 가져오기
     *
     * @param memberId    팔로워 목록을 가져올 회원 ID
     * @param pageRequest 페이지 요청
     * @return 팔로워 정보 리스트
     */
    public List<MemberInfo> getFollower(Integer memberId, PageRequest pageRequest) {
        // 회원의 팔로워 목록 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", memberId);
                    return new NotFoundMemberException();
                });

        List<Follow> followerList = followRepository.findByToMemberId(member.getId(), pageRequest);
        List<MemberInfo> followerInfoList = new LinkedList<>();
        for (Follow follower : followerList) {
            Member followerMember = follower.getFromMember();
            MemberInfo memberInfo = MemberInfo.builder()
                    .memberId(followerMember.getId())
                    .nickname(followerMember.getNickname())
                    .profileImg(followerMember.getProfileImage())
                    .build();

            followerInfoList.add(memberInfo);
        }

        if(followerInfoList.isEmpty()){
            throw new NotFoundPageException();
        }

        return followerInfoList;
    }

    /**
     * 회원의 팔로잉 목록 가져오기
     *
     * @param memberId    팔로잉 목록을 가져올 회원 ID
     * @param pageRequest 페이지 요청
     * @return 팔로잉 정보 리스트
     */
    public List<MemberInfo> getFollowing(Integer memberId, PageRequest pageRequest) {
        // 회원의 팔로잉 목록 가져오기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", memberId);
                    return new NotFoundMemberException();
                });


        List<Follow> followingList = followRepository.findByFromMemberId(member.getId(), pageRequest);
        List<MemberInfo> followingInfoList = new LinkedList<>();
        for (Follow following : followingList) {
            Member followingMember = following.getToMember();
            MemberInfo memberInfo = MemberInfo.builder()
                    .memberId(followingMember.getId())
                    .nickname(followingMember.getNickname())
                    .profileImg(followingMember.getProfileImage())
                    .build();

            followingInfoList.add(memberInfo);
        }

        if(followingInfoList.isEmpty()){
            throw new NotFoundPageException();
        }

        return followingInfoList;
    }


    /**
     * 회원 팔로우 신청
     *
     * @param requestFollowRequestDto 팔로우 신청 요청 DTO
     */
    public void followMember(RequestFollowRequestDto requestFollowRequestDto) {
        Member member = authService.getMember();
        boolean followRequest = requestFollowRequestDto.getFollow();

        Member targetMember = memberRepository.findById(requestFollowRequestDto.getMemberId())
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", requestFollowRequestDto.getMemberId());
                    return new NotFoundMemberException();
                });

        if (followRequest) { // 팔로우 신청 요청인경우,
            followRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId()).ifPresent(follow -> {
                log.warn("이미 팔로우 신청한 회원입니다. memberId : {}", requestFollowRequestDto.getMemberId());
                throw new DuplicateFollowRequestException();
            });

            Follow follow = Follow.builder()
                    .fromMember(member)
                    .toMember(targetMember)
                    .build();

            followRepository.save(follow);
            relationshipService.createFollowRelationship(member.getId(), targetMember.getId());
        } else { // 팔로우 취소 요청인 경우,
            Follow follow = followRepository.findByFromMemberIdAndToMemberId(member.getId(), targetMember.getId()).orElseThrow(()->{
                log.warn("팔로우 신청하지 않은 회원입니다. memberId : {}", requestFollowRequestDto.getMemberId());
                return new DuplicateFollowRequestException();
            });

            followRepository.delete(follow);
            relationshipService.deleteFollowRelationship(member.getId(), targetMember.getId());
        }
    }

    /**
     * 회원의 트랙 목록 가져오기
     *
     * @param memberId    트랙 목록을 가져올 회원 ID
     * @param pageRequest 페이지 요청
     * @return 트랙 정보 리스트
     */
    public List<TrackInfo> getTrack(Integer memberId, PageRequest pageRequest) {
        // 회원의 트랙 목록 가져오기
        Member requestMember = authService.getMember();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", memberId);
                    return new NotFoundMemberException();
                });
        List<Track> trackList;

        // 트랙 목록 조회
        if(requestMember.getId().equals(member.getId())){
            trackList = trackRepository.findByMemberId(member.getId(), pageRequest);
        }else{
            trackList = trackRepository.findByMemberIdAndVisibility(member.getId(), true, pageRequest);
        }

        // 트랙 정보 리스트 생성
        List<TrackInfo> trackInfoList = new LinkedList<>();
        for (Track track : trackList) {
            TrackInfo trackInfo = TrackInfo.builder()
                    .trackId(track.getId())
                    .title(track.getTitle())
                    .nickname(member.getNickname())
                    .duration(track.getDuration())
                    .imageUrl(track.getImageUrl())
                    .build();

            trackInfoList.add(trackInfo);
        }

        if(trackInfoList.isEmpty()){
            throw new NotFoundPageException();
        }

        return trackInfoList;
    }

    /**
     * 회원의 좋아요 트랙 목록 가져오기
     * @param memberId 트랙 목록을 가져올 회원 ID
     * @param pageRequest 페이지 요청
     * @return 트랙 정보 리스트
     */
    public List<TrackInfo> getLike(Integer memberId, PageRequest pageRequest) {
        // 회원의 트랙 목록 가져오기
        Member requestMember = authService.getMember();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("해당하는 회원이 없습니다. memberId : {}", memberId);
                    return new NotFoundMemberException();
                });
        List<Like> likeList;

        // 트랙 목록 조회
        if(requestMember.getId().equals(member.getId())){
            likeList = likeRepository.findByMemberId(member.getId(), pageRequest);
        }else{
            log.warn("권한이 없는 회원입니다. memberId : {}", requestMember.getId());
            throw new NotPermittedException();
        }

        // 트랙 정보 리스트 생성
        List<TrackInfo> trackInfoList = new LinkedList<>();
        for (Like like : likeList) {
            Track track = like.getTrack();

            TrackInfo trackInfo = TrackInfo.builder()
                    .trackId(track.getId())
                    .title(track.getTitle())
                    .nickname(member.getNickname())
                    .duration(track.getDuration())
                    .imageUrl(track.getImageUrl())
                    .build();

            trackInfoList.add(trackInfo);
        }

        if(trackInfoList.isEmpty()){
            throw new NotFoundPageException();
        }

        return trackInfoList;
    }
}
