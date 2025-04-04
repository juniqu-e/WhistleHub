package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Track;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {
    List<Track> findAllByTitleContains(String title, PageRequest pageRequest);
    List<Track> findAllByTitleContains(String title);

    List<Track> findByMemberIdAndVisibility(int memberId, boolean visibility, PageRequest pageRequest);
    List<Track> findByMemberId(int memberId, PageRequest pageRequest);
<<<<<<< HEAD
    int countByMemberId(int memberId);
=======
>>>>>>> origin/feature/discovery

    @Query(value = "SELECT t.* FROM track t WHERE t.track_id NOT IN " +
                   "(SELECT lr.track_id FROM listen_record lr WHERE lr.member_id = :memberId) " +
                   "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Track> findRandomTracksNotListenedByMember(@Param("memberId") int memberId, @Param("limit") int limit);
}
