package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "member")
public class Member extends Common{
    @Id
    @Column(name = "member_id", nullable = false)
    private Integer id;

    @Column(name = "login_id", nullable = false, length = 20)
    private String loginId;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Lob
    @Column(name = "profile_text")
    private String profileText;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin = false;

}