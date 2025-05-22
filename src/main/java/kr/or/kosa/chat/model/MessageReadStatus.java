package kr.or.kosa.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageReadStatus {
    private Long messageReadId;
    private Long messageId;
    private Long chatRoomId;
    private Long userId;
    private String isRead; // 'Y' or 'N'
    private LocalDateTime updateTime; // 업데이트(읽은) 시간
}
