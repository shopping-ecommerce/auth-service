// src/main/java/iuh/fit/se/batch/InvalidTokenCleanupJob.java
package iuh.fit.se.batch;

import iuh.fit.se.config.InvalidTokenCleanupProperties;
import iuh.fit.se.entity.InvalidatedToken;
import iuh.fit.se.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvalidTokenCleanupJob {

    private final InvalidatedTokenRepository tokenRepo;
    private final InvalidTokenCleanupProperties props;

    // Chạy theo cron trong properties, zone VN cho chuẩn giờ
    @Scheduled(cron = "${invalid-token.cleanup.cron:0 0 2 * * MON}", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        if (!props.isEnabled()) {
            log.info("[InvalidTokenCleanup] Disabled -> skip");
            return;
        }

        final int batchSize = Math.max(50, props.getBatchSize() == null ? 500 : props.getBatchSize());
        final Date cutoff = Date.from(Instant.now()); // xoá token đã hết hạn đến thời điểm hiện tại

        log.info("[InvalidTokenCleanup] Start. cutoff={}, batchSize={}", cutoff, batchSize);

        long totalDeleted = 0;
        int round = 0;

        while (true) {
            int deleted = deleteOneBatch(cutoff, batchSize);
            totalDeleted += deleted;
            round++;
            log.info("[InvalidTokenCleanup] Round #{} deleted={}", round, deleted);
            if (deleted < batchSize) break; // không còn đủ 1 batch -> kết thúc
        }

        log.info("[InvalidTokenCleanup] Done. Total deleted={}", totalDeleted);
    }

    @Transactional
    public int deleteOneBatch(Date cutoff, int batchSize) {
        var page = tokenRepo.findByExpiryTimeBefore(cutoff, PageRequest.of(0, batchSize));
        List<InvalidatedToken> tokens = page.getContent();
        if (tokens.isEmpty()) return 0;

        var ids = tokens.stream().map(InvalidatedToken::getId).toList();
        tokenRepo.deleteAllByIdInBatch(ids);
        return ids.size();
    }
}
