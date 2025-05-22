package kr.or.kosa.chat.service;

import kr.or.kosa.chat.dto.NotificationDto;
import kr.or.kosa.chat.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationMapper notificationMapper;

    // 알림 저장
    @Transactional
    public NotificationDto saveNotification(NotificationDto notification) {
        // 기본값 설정
        if (notification.getIsRead() == null) {
            notification.setIsRead("N");
        }

        notificationMapper.insertNotification(notification);
        return notification;
    }

    // 채팅 알림 생성
    @Transactional
    public NotificationDto createChatNotification(Long receiverUserId, Long senderUserId, Long chatRoomId, String content) {
        NotificationDto notification = NotificationDto.builder()
                .receiverUserId(receiverUserId)
                .senderUserId(senderUserId)
                .chatRoomId(chatRoomId)
                .content(content)
                .notificationType("CHAT")
                .isRead("N")
                .build();

        return saveNotification(notification);
    }

    @Transactional
    public NotificationDto createAdminNotification(Long receiverUserId, Long senderUserId, String content) {
        NotificationDto notification = NotificationDto.builder()
                .receiverUserId(receiverUserId)
                .senderUserId(senderUserId)
                .content(content)
                .notificationType("ADMIN")
                .isRead("N")
                .build();

        return saveNotification(notification);
    }

    // 사용자의 모든 알림 조회
    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationMapper.selectNotificationsByUserId(userId);
    }

    // 사용자의 읽지 않은 알림 조회
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationMapper.selectUnreadNotifications(userId);
    }

    // 읽지 않은 알림 개수 조회
    public long countUnreadNotifications(Long userId) {
        return notificationMapper.countUnreadNotifications(userId);
    }

    // 특정 알림 읽음 처리
    @Transactional
    public boolean markAsRead(Long notificationId) {
        long result = notificationMapper.updateNotificationReadStatus(notificationId);
        return result > 0;
    }

    // 모든 알림 읽음 처리
    @Transactional
    public long markAllAsRead(Long userId) {
        log.info("사용자 ID {}의 모든 알림 읽음 처리 시작", userId);
        long result = notificationMapper.markAllAsRead(userId);
        log.info("사용자 ID {}의 모든 알림 읽음 처리 완료: 업데이트된 알림 수 {}", userId, result);
        return result;
    }

    // 특정 알림 조회
    public NotificationDto getNotification(Long notificationId) {
        return notificationMapper.selectNotificationById(notificationId);
    }
}