package kr.or.kosa.chat.controller;

import kr.or.kosa.chat.dto.ChatMessageDto;
import kr.or.kosa.chat.model.ChatMessage;
import kr.or.kosa.chat.model.ChatParticipant;
import kr.or.kosa.chat.service.ChatService;
import kr.or.kosa.chat.config.Base64DecodedMultipartFile;
import kr.or.kosa.common.S3Service;
import kr.or.kosa.user.dto.CustomUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@Slf4j
@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final SseController sseController;
    private final S3Service s3Service;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, 
                          SseController sseController, S3Service s3Service) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.sseController = sseController;
        this.s3Service = s3Service;
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

            // 이미지 처리 (Base64 -> S3 업로드)
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty() 
                    && message.getImageUrl().startsWith("data:image")) {
                try {
                    String imageUrl = uploadBase64Image(message.getImageUrl(), "chat-images");
                    message.setImageUrl(imageUrl);
                } catch (Exception e) {
                    log.error("이미지 업로드 실패: {}", e.getMessage(), e);
                    // 이미지 업로드 실패 시 이미지 URL 제거
                    message.setImageUrl(null);
                }
            }

            // 메시지에 발신자 정보 설정 (프론트엔드 식별용)
            message.setSenderEmail(user.getUsername());  // 이메일로 식별
            message.setSenderId(user.getUserId());       // ID로 식별
            message.setNickname(user.getNickname());     // 이름 설정
            message.setCreatedTime(LocalDateTime.now()); // 시간 설정

            // 메시지 저장을 위한 객체 생성
            ChatMessage chatMessage = ChatMessage.builder()
                    .chatRoomId(Long.parseLong(roomId))
                    .userId(user.getUserId())
                    .content(message.getContent() != null && !message.getContent().trim().isEmpty() ? message.getContent() : null)
                    .imageUrl(message.getImageUrl())    // 이미지 URL 설정 추가
                    .build();

            // 메시지 저장 및 읽지 않은 상태 생성
            chatService.sendMessage(chatMessage);

            // 채팅방에 메시지 브로드캐스트
            messageTemplate.convertAndSend("/topic/" + roomId, message);

            log.info("메시지 전송 완료: roomId={}, sender={}", roomId, user.getName());
            log.info("최종 전송 메시지: {}", message);

            //채팅방 참가자들에게 알림 전송
            sendNotifications(roomId, message, user);

        } catch (Exception e) {
            log.error("메시지 처리 중 오류: {}", e.getMessage(), e);
            // 오류가 발생해도 기본 메시지는 전송
            messageTemplate.convertAndSend("/topic/" + roomId, message);
        }
    }

    // 참가자들에게 알림 전송하는 메서드
    private void sendNotifications(String roomId, ChatMessageDto message, CustomUser sender) {
        List<ChatParticipant> participants = chatService.getChatRoomParticipants(Long.parseLong(roomId));

        // 발신자를 제외한 참가자들에게만 알림을 전송
        for (ChatParticipant participant : participants) {
            if (!participant.getUserId().equals(sender.getUserId())) {
                // 각 참가자의 개인 채널로 알림 전송
                messageTemplate.convertAndSend(
                        "/topic/chat-update/" + participant.getUserId(),
                        Map.of(
                                "type", "NEW_MESSAGE",
                                "roomId", roomId,
                                "timestamp", System.currentTimeMillis()
                        )
                );

                // 알림 메시지 생성
                String notificationMessage;
                if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                    notificationMessage = sender.getNickname() + "님이 이미지를 보냈습니다";
                } else if (message.getContent() != null && !message.getContent().isEmpty()) {
                    notificationMessage = sender.getNickname() + "님이 새로운 메시지를 보냈습니다: " +
                            (message.getContent().length() > 20 ?
                                    message.getContent().substring(0, 20) + "..." :
                                    message.getContent());
                } else {
                    notificationMessage = sender.getNickname() + "님이 새로운 메시지를 보냈습니다";
                }

                // SSE 알림 전송
                sseController.sendNotification(
                        participant.getUserId(),  // 수신자
                        sender.getUserId(),       // 발신자
                        Long.parseLong(roomId),   // 채팅방 ID
                        notificationMessage       // 알림 내용
                );
            }
        }
    }

    // Base64 이미지 데이터를 S3에 업로드하는 메서드 >> 스톰프(웹소켓 기반 프로토콜) 에서는 일반 HTTP 요청과 다르다...
    private String uploadBase64Image(String base64Image, String folder) throws Exception {
        try {
            // Base64 헤더 제거 (예: data:image/jpeg;base64,)
            String[] parts = base64Image.split(",");
            String imageData = parts.length > 1 ? parts[1] : parts[0];
            
            // 이미지 포맷 추출 (예: image/jpeg)
            String imageFormat = "image/jpeg"; // 기본값
            if (parts.length > 1 && parts[0].contains("image")) {
                imageFormat = parts[0].split(";")[0].split(":")[1];
            }
            
            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            
            // 파일 확장자 결정
            String extension = getExtensionFromMimeType(imageFormat);
            String filename = "image_" + System.currentTimeMillis() + extension;
            
            // Base64를 MultipartFile로 변환
            MultipartFile multipartFile = new Base64DecodedMultipartFile(imageBytes, filename, imageFormat);
            
            // 기존 S3Service 활용하여 업로드
            return s3Service.uploadFile(multipartFile, folder);
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    // MIME 타입에서 파일 확장자 추출
    private String getExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            case "image/jpeg": return ".jpg";
            case "image/png": return ".png";
            case "image/gif": return ".gif";
            case "image/webp": return ".webp";
            default: return ".jpg"; // 기본값
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
            
            // 채팅방 목록 화면에 읽음 상태 업데이트 알림 전송
            // 사용자의 개인 채널로 읽음 상태 변경 알림 전송
            messageTemplate.convertAndSend(
                    "/topic/chat-update/" + userId,
                    Map.of(
                            "type", "READ_STATUS_UPDATED",
                            "roomId", roomId,
                            "timestamp", System.currentTimeMillis()
                    )
            );
            
            log.info("읽음 상태 업데이트 알림 전송: userId={}, roomId={}", userId, roomId);
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