package kr.or.kosa.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long notificationId;
    private Long receiverUserId;
    private String isRead;  // "Y" 또는 "N"
    private Long senderUserId;
    private Long chatRoomId;
    private String content;
    private String notificationType; // "CHAT" 또는 "ADMIN"
    private LocalDateTime createdAt;

    // 읽음 여부 확인 편의 메소드
    public boolean isReadStatus() {
        return "Y".equals(this.isRead);
    }
}