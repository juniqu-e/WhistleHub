package com.ssafy.backend.track.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.mysql.entity.Comment;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.entity.Track;
import com.ssafy.backend.mysql.repository.CommentRepository;
import com.ssafy.backend.mysql.repository.MemberRepository;
import com.ssafy.backend.mysql.repository.TrackRepository;
import com.ssafy.backend.track.dto.response.CommentResponseDto;
import com.ssafy.backend.track.dto.response.MemberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackCommentService {
    private final TrackRepository trackRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(int trackId) {
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> new RuntimeException("Track not found")
        );
        Member member = authService.getMember();

        if (track.getBlocked() || !track.getEnabled()) return null; // 정지된 트랙 처리
        if (!track.getVisibility() && !member.getId().equals(track.getMember().getId())) return null; // 비공개 트랙 처리


        List<Comment> comments = commentRepository.findAllByTrackId(trackId);
        if (comments.isEmpty()) return null; // Comment Empty
        List<CommentResponseDto> commentList = new ArrayList<>();
        for (Comment comment : comments) {
            Member m = memberRepository.findById(comment.getMember().getId()).orElseThrow(
                    () -> new RuntimeException("Member not found")
            );
            if (!m.getEnabled()) { // 삭제된 회원 처리
                m = Member.builder().id(-1)
                        .nickname("알수 없음")
                        .profileImage(null)
                        .build();
            }
            
            commentList.add(CommentResponseDto.builder()
                    .comment(comment.getComment())
                    .commentId(comment.getId())
                    .memberInfo(MemberInfo.builder()
                            .memberId(m.getId())
                            .nickname(m.getNickname())
                            .profileImg(m.getProfileImage())
                            .build())
                    .build());

        }
        return commentList;
    }

    public void insertTrackComment(int trackId, String context) {
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.error("Track({}) not found", trackId);
                    return new RuntimeException("Track not found");
                }
        );
        Member member = authService.getMember();
        commentRepository.save(Comment.builder()
                .track(track)
                .member(member)
                .comment(context)
                .build());
    }

    @Transactional
    public void updateTrackComment(int commentId, String context) {
        Member member = authService.getMember();
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> {
                    log.warn("찾을 수 없는 Comment({})", commentId);
                    return new RuntimeException();
                }
        );
        if(!comment.getMember().getId().equals(member.getId())) {
            log.warn("권한 없는 덧글 수정 요청");
            throw new RuntimeException();
        }
        comment.setComment(context);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteTrackComment(int commentId) {
        Member member = authService.getMember();
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> {
                    log.warn("Comment not found");
                    return new RuntimeException("Comment not found");
                }
        );
        if (member.getId().equals(comment.getMember().getId())) {
            commentRepository.delete(comment);
        } else {
            throw new RuntimeException("삭제 권한 없음"); // TODO: 예외 클래스 변경
        }
    }

}
