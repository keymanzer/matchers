package kr.or.kosa.chat.controller;

import kr.or.kosa.chat.dto.NotificationDto;
import kr.or.kosa.chat.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SseController {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    public SseController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // SSE 구독 엔드포인트
    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable Long userId) {
        log.info("SSE 구독 요청: userId={}", userId);

        // 기존 emitter가 있으면 제거
        removeExistingEmitter(userId);

        // 새 emitter 생성
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃

        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: userId={}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: userId={}", userId);
            emitter.complete();
            emitters.remove(userId);
        });

        emitter.onError(e -> {
            log.error("SSE 연결 오류: userId={}, error={}", userId, e.getMessage());
            emitter.complete();
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);

        try {
            // 초기 연결 확인을 위한 더미 이벤트 전송 >> 초기 연결시 확인용
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결이 성공적으로 설정되었습니다.")
            );
            log.info("SSE 초기 연결 이벤트 전송 완료: userId={}", userId);

            // 읽지 않은 알림 개수 전송
            long unreadCount = notificationService.countUnreadNotifications(userId);
            emitter.send(SseEmitter.event()
                    .name("unread-count")
                    .data(unreadCount)
            );
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패: userId={}, error={}", userId, e.getMessage());
            emitter.complete();
            emitters.remove(userId);
        }

        return emitter;
    }

    // 기존 emitter 제거
    private void removeExistingEmitter(Long userId) {
        SseEmitter existingEmitter = emitters.get(userId);
        if (existingEmitter != null) {
            existingEmitter.complete();
            emitters.remove(userId);
            log.info("기존 SSE 연결 제거: userId={}", userId);
        }
    }

    // 알림 전송 메서드 (DB 저장 + 실시간 알림) >> 채팅 알람
    public void sendNotification(Long receiverUserId, Long senderUserId, Long chatRoomId, String message) {
        log.info("알림 전송 시작: receiverUserId={}, message={}", receiverUserId, message);

        // 1. DB에 알림 저장
        NotificationDto notification = notificationService.createChatNotification(
                receiverUserId, senderUserId, chatRoomId, message);

        // 2. SSE를 통한 실시간 알림 전송
        SseEmitter emitter = emitters.get(receiverUserId);
        if (emitter != null) {
            try {
                log.info("SSE 알림 전송: userId={}, message={}", receiverUserId, message);

                // 알림 데이터 전송
                emitter.send(SseEmitter.event()
                        .name("chat-message")
                        .data(notification)
                );

                // 읽지 않은 알림 개수 전송
                long unreadCount = notificationService.countUnreadNotifications(receiverUserId);
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(unreadCount)
                );

                log.info("SSE 알림 전송 완료: userId={}", receiverUserId);
            } catch (IOException e) {
                log.error("SSE 알림 전송 실패: userId={}, error={}", receiverUserId, e.getMessage());
                emitters.remove(receiverUserId);
            }
        } else {
            log.info("SSE emitter not found for user: {}", receiverUserId);
            // emitter가 없어도 DB에는 저장되어 있으므로 나중에 확인 가능
        }
    }


    // 권한 승인 알람
    public void sendNotification(Long receiverUserId, Long senderUserId, String message) {
        log.info("알림 전송 시작: receiverUserId={}, message={}", receiverUserId, message);

        // 1. DB에 알림 저장
        NotificationDto notification = notificationService.createAdminNotification(
                receiverUserId, senderUserId, message);

        // 2. SSE를 통한 실시간 알림 전송
        SseEmitter emitter = emitters.get(receiverUserId);
        if (emitter != null) {
            try {
                log.info("SSE 알림 전송: userId={}, message={}", receiverUserId, message);

                // 알림 데이터 전송
                emitter.send(SseEmitter.event()
                        .name("admin-message")
                        .data(notification)
                );

                // 읽지 않은 알림 개수 전송
                long unreadCount = notificationService.countUnreadNotifications(receiverUserId);
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(unreadCount)
                );

                log.info("SSE 알림 전송 완료: userId={}", receiverUserId);
            } catch (IOException e) {
                log.error("SSE 알림 전송 실패: userId={}, error={}", receiverUserId, e.getMessage());
                emitters.remove(receiverUserId);
            }
        } else {
            log.info("SSE emitter not found for user: {}", receiverUserId);
            // emitter가 없어도 DB에는 저장되어 있으므로 나중에 확인 가능
        }
    }
}