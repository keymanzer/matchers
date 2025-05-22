package kr.or.kosa.chat.mapper;

import kr.or.kosa.chat.model.ChatMessage;
import kr.or.kosa.chat.model.ChatParticipant;
import kr.or.kosa.chat.model.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {
    void createChatRoom(ChatRoom chatRoom);

    ChatRoom findRoomById(Long id);

    List<ChatRoom> findRoomsByBoardId(Long boardId);

    void addParticipant(ChatParticipant participant);

    List<ChatParticipant> findParticipantsByRoomId(Long roomId);

    boolean existsParticipant(@Param("roomId") Long roomId, @Param("userId") Long userId);

    List<ChatRoom> findRoomsByUserId(Long userId);

    Long findRoomByUserIdsAndBoardId(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("boardId") Long boardId);

    void insertChatMessage(ChatMessage message);

    List<ChatMessage> findChatMessagesByRoomId(Long roomId);

    ChatMessage findLastMessageByRoomId(Long roomId);
}