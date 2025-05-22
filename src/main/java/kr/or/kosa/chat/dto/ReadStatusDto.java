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
public class ReadStatusDto {
    private String isRead; // 'Y' or 'N'
    private Long chatRoomId;
    private Long userId;
    private LocalDateTime timestamp; // 읽은 시간
}