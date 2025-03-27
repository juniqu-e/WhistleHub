package com.ssafy.backend.common.service;

import com.ssafy.backend.common.error.exception.FileUploadFailedException;
import com.ssafy.backend.common.error.exception.UnreadableFileException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


/**
 * <pre>AWS S3 서비스</pre>
 *
 * @author 박병주
 * @author 허현준
 * @version 1.1
 * @since 2025-03-13
 * @changes 1.0 - 최초 작성
 *          1.1 - 파일 업로드 예외 처리 추가
 */

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    @Value("${AWS_S3_BUCKET}")
    private String bucketName;
    private String filePrefix;

    public static String IMAGE = "image";
    public static String MUSIC = "music";

    @PostConstruct
    public void init() {
        filePrefix = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";
    }


    /**
     * S3 파일 업로드
     *
     * @param file   업로드 할 파일
     * @param folder 업로드할 폴더
     * @return 업로드 경로 문자열
     */
    public String uploadFile(MultipartFile file, String folder) {
        String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString();

        // UUID + 시간값 + .확장자
        String fileName = folder + "/" + uuid + System.currentTimeMillis() + "." + filenameExtension;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, file.getSize()));

            if (response.sdkHttpResponse().isSuccessful()) {
                return filePrefix + fileName;
            } else {
                throw new FileUploadFailedException();
            }
        } catch (IOException e) {
            throw new UnreadableFileException();
        }
    }

    /**
     * s3 파일 삭제
     *
     * @param fileUrl 삭제할 파일 Url
     */
    public void deleteFile(String fileUrl) {
        // S3에서의 파일 경로 추출 (filePrefix 이후의 경로만 사용)
        String fileKey = fileUrl.split("/")[3];

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();
        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (AwsServiceException e) {
            System.err.println("파일 삭제 실패");
            throw new RuntimeException("파일 삭제 실패");
        }
    }

    /**
     * <pre>S3 업데이트 파일</pre>
     * 기존 파일 삭제와 업로드를 같이
     *
     * @param existingFileUrl 기존 교체 대상 파일의 Url
     * @param newFile         업로드할 파일
     * @param folder          업로드할 폴더
     * @return 새로 업로드된 파일의 Url
     */

    public String updateFile(String existingFileUrl, MultipartFile newFile, String folder) {
        // 기존 파일 삭제
        deleteFile(existingFileUrl);

        // 새 파일 업로드
        return uploadFile(newFile, folder);
    }

    /**
     *
     * @param key 파일 다운로드 키 폴더/파일명
     * @return byte[] 파일
     */
    public byte[] downloadFile(String key) {
        byte[] file = null;
        try {
            ResponseInputStream<GetObjectResponse> s3object = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            // S3 스트림을 byte 배열로 변환
            file = s3object.readAllBytes();

        } catch (AwsServiceException e) {
            throw new RuntimeException("에러: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("파일 다운로드 중 IO 에러 발생: " + e.getMessage(), e);
        }
        return file;
//        ByteArrayResource resource = new ByteArrayResource(file);
    }


}

