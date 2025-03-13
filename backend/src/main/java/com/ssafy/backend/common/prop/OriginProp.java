package com.ssafy.backend.common.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <pre>ALLOWED_ORIGINS 프로펄티스 로딩</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@Component
@Data
public class OriginProp {
    @Value("ALLOWED_ORIGINS")
    private String ALLOWED_ORIGINS;

    public List<String> getAllowedOriginsList() {
        return List.of(ALLOWED_ORIGINS.split(","));
    }
}
