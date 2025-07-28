package iuh.fit.se.service.impl;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import iuh.fit.event.dto.NotificationEvent;
import iuh.fit.se.dto.request.VerifyOTPRequest;
import iuh.fit.se.service.OTPService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class OTPServiceImpl implements OTPService {
    RedisTemplate<String, String> redisTemplate;
    KafkaTemplate<String, Object> kafkaTemplate;
    static String OTP_KEY_PREFIX = "otp:user:";
    static long OTP_EXPIRY_SECONDS = 60; // 1 phút

    @Override
    public void createAndSaveOTP(String email) {
        try {
            String otp = OTPGenerator.generateOTP();
            String key = OTP_KEY_PREFIX + email;
            redisTemplate.opsForValue().set(key, otp, OTP_EXPIRY_SECONDS, TimeUnit.SECONDS);
            log.info("OTP for {}: {}", email, otp);
            // publish message to kafka
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .body(otp)
                    .subject("Your OTP Code")
                    .recipient(email)
                    .build();
            //            kafkaTemplate.send("notification-delivery", notificationEvent);
            kafkaTemplate.send("notification-delivery", notificationEvent);
        } catch (Exception e) {
            log.error("Failed to save OTP to Redis", e);
        }
    }

    @Override
    public boolean verifyOTP(VerifyOTPRequest verifyOTPRequest) {
        // Get the OTP from Redis
        String key = OTP_KEY_PREFIX + verifyOTPRequest.getEmail();
        String storedOTP = redisTemplate.opsForValue().get(key);
        if (storedOTP == null) {
            log.warn("No OTP found for email: {}", verifyOTPRequest.getEmail());
            return false;
        }
        // Check if the OTP matches
        if (storedOTP.equals(verifyOTPRequest.getOtp())) {
            // OTP is valid, delete it from Redis
            deleteOTP(verifyOTPRequest.getEmail());
            log.info("OTP verified successfully for email: {}", verifyOTPRequest.getEmail());
            return true;
        } else {
            log.warn("Invalid OTP for email: {}", verifyOTPRequest.getEmail());
            return false;
        }
    }

    @Override
    public void deleteOTP(String email) {
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.delete(key);
        log.info("OTP deleted for email: {}", email);
    }

    class OTPGenerator {
        private static final String DIGITS = "0123456789";
        private static final int OTP_LENGTH = 6;

        public static String generateOTP() {
            SecureRandom random = new SecureRandom();
            StringBuilder otp = new StringBuilder(OTP_LENGTH);
            for (int i = 0; i < OTP_LENGTH; i++) {
                otp.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
            }
            return otp.toString();
        }
    }
}
