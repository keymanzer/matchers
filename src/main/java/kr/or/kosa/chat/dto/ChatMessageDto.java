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
public class ChatMessageDto {
    private Long messageId;
    private String content;
    private String imageUrl;      // 이미지 URL 추가
    private Long senderId;        // 발신자 ID
    private String senderEmail;   // 발신자 이메일
    private String nickname;      // 발신자 닉네임
    private LocalDateTime createdTime;
    private boolean read;
}