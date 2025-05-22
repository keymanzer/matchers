package kr.or.kosa.chat.controller;

import kr.or.kosa.chat.dto.ChatMessageDto;
import kr.or.kosa.chat.dto.MyChatListResDto;
import kr.or.kosa.chat.dto.OtherUserDto;
import kr.or.kosa.chat.service.ChatService;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    // 내 채팅방 목록 페이지
    @GetMapping("/chat/my/rooms")
    public String getMyChatRooms(Model model) {
        CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MyChatListResDto> chatRooms = chatService.getMyChatRooms(user.getUserId());
        model.addAttribute("chatRooms", chatRooms);
        return "chat/my-rooms";
    }

    // 특정 게시글의 채팅방 목록 페이지
    @GetMapping("/chat/{boardId}/rooms")
    public String getBoardChatRooms(@PathVariable Long boardId, Model model) {
        CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MyChatListResDto> chatRooms = chatService.getBoardChatRooms(boardId, user.getUserId());
        model.addAttribute("boardId", boardId);
        model.addAttribute("chatRooms", chatRooms);
        return "chat/board-rooms";
    }

    // 채팅방 생성 페이지
    @GetMapping("/chat/create")
    public String showCreateChatRoomForm() {
        return "chat/create-room";
    }

    // 채팅방 페이지
    @GetMapping("/chat/room/{roomId}")
    public String chatRoom(@PathVariable Long roomId, Model model) {
        CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUserId();

        // 채팅방 참여자 확인
        if (!chatService.isRoomParticipant(userId, roomId)) {
            return "redirect:/chat/my/rooms?error=unauthorized";
        }

        // 입장 시 모든 메시지 읽음 처리
        chatService.markAllMessagesAsRead(roomId, userId);

        model.addAttribute("roomId", roomId);
        return "chat/room";
    }

    // 내가 참여중인 채팅방 목록 반환 API
    @GetMapping("/chat/my/rooms/api")
    @ResponseBody
    public List<MyChatListResDto> getMyChatRoomsApi() {
        CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getMyChatRooms(user.getUserId());
    }

    // 게시판 관련 채팅방 목록 반환 API
    @GetMapping("/chat/{boardId}/rooms/api")
    @ResponseBody
    public List<MyChatListResDto> getBoardChatRoomsApi(@PathVariable Long boardId) {
        CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getBoardChatRooms(boardId, user.getUserId());
    }

    // 채팅방 생성 API
    @PostMapping("/chat/create")
    public String createChatRoom(@RequestParam Long boardId, @RequestParam Long userId) {
        try {
            log.info("채팅방 생성 요청: boardId={}, userId={}", boardId, userId);
            Long roomId = chatService.getOrCreateChatRoom(boardId, userId);
            log.info("채팅방 생성 완료: roomId={}", roomId);
            return "redirect:/chat/room/" + roomId;
        } catch (Exception e) {
            log.error("채팅방 생성 실패: {}", e.getMessage(), e);
            return "redirect:/chat/create";
        }
    }

    // 채팅 내역 조회 API
    @GetMapping("/chat/history/{roomId}")
    @ResponseBody
    public List<ChatMessageDto> getChatHistory(@PathVariable Long roomId) {
        return chatService.getChatHistory(roomId);
    }

    // 상대방 정보 조회 API
    @GetMapping("/chat/room/{roomId}/other-user")
    @ResponseBody
    public OtherUserDto getOtherUserInfo(@PathVariable Long roomId) {
        return chatService.getOtherUserInfo(roomId);
    }

    // 채팅방 입장 시 메시지 읽음 처리 API
    @PostMapping("/chat/room/{roomId}/enter")
    @ResponseBody
    public ResponseEntity<Void> enterChatRoom(@PathVariable Long roomId) {
        CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        chatService.markAllMessagesAsRead(roomId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    // 읽지 않은 메시지 개수 조회 API
    @GetMapping("/chat/room/{roomId}/unread")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long roomId) {
        try {
            CustomUser user = (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long count = chatService.getUnreadMessageCount(roomId, user.getUserId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("읽지 않은 메시지 수 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(0L); // 오류 발생 시 0으로 반환
        }
    }
}