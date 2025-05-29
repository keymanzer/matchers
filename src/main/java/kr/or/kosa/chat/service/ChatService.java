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
import kr.or.kosa.quotationBoard.dto.QuotationBoard;
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
                if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {
                    dto.setLastMessage(lastMessage.getContent());
                } else if (lastMessage.getImageUrl() != null && !lastMessage.getImageUrl().isEmpty()) {
                    dto.setLastMessage("이미지를 보냈습니다");
                } else {
                    dto.setLastMessage("메시지 없음");
                }
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

        // 게시글의 현재 상태와 할당된 전문가 ID 조회
        String boardState = chatMapper.getBoardStateByBoardId(boardId);
        Long assignedExpertId = null;
        
        // 진행중 또는 완료 상태일 경우, 해당 게시글의 할당된 전문가 ID 조회
        if ("진행중".equals(boardState) || "완료".equals(boardState)) {
            assignedExpertId = chatMapper.getAssignedExpertIdByBoardId(boardId);
            System.out.println("게시글 ID: " + boardId + ", 상태: " + boardState + ", 할당된 전문가 ID: " + assignedExpertId);
        }

        for (ChatRoom room : rooms) {
            // 사용자가 참여한 채팅방만 표시
            if (!chatMapper.existsParticipant(room.getChatRoomId(), userId)) {
                continue;
            }

            OtherUserDto otherUser = getOtherUserInfo(room.getChatRoomId());
            Long otherUserId = otherUser.getUserId();

            MyChatListResDto dto = new MyChatListResDto();
            dto.setRoomId(room.getChatRoomId());
            dto.setRoomName(room.getName());
            dto.setOtherUserName(otherUser.getName());

            ChatMessage lastMessage = chatMapper.findLastMessageByRoomId(room.getChatRoomId());
            if (lastMessage != null) {
                if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty()) {
                    dto.setLastMessage(lastMessage.getContent());
                } else if (lastMessage.getImageUrl() != null && !lastMessage.getImageUrl().isEmpty()) {
                    dto.setLastMessage("이미지를 보냈습니다");
                } else {
                    dto.setLastMessage("메시지 없음");
                }
                dto.setLastMessageTime(lastMessage.getCreatedTime());
            } else {
                dto.setLastMessage("메시지 없음");
            }

            // 읽지 않은 메시지 수 설정
            dto.setUnReadCount(getUnreadMessageCount(room.getChatRoomId(), userId));

            // 상대방 프로필 이미지 설정
            dto.setOtherUserProfileImg(otherUser.getProfileImg());

            // 채팅방별 상태 설정
            if (assignedExpertId != null) {
                // 이 채팅방의 전문가가 할당된 전문가인 경우
                if (assignedExpertId.equals(otherUserId)) {
                    dto.setBoardState(boardState); // "진행중" 또는 "완료"
                } else {
                    // 이 채팅방은 선택되지 않은 전문가의 채팅방
                    dto.setBoardState("거절됨");
                }
            } else {
                // 아직 전문가가 선택되지 않은 경우
                dto.setBoardState("진행전");
            }

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
                    .imageUrl(msg.getImageUrl())
                    .senderId(msg.getUserId())
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

        return new OtherUserDto(otherUserId, name != null ? name : "Unknown", email, profileImg);
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

    @Transactional
    public boolean boardAccept(Long roomId) {
        try {
            // 로그인한 사용자 확인 (일반 사용자)
            CustomUser loginUser = getLoginUser();
            if (loginUser == null) {
                System.err.println("로그인 사용자 정보를 찾을 수 없습니다.");
                return false;
            }
            Long userId = loginUser.getUserId();
            
            // 채팅방 정보 조회
            ChatRoom chatRoom = chatMapper.findRoomById(roomId);
            if (chatRoom == null) {
                System.err.println("채팅방을 찾을 수 없음: roomId=" + roomId);
                return false;
            }
            Long boardId = chatRoom.getBoardId();
            
            // 게시글 상태 확인 (이미 진행 중인지 확인)
            String currentState = chatMapper.getBoardStateByBoardId(boardId);
            if (!"진행전".equals(currentState)) {
                System.err.println("이미 처리된 게시글입니다. 현재 상태: " + currentState);
                return false;
            }
            
            // 채팅방 참여자 조회 - 상대방(전문가)의 ID를 찾기 위함
            List<ChatParticipant> participants = chatMapper.findParticipantsByRoomId(roomId);
            List<ChatParticipant> validParticipants = participants.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 상대방(전문가) 찾기
            Optional<ChatParticipant> expertParticipantOpt = validParticipants.stream()
                    .filter(p -> !userId.equals(p.getUserId()))
                    .findFirst();
            
            if (expertParticipantOpt.isEmpty()) {
                System.err.println("전문가 정보를 찾을 수 없습니다.");
                return false;
            }
            
            // 전문가 ID 가져오기
            Long expertId = expertParticipantOpt.get().getUserId();
            
            System.out.println("수락 처리 시작: roomId=" + roomId + ", boardId=" + boardId + 
                    ", 사용자ID=" + userId + ", 전문가ID=" + expertId);
            
            // 게시글 상태 업데이트 - 전문가 ID를 expertId로 설정
            int result = chatMapper.updateBoardAccept(boardId, expertId);
            if (result > 0) {
                System.out.println("게시글 수락 성공: boardId=" + boardId + ", expertId=" + expertId);
                return true;
            } else {
                System.err.println("게시글 수락 실패: boardId=" + boardId + ", expertId=" + expertId);
                return false;
            }
        } catch (Exception e) {
            System.err.println("boardAccept 메서드 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean boardComplete(Long roomId) {
        try {
            // 1. 로그인한 사용자 확인 (일반 사용자)
            CustomUser loginUser = getLoginUser();
            if (loginUser == null) {
                System.err.println("로그인 사용자 정보를 찾을 수 없습니다.");
                return false;
            }
            Long userId = loginUser.getUserId();
            
            // 2. 채팅방 정보 조회
            ChatRoom chatRoom = chatMapper.findRoomById(roomId);
            if (chatRoom == null) {
                System.err.println("채팅방을 찾을 수 없음: roomId=" + roomId);
                return false;
            }
            Long boardId = chatRoom.getBoardId();
            
            // 3. 게시글 상태 확인 (진행 중인지 확인)
            String currentState = chatMapper.getBoardStateByBoardId(boardId);
            if (!"진행중".equals(currentState)) {
                System.err.println("완료 처리할 수 없는 게시글입니다. 현재 상태: " + currentState);
                return false;
            }
            
            // 4. 채팅방 참여자 조회 - 전문가 ID를 찾기 위함
            List<ChatParticipant> participants = chatMapper.findParticipantsByRoomId(roomId);
            List<ChatParticipant> validParticipants = participants.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 5. 전문가 참여자 찾기
            Optional<ChatParticipant> expertParticipantOpt = validParticipants.stream()
                    .filter(p -> !userId.equals(p.getUserId()))
                    .findFirst();
            
            if (expertParticipantOpt.isEmpty()) {
                System.err.println("전문가 정보를 찾을 수 없습니다.");
                return false;
            }
            
            // 6. 전문가 ID 가져오기
            Long expertId = expertParticipantOpt.get().getUserId();
            
            System.out.println("완료 처리 시작: roomId=" + roomId + ", boardId=" + boardId + 
                    ", 사용자ID=" + userId + ", 전문가ID=" + expertId);
            
            // 7. 게시글 상태 업데이트 - 전문가 ID 유지하며 완료 처리
            int result = chatMapper.updateBoardComplete(boardId, expertId);
            if (result > 0) {
                System.out.println("게시글 완료 처리 성공: boardId=" + boardId + ", expertId=" + expertId);
                return true;
            } else {
                System.err.println("게시글 완료 처리 실패: boardId=" + boardId + ", expertId=" + expertId);
                return false;
            }
        } catch (Exception e) {
            System.err.println("boardComplete 메서드 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



}