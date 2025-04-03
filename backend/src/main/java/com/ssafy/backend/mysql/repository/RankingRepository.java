package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.discovery.model.common.TrackScore;
import com.ssafy.backend.mysql.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <pre>랭킹 Repository</pre>
 * 랭킹 관련 데이터베이스 작업을 처리하는 Repository 인터페이스이다.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-04-03
 */

public interface RankingRepository extends JpaRepository<Track, Integer> {
    /**
     * <pre>랭킹 조회</pre>
     * 태그 ID에 해당하는 트랙의 랭킹을 조회한다.
     *
     * @param tagId      태그 ID
     * @param startDate  시작일자
     * @param limit      페이지 크기
     * @param likeWeight 좋아요 가중치
     * @param viewWeight 재생 수 가중치
     * @return 트랙 랭킹 리스트
     */
    @Query(value = "SELECT t.track_id as trackId, " +
            "       (COALESCE(l.like_count, 0) * :likeWeight + COALESCE(v.view_count, 0) * :viewWeight) as score " +
            "FROM track t " +
            "JOIN track_tag tt ON t.track_id = tt.track_id " +
            "LEFT JOIN ( " +
            "    SELECT track_id, COUNT(*) as like_count " +
            "    FROM `like` " +
            "    WHERE created_at >= :startDate " +
            "    GROUP BY track_id " +
            ") l ON t.track_id = l.track_id " +
            "LEFT JOIN ( " +
            "    SELECT track_id, COUNT(*) as view_count " +
            "    FROM listen_record " +
            "    WHERE created_at  >= :startDate " +
            "    GROUP BY track_id " +
            ") v ON t.track_id = v.track_id " +
            "WHERE tt.tag_id = :tagId " +
            "ORDER BY score DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<TrackScore> findRanking(@Param("tagId") Integer tagId, @Param("startDate") String startDate, @Param("limit") Integer limit, @Param("likeWeight") Integer likeWeight, @Param("viewWeight") Integer viewWeight);
}
