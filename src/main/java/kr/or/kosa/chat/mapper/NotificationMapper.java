package kr.or.kosa.chat.mapper;

import kr.or.kosa.chat.dto.NotificationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    // 알림 저장
    long insertNotification(NotificationDto notification);

    // 사용자의 모든 알림 조회 (최신순)
    List<NotificationDto> selectNotificationsByUserId(Long receiverUserId);

    // 읽지 않은 알림 조회
    List<NotificationDto> selectUnreadNotifications(Long receiverUserId);

    // 읽지 않은 알림 개수 조회
    long countUnreadNotifications(Long receiverUserId);

    // 특정 알림 읽음 처리
    long updateNotificationReadStatus(@Param("notificationId") Long notificationId);

    // 모든 알림 읽음 처리
    long markAllAsRead(Long userId);

    // 특정 알림 조회
    NotificationDto selectNotificationById(Long notificationId);
}