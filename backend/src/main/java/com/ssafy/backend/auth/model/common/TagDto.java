package com.ssafy.backend.auth.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>
 * 태그 정보 dto
 * </pre>
 *
 * @see com.ssafy.backend.mysql.entity.Tag
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagDto {
    private Integer id;
    private String name;
}
