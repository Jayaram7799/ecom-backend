
package in.btm.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import in.btm.service.AuthServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailUtil {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	// =========================================================
	// REGISTRATION EMAIL
	// =========================================================

	public void sendRegistrationEmail(String to, String temporaryPassword) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom(fromEmail);
		message.setTo(to);
		message.setSubject("Cartora Account Registration - Activation Required");

		message.setText(
				"Hello,\n\n" + "Your Cartora account has been created successfully.\n\n" + "Temporary Password: "
						+ temporaryPassword + "\n\n" + "Please use this temporary password to activate "
						+ "your account and create your permanent password.\n\n"
						+ "For security reasons, please do not share this " + "password with anyone.\n\n" + "Regards,\n"
						+ "Cartora Team");

		mailSender.send(message);
	}

	// =========================================================
	// PASSWORD RESET EMAIL
	// =========================================================

	public void sendResetPasswordEmail(String to, String resetToken) {

	    try {

	        String resetLink =
	                frontendUrl + "/reset-password?token=" + resetToken;

	        MimeMessage message = mailSender.createMimeMessage();

	        MimeMessageHelper helper =
	                new MimeMessageHelper(
	                        message,
	                        true,
	                        StandardCharsets.UTF_8.name()
	                );

	        helper.setFrom(fromEmail);
	        helper.setTo(to);
	        helper.setSubject("Cartora Account - Password Reset");

	        String htmlContent = """
	                <!DOCTYPE html>
	                <html>
	                <head>
	                    <meta charset="UTF-8">
	                    <meta name="viewport"
	                          content="width=device-width, initial-scale=1.0">
	                    <title>Cartora Password Reset</title>
	                </head>

	                <body style="
	                    margin: 0;
	                    padding: 0;
	                    background-color: #f4f4f4;
	                    font-family: Arial, Helvetica, sans-serif;
	                ">

	                    <div style="
	                        max-width: 600px;
	                        margin: 40px auto;
	                        padding: 35px;
	                        background-color: #ffffff;
	                        border-radius: 10px;
	                    ">

	                        <h2 style="
	                            color: #222222;
	                            margin-bottom: 20px;
	                        ">
	                            Cartora Account - Password Reset
	                        </h2>

	                        <p style="
	                            color: #333333;
	                            font-size: 16px;
	                        ">
	                            Hello,
	                        </p>

	                        <p style="
	                            color: #333333;
	                            font-size: 16px;
	                            line-height: 1.6;
	                        ">
	                            We received a request to reset your
	                            Cartora account password.
	                        </p>

	                        <p style="
	                            color: #333333;
	                            font-size: 16px;
	                            line-height: 1.6;
	                        ">
	                            Click the button below to reset your password.
	                        </p>

	                        <div style="
	                            text-align: center;
	                            margin: 30px 0;
	                        ">

	                            <a href="%s"
	                               target="_blank"
	                               style="
	                                   display: inline-block;
	                                   background-color: #1976d2;
	                                   color: #ffffff;
	                                   padding: 14px 28px;
	                                   text-decoration: none;
	                                   border-radius: 6px;
	                                   font-size: 16px;
	                                   font-weight: bold;
	                               ">
	                                Reset Password
	                            </a>

	                        </div>

	                        <p style="
	                            color: #555555;
	                            font-size: 14px;
	                            line-height: 1.6;
	                        ">
	                            This password reset link is valid for
	                            <strong>15 minutes</strong>.
	                        </p>

	                        <p style="
	                            color: #555555;
	                            font-size: 14px;
	                            line-height: 1.6;
	                        ">
	                            If you did not request a password reset,
	                            please ignore this email.
	                        </p>

	                        <p style="
	                            margin-top: 30px;
	                            color: #333333;
	                            font-size: 14px;
	                        ">
	                            Regards,<br>
	                            <strong>Cartora Team</strong>
	                        </p>

	                    </div>

	                </body>
	                </html>
	                """.formatted(resetLink);

	        // Send HTML email
	        helper.setText(htmlContent, true);

	        mailSender.send(message);

	        log.info("Password reset email sent to {}", to);

	    } catch (MessagingException e) {

	        log.error(
	                "Failed to send password reset email to {}",
	                to,
	                e
	        );

	        throw new RuntimeException(
	                "Failed to send password reset email",
	                e
	        );
	    }
	}
	// =========================================================
	// ACCOUNT ACTIVATION EMAIL
	// =========================================================

	public void sendActivationEmail(String to, String activationToken) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom(fromEmail);
		message.setTo(to);
		message.setSubject("Cartora Account - Activate Your Account");

		message.setText("Hello,\n\n" + "Thank you for registering with Cartora.\n\n"
				+ "Your account activation token is:\n\n" + activationToken + "\n\n"
				+ "This token is valid for 15 minutes.\n\n" + "Please use this token to activate your account "
				+ "and create your password.\n\n" + "If you did not create this account, "
				+ "please ignore this email.\n\n" + "Regards,\n" + "Cartora Team");

		mailSender.send(message);
	}
}
