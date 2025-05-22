package kr.or.kosa.chat.controller;

import kr.or.kosa.chat.dto.NotificationDto;
import kr.or.kosa.chat.service.NotificationService;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // 사용자의 모든 알림 조회
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUserNotifications(Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long userId = user.getUserId();

        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long userId = user.getUserId();

        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    // 특정 알림 읽음 처리
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Boolean> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long userId = user.getUserId();

        // 보안을 위해 알림이 해당 사용자의 것인지 확인 (옵션)
        NotificationDto notification = notificationService.getNotification(notificationId);
        if (notification != null && !notification.getReceiverUserId().equals(userId)) {
            return ResponseEntity.badRequest().build(); // 다른 사용자의 알림에 접근 시도
        }

        boolean success = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(success);
    }

    // 모든 알림 읽음 처리
    @PostMapping("/read-all")
    public ResponseEntity<Long> markAllAsRead(Authentication authentication) {
        log.info("모든 메시지 읽음 처리 컨트롤러");
        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long userId = user.getUserId();

        long updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(updatedCount);
    }
}