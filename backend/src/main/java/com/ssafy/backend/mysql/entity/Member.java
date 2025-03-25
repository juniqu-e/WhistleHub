package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * <pre>회원 정보 엔티티</pre>
 *
 * 회원 정보를 저장하는 엔티티
 *
 * @since 2025. 3. 25.
 * @version 1.1
 */

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "member")
public class Member extends Common{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name="birth", nullable = false,  length = 20)
    private String birth;

    @Column(name="gender", nullable = false)
    private Character gender;

}