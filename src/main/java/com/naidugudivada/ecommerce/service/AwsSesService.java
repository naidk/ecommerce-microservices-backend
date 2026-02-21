package com.naidugudivada.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

@Service
@Slf4j
public class AwsSesService {

    private final SesV2Client sesClient;
    private final String senderEmail;

    public AwsSesService(@Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.region}") String regionString,
            @Value("${aws.ses.sender}") String senderEmail) {

        log.info("Initializing AWS SES v2 Client for region: {}", regionString);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        Region region = Region.of(regionString);

        this.sesClient = SesV2Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.senderEmail = senderEmail;
    }

    public void sendEmail(String toAddress, String subject, String bodyHtml) {
        log.info("AWS SES: Attempting to send email to {}", toAddress);

        try {
            Destination destination = Destination.builder()
                    .toAddresses(toAddress)
                    .build();

            Content contentSubject = Content.builder()
                    .data(subject)
                    .build();

            Content contentBody = Content.builder()
                    .data(bodyHtml)
                    .build();

            Body msgBody = Body.builder()
                    .html(contentBody)
                    .build();

            Message message = Message.builder()
                    .subject(contentSubject)
                    .body(msgBody)
                    .build();

            EmailContent emailContent = EmailContent.builder()
                    .simple(message)
                    .build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .fromEmailAddress(senderEmail)
                    .destination(destination)
                    .content(emailContent)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            log.info("AWS SES: Email sent successfully! Message ID: {}", response.messageId());
        } catch (Exception e) {
            log.error("AWS SES: Failed to send email to {} - Error: {}", toAddress, e.getMessage(), e);
        }
    }
}
