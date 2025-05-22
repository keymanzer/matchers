package kr.or.kosa.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatParticipant {
    private Long participantId;
    private Long chatRoomId;
    private Long userId;
}
