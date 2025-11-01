// src/main/java/iuh/fit/se/config/InvalidTokenCleanupProperties.java
package iuh.fit.se.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "invalid-token.cleanup")
public class InvalidTokenCleanupProperties {
    /** Bật/tắt job */
    private boolean enabled = true;
    /** Cron mặc định: 2:00 sáng Thứ Hai hằng tuần (theo VN) */
    private String cron = "0 0 2 * * MON";
    /** Số bản ghi xoá mỗi vòng (batch) */
    private Integer batchSize = 500;
}
