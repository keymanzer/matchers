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
public class ChatMessage {
    private Long messageId;
    private Long chatRoomId;
    private Long userId;
    private String content;
    private String imageUrl; // S3에 저장된 이미지 URL
    private LocalDateTime createdTime; // DB : TiMESTAMP 타입과 자동 매핑
}