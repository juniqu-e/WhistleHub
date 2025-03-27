package com.ssafy.backend.member.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * <pre>
 * 프로필 이미지 업로드 요청 DTO
 * </pre>
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-27
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadProfileImageRequestDto {
    private Integer memberId;
    private MultipartFile image;
}
