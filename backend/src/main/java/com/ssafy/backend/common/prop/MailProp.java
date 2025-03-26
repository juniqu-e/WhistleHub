package com.ssafy.backend.common.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <pre>Mail 프로퍼티스 로딩</pre>
 * 메일 관련 프로퍼티스를 로딩하는 클래스
 *
 * @author  허현준
 * @version 1.0
 * @since 2025-03-26
 */

@Component
@Data
public class MailProp {
    @Value("${MAIL_CODE_EXPIRE_TIME}")
    private Long MAIL_CODE_EXPIRE_TIME;

    @Value("${MAIL_CODE_LENGTH}")
    private Long MAIL_CODE_LENGTH;
}
