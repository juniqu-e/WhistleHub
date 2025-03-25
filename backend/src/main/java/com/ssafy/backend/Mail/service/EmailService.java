package com.ssafy.backend.Mail.service;

import com.ssafy.backend.Mail.model.common.EmailMessage;
import com.ssafy.backend.common.error.exception.EmailSendFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * <pre>메일 서비스</pre>
 * 
 * 메일 전송을 담당하는 서비스
 * 
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendMail(EmailMessage emailMessage, boolean isHtml) {
        // MimeMessage 객체 생성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            // emailMessage의 내용을 mimeMessage에 담아서 전송
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), isHtml);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.warn("Failed to send email", e);
            throw new EmailSendFailedException();
        }
    }
}
