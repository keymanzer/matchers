package kr.or.kosa.chat.service;

import kr.or.kosa.chat.dto.ChatMessageDto;
import kr.or.kosa.chat.dto.MyChatListResDto;
import kr.or.kosa.chat.dto.OtherUserDto;
import kr.or.kosa.chat.mapper.ChatMapper;
import kr.or.kosa.chat.mapper.ReadStatusMapper;
import kr.or.kosa.chat.model.ChatMessage;
import kr.or.kosa.chat.model.ChatParticipant;
import kr.or.kosa.chat.model.ChatRoom;
import kr.or.kosa.mapper.UserMapper;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMapper chatMapper;
    private final ReadStatusMapper readStatusMapper;
    private final UserMapper userMapper;

    // 로그인한 사용자 정보 가져오기
    private CustomUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return (CustomUser) auth.getPrincipal();
    }

    // 채팅방 생성 또는 조회
    @Transactional
    public Long getOrCreateChatRoom(Long boardId, Long otherUserId) {
        CustomUser loginUser = getLoginUser();
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Long loginUserId = loginUser.getUserId();

        // 이미 존재하는 채팅방 확인
        Long roomId = chatMapper.findRoomByUserIdsAndBoardId(loginUserId, otherUserId, boardId);
        if (roomId != null) {
            return roomId; // 기존 채팅방 ID 반환
        }

        // 상대방 이름 가져오기
        String otherUserName = userMapper.findNameById(otherUserId);
        if (otherUserName == null) {
            otherUserName = "익명";
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(otherUserName) // 채팅방 이름을 상대방 이름으로 설정
                .boardId(boardId)
                .build();

        chatMapper.createChatRoom(chatRoom);

        // 참가자 추가
        chatMapper.addParticipant(ChatParticipant.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .userId(loginUserId)
                .build());

        chatMapper.addParticipant(ChatParticipant.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .userId(otherUserId)
                .build());

        return chatRoom.getChatRoomId();
    }

    // 채팅방 접근 권한 확인
    public boolean isRoomParticipant(Long userId, Long roomId) {
        return chatMapper.existsParticipant(roomId, userId);
    }

    // 모든 메시지 읽음 처리
    @Transactional
    public void markAllMessagesAsRead(Long roomId, Long userId) {
        readStatusMapper.markAllAsRead(roomId, userId);
    }

    // 특정 채팅방의 읽지 않은 메시지 수 조회
    public Long getUnreadMessageCount(Long roomId, Long userId) {
        return readStatusMapper.countUnreadMessages(roomId, userId);
    }

    // 사용자의 모든 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<MyChatListResDto> getMyChatRooms(Long userId) {
        List<ChatRoom> rooms = chatMapper.findRoomsByUserId(userId);
        List<MyChatListResDto> dtos = new ArrayList<>();

        for (ChatRoom room : rooms) {
            MyChatListResDto dto = new MyChatListResDto();
            OtherUserDto otherUser = getOtherUserInfo(room.getChatRoomId());
            dto.setRoomId(room.getChatRoomId());
            dto.setRoomName(room.getName());
            dto.setOtherUserName(otherUser.getName());

            // 마지막 메시지 정보 설정
            ChatMessage lastMessage = chatMapper.findLastMessageByRoomId(room.getChatRoomId());
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getCreatedTime());
            } else {
                dto.setLastMessage("메시지 없음");
            }

            // 읽지 않은 메시지 수 설정
            dto.setUnReadCount(getUnreadMessageCount(room.getChatRoomId(), userId));

            // 상대방 프로필 이미지 설정
            dto.setOtherUserProfileImg(otherUser.getProfileImg());

            dtos.add(dto);
        }

        // 마지막 메시지 시간 기준 내림차순 정렬
        dtos.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return dtos;
    }

    // 특정 게시글에 대한 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<MyChatListResDto> getBoardChatRooms(Long boardId, Long userId) {
        List<ChatRoom> rooms = chatMapper.findRoomsByBoardId(boardId);
        List<MyChatListResDto> dtos = new ArrayList<>();


        for (ChatRoom room : rooms) {
            // 사용자가 참여한 채팅방만 표시
            if (!chatMapper.existsParticipant(room.getChatRoomId(), userId)) {
                continue;
            }

            OtherUserDto otherUser = getOtherUserInfo(room.getChatRoomId());

            MyChatListResDto dto = new MyChatListResDto();
            dto.setRoomId(room.getChatRoomId());
            dto.setRoomName(room.getName());
            dto.setOtherUserName(otherUser.getName());

            // 마지막 메시지 정보 설정
            ChatMessage lastMessage = chatMapper.findLastMessageByRoomId(room.getChatRoomId());
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getCreatedTime());
            } else {
                dto.setLastMessage("메시지 없음");
            }

            // 읽지 않은 메시지 수 설정
            dto.setUnReadCount(getUnreadMessageCount(room.getChatRoomId(), userId));

            // 상대방 프로필 이미지 설정
            dto.setOtherUserProfileImg(otherUser.getProfileImg());

            dtos.add(dto);

        }

        // 마지막 메시지 시간 기준 내림차순 정렬
        dtos.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return dtos;
    }

    // 대화 내역 조회
    // 대화 내역 조회 - 이 부분을 수정
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(Long roomId) {
        Long memberId = getLoginUser().getUserId();

        // 참여자 여부 확인
        boolean isParticipant = chatMapper.existsParticipant(roomId, memberId);
        if (!isParticipant) {
            return Collections.emptyList();
        }

        // 채팅 메시지 조회
        List<ChatMessage> chatMessages = chatMapper.findChatMessagesByRoomId(roomId);
        List<ChatMessageDto> dtos = new ArrayList<>();

        for (ChatMessage msg : chatMessages) {
            if (msg == null || msg.getUserId() == null) continue;

            String senderName = userMapper.findNameById(msg.getUserId());

            // null 체크
            String displayName = senderName != null ? senderName : "Unknown";

            // 여기가 중요 변경 부분: userId와 senderId를 모두 설정
            ChatMessageDto dto = ChatMessageDto.builder()
                    .messageId(msg.getMessageId())
                    .content(msg.getContent())
                    .nickname(displayName)
                    .senderId(msg.getUserId())   // 이 필드도 추가 (일관성을 위해)
                    .createdTime(msg.getCreatedTime())
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    // 상대방 정보 조회
    @Transactional(readOnly = true)
    public OtherUserDto getOtherUserInfo(Long roomId) {
        Long myId = getLoginUser().getUserId();
        List<ChatParticipant> participants = chatMapper.findParticipantsByRoomId(roomId);

        // 참여자 목록이 비어있으면 기본 정보 반환
        if (participants == null || participants.isEmpty()) {
            return new OtherUserDto(0L, "알 수 없음", "unknown@example.com", "");
        }

        // null이 아닌 참여자 필터링
        List<ChatParticipant> validParticipants = participants.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 내가 아닌 참여자 찾기
        Optional<ChatParticipant> otherParticipantOpt = validParticipants.stream()
                .filter(p -> !myId.equals(p.getUserId()))
                .findFirst();

        if (otherParticipantOpt.isEmpty()) {
            return new OtherUserDto(0L, "상대방 없음", "no-user@example.com", "");
        }

        ChatParticipant otherParticipant = otherParticipantOpt.get();
        Long otherUserId = otherParticipant.getUserId();
        String name = userMapper.findNameById(otherUserId);
        String email = userMapper.findEmailByUserId(otherUserId);
        String profileImg = userMapper.findUserByEmail(email).getProfileImg();

        return new OtherUserDto(otherUserId, name != null ? name : "Unknown", email, "");
    }

    // 메시지 전송 시 호출 - 상대방에게 읽지 않음 상태 생성
    @Transactional
    public void sendMessage(ChatMessage message) {
        // 메시지 저장
        chatMapper.insertChatMessage(message);

        // 메시지 수신자에 대한 읽지 않은 메시지 상태 생성
        List<ChatParticipant> participants = chatMapper.findParticipantsByRoomId(message.getChatRoomId());
        for (ChatParticipant participant : participants) {
            if (!participant.getUserId().equals(message.getUserId())) { // 발신자가 아닌 참여자만
                readStatusMapper.createUnreadStatus(
                        message.getChatRoomId(),
                        participant.getUserId(),
                        message.getMessageId()
                );
            }
        }
    }

    // 채팅방의 참여자들 조회
    public List<ChatParticipant> getChatRoomParticipants(Long roomId) {
        return chatMapper.findParticipantsByRoomId(roomId);
    }
}