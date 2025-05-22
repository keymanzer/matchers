package kr.or.kosa.chat.controller;

import kr.or.kosa.chat.dto.ChatMessageDto;
import kr.or.kosa.chat.model.ChatMessage;
import kr.or.kosa.chat.model.ChatParticipant;
import kr.or.kosa.chat.service.ChatService;
import kr.or.kosa.user.dto.CustomUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final SseController sseController;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, SseController sseController) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.sseController = sseController;
    }

    //클라이언트에서 /publish/{roomId} 형태로 메시지를 발행하면 이 메서드가 처리
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            ChatMessageDto message,
                            @Header("simpUser") Principal principal) {

        log.info("메시지 수신: roomId={}, content={}", roomId, message.getContent());

        try {
            // Principal에서 CustomUser 추출
            CustomUser user = extractCustomUser(principal);

            // 메시지에 발신자 정보 설정 (프론트엔드 식별용)
            message.setSenderEmail(user.getUsername());  // 이메일로 식별
            message.setSenderId(user.getUserId());       // ID로 식별
            message.setNickname(user.getNickname());         // 이름 설정
            message.setCreatedTime(LocalDateTime.now()); // 시간 설정

            // 메시지 저장을 위한 객체 생성
            ChatMessage chatMessage = ChatMessage.builder()
                    .chatRoomId(Long.parseLong(roomId))
                    .userId(user.getUserId())
                    .content(message.getContent())
                    .build();

            // 메시지 저장 및 읽지 않은 상태 생성
            chatService.sendMessage(chatMessage);

            // 채팅방에 메시지 브로드캐스트
            messageTemplate.convertAndSend("/topic/" + roomId, message);

            log.info("메시지 전송 완료: roomId={}, sender={}", roomId, user.getName());

            //채팅방 참가자들에게 알림 전송
            List<ChatParticipant> participants = chatService.getChatRoomParticipants(Long.parseLong(roomId));

            // 발신자를 제외한 참가자들에게만 알림을 전송
            for (ChatParticipant participant : participants) {
                if (!participant.getUserId().equals(user.getUserId())) {
                    // 각 참가자의 개인 채널로 알림 전송
                    messageTemplate.convertAndSend(
                            "/topic/chat-update/" + participant.getUserId(),
                            Map.of(
                                    "type", "NEW_MESSAGE",
                                    "roomId", roomId,
                                    "timestamp", System.currentTimeMillis()
                            )
                    );

                    // SSE 알림 전송 (DB 저장 및 실시간 알림)
                    String notificationMessage = user.getNickname() + "님이 새로운 메시지를 보냈습니다: " +
                            (message.getContent().length() > 20 ?
                                    message.getContent().substring(0, 20) + "..." :
                                    message.getContent());

                    sseController.sendNotification(
                            participant.getUserId(),  // 수신자
                            user.getUserId(),         // 발신자
                            Long.parseLong(roomId),   // 채팅방 ID
                            notificationMessage       // 알림 내용
                    );
                }
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 오류: {}", e.getMessage(), e);
            // 오류가 발생해도 기본 메시지는 전송
            messageTemplate.convertAndSend("/topic/" + roomId, message);
        }
    }

    // 메시지 읽음 상태 업데이트
    @MessageMapping("/{roomId}/read")
    public void updateReadStatus(@DestinationVariable String roomId,
                                 @Header("simpUser") Principal principal) {
        try {
            // Principal에서 CustomUser 추출
            CustomUser user = extractCustomUser(principal);
            Long userId = user.getUserId();

            log.info("읽음 상태 업데이트: roomId={}, userId={}", roomId, userId);

            // 모든 메시지를 읽음 상태로 변경
            chatService.markAllMessagesAsRead(Long.parseLong(roomId), userId);
        } catch (Exception e) {
            log.error("읽음 상태 업데이트 중 오류: {}", e.getMessage(), e);
        }
    }

    //Principal에서 CustomUser 객체를 안전하게 추출
    private CustomUser extractCustomUser(Principal principal) {
        if (!(principal instanceof Authentication)) {
            throw new IllegalStateException("인증 정보가 올바르지 않습니다");
        }

        Authentication auth = (Authentication) principal;
        Object principalObj = auth.getPrincipal();

        if (!(principalObj instanceof CustomUser)) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다");
        }

        return (CustomUser) principalObj;
    }
}