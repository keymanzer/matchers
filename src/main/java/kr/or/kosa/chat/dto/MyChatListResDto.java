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
public class MyChatListResDto {

    private Long roomId;
    private String roomName;
    private Long unReadCount;
    private String lastMessage;    // 마지막 메시지 내용
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간
    private String otherUserName; // 상대방 이름 --> 프론트에서 사용자별로 채팅방 이름이 달라야해서...
    private String otherUserProfileImg; // 상대방 프로필 이미지
    private String boardState; // 게시판 상태

}
