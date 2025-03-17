package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "playlist_track")
public class PlaylistTrack {
    @Id
    @Column(name = "playlist_track_id", nullable = false)
    private Integer id;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Column(name = "playlist_id", nullable = false)
    private Integer playlistId;

    @Column(name = "play_order")
    private Double playOrder;

}