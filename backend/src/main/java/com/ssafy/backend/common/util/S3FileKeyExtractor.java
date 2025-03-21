package com.ssafy.backend.common.util;

/**
 * <pre>AWS S3 파일 키 추출</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-20
 */
public class S3FileKeyExtractor {
    static String cutter = "https://whistlehub.s3.ap-northeast-2.amazonaws.com/";

    /**
     * 파일 키 추출
     * @param filePath S3 URL
     * @return Key ("폴더/파일명")
     */
    static public String extractS3FileKey(String filePath) {
        return filePath.replaceAll(cutter, "");

    }
}
