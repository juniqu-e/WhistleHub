package com.ssafy.backend.mail.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>메일 전송을 위한 메시지 객체</pre>
 * 메일 전송을 위한 메시지 객체
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String to;
    private String subject;
    private String message;
}
