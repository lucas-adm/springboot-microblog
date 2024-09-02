package com.adm.lucas.microblog.adapter.consumer;

import com.adm.lucas.microblog.adapter.consumer.dto.ActivationDTO;
import com.adm.lucas.microblog.adapter.consumer.dto.RecoveryDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailConsumer {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public void sendActivationMail(ActivationDTO dto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
        helper.setFrom(mailFrom);
        helper.setTo(dto.mailTo());
        helper.setSubject(dto.subject());
        helper.setText(dto.text(), true);
        mailSender.send(message);
    }

    public void sendRecoveryMail(RecoveryDTO dto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
        helper.setFrom(mailFrom);
        helper.setTo(dto.mailTo());
        helper.setSubject(dto.subject());
        helper.setText(dto.text(), true);
        mailSender.send(message);
    }

    @RabbitListener(queues = "${broker.queue.activation.name}")
    public void activationQueueListenner(@Payload ActivationDTO dto) throws MessagingException {
        try {
            sendActivationMail(dto);
        } catch (MessagingException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @RabbitListener(queues = "${broker.queue.recovery.name}")
    public void recoveryQueueListenner(@Payload RecoveryDTO dto) throws MessagingException {
        try {
            sendRecoveryMail(dto);
        } catch (MessagingException exception) {
            System.out.println(exception.getMessage());
        }
    }

}