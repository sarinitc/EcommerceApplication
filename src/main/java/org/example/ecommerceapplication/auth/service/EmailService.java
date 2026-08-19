package org.example.ecommerceapplication.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    public void sendOtp(
            String toEmail,
            String otp
    ) {

        try {

            MimeMessage mimeMessage =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(
                    "Verify your email - ECommerce"
            );

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                    </head>

                    <body style="
                        margin:0;
                        padding:0;
                        background:#f4f6f8;
                        font-family:Arial, sans-serif;
                    ">

                        <table width="100%%"
                               cellpadding="0"
                               cellspacing="0">

                            <tr>
                                <td align="center"
                                    style="padding:40px 15px;">

                                    <table width="100%%"
                                           cellpadding="0"
                                           cellspacing="0"
                                           style="
                                               max-width:500px;
                                               background:white;
                                               border-radius:12px;
                                               padding:40px;
                                               box-shadow:
                                                   0 4px 15px
                                                   rgba(0,0,0,0.08);
                                           ">

                                        <tr>
                                            <td align="center">

                                                <h2 style="
                                                    margin:0;
                                                    color:#111827;
                                                ">
                                                    ECommerce
                                                </h2>

                                                <p style="
                                                    color:#6b7280;
                                                    margin-top:8px;
                                                ">
                                                    Email Verification
                                                </p>

                                            </td>
                                        </tr>


                                        <tr>
                                            <td style="
                                                padding-top:30px;
                                            ">

                                                <h3 style="
                                                    color:#111827;
                                                    margin-bottom:10px;
                                                ">
                                                    Verify your email
                                                </h3>

                                                <p style="
                                                    color:#4b5563;
                                                    line-height:1.6;
                                                ">
                                                    Thank you for
                                                    registering.
                                                    Use the verification
                                                    code below to
                                                    complete your
                                                    registration.
                                                </p>

                                            </td>
                                        </tr>


                                        <tr>
                                            <td align="center"
                                                style="
                                                    padding:25px 0;
                                                ">

                                                <div style="
                                                    display:inline-block;
                                                    padding:
                                                        16px 30px;
                                                    background:#f3f4f6;
                                                    border-radius:8px;
                                                    font-size:32px;
                                                    font-weight:bold;
                                                    letter-spacing:8px;
                                                    color:#111827;
                                                ">
                                                    %s
                                                </div>

                                            </td>
                                        </tr>


                                        <tr>
                                            <td>

                                                <p style="
                                                    color:#6b7280;
                                                    font-size:14px;
                                                    line-height:1.6;
                                                ">
                                                    This verification
                                                    code will expire
                                                    in
                                                    <strong>
                                                        5 minutes
                                                    </strong>.
                                                </p>

                                                <p style="
                                                    color:#6b7280;
                                                    font-size:14px;
                                                ">
                                                    If you did not
                                                    create this
                                                    account, you can
                                                    safely ignore
                                                    this email.
                                                </p>

                                            </td>
                                        </tr>


                                        <tr>
                                            <td style="
                                                padding-top:30px;
                                                border-top:
                                                    1px solid #e5e7eb;
                                            ">

                                                <p style="
                                                    text-align:center;
                                                    color:#9ca3af;
                                                    font-size:12px;
                                                ">
                                                    © 2026 ECommerce
                                                    Application
                                                </p>

                                            </td>
                                        </tr>

                                    </table>

                                </td>
                            </tr>

                        </table>

                    </body>
                    </html>
                    """.formatted(otp);

            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send OTP email",
                    e
            );
        }
    }
}