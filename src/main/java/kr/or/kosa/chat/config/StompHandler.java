package kr.or.kosa.chat.config;

import jakarta.annotation.PreDestroy;
import kr.or.kosa.chat.service.ChatService;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;

      // 세션 ID를 키로, 사용자 ID를 값으로 저장하는 맵
    private final Map<String, Long> connectedUsers = new ConcurrentHashMap<>();

    // 이미 처리된 연결 해제 세션 ID를 추적하기 위한 집합
    private final Set<String> processedDisconnects = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // 스케줄링 작업을 위한 실행기 추가
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        // 명령어 없는 경우 즉시 반환
        if (command == null) {
            return message;
        }

        log.info("[STOMP] {} 명령 수신됨", command);

        // 각 명령에 따른 처리
        switch (command) {
            case CONNECT:
                handleConnect(accessor);
                break;
            case SUBSCRIBE:
                handleSubscribe(accessor);
                break;
            case DISCONNECT:
                handleDisconnect(accessor);
                break;
            default:
                break;
        }

        return message;
    }

    // CONNECT 명령 처리
    private void handleConnect(StompHeaderAccessor accessor) {
        log.info("[STOMP] CONNECT 요청 처리 중");

        // 인증 정보 추출
        Authentication auth = getAuthenticationFromSession(accessor);
        if (auth != null) {
            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(auth);
            // STOMP 세션에 인증 정보 설정
            accessor.setUser(auth);

            // 사용자 정보 추출 및 연결 정보 저장
            if (auth.getPrincipal() instanceof CustomUser) {
                CustomUser user = (CustomUser) auth.getPrincipal();
                String sessionId = accessor.getSessionId();

                if (sessionId != null) {
                    connectedUsers.put(sessionId, user.getUserId());
                    log.info("사용자 연결 성공: userId={}, sessionId={}", user.getUserId(), sessionId);
                }
            }

            log.info("인증된 사용자: {}", auth.getName());
        } else {
            log.warn("인증 정보 없이 연결 시도");
        }
    }

    // SUBSCRIBE 명령 처리
    private void handleSubscribe(StompHeaderAccessor accessor) {
        log.info("[STOMP] SUBSCRIBE 요청 처리 중: {}", accessor.getDestination());

        // 인증 정보 추출
        Authentication auth = getAuthenticationFromSession(accessor);
        if (auth != null) {
            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(auth);
            // 정보 검증
            validateAuthentication(auth);

            // 구독 경로에 따라 다른 검증 적용
            String destination = accessor.getDestination();
            if (destination != null) {
                if (destination.startsWith("/topic/chat-update/")) {
                    // 사용자별 알림 구독은 사용자 ID만 확인
                    validateUserNotificationAccess(destination, auth);
                } else if (destination.startsWith("/topic/")) {
                    // 채팅방 구독은 참여자 여부 확인
                    validateChatRoomAccess(accessor, auth);
                }
            }
        } else {
            throw new AuthenticationServiceException("인증 정보가 없습니다");
        }
    }

    // DISCONNECT 명령 처리
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();

        // 이미 처리된 연결 해제는 무시
        if (sessionId != null && !processedDisconnects.add(sessionId)) {
            return;
        }

        if (sessionId != null && connectedUsers.containsKey(sessionId)) {
            Long userId = connectedUsers.remove(sessionId);
            log.info("[STOMP] 사용자 연결 해제 확인: userId={}, sessionId={}", userId, sessionId);

            // 일정 시간 후 processedDisconnects에서 세션 ID 제거 (메모리 관리)
            scheduledExecutor.schedule(() -> {
                processedDisconnects.remove(sessionId);
            }, 60, TimeUnit.SECONDS);
        } else {
            log.info("[STOMP] 알 수 없는 세션 연결 해제: sessionId={}", sessionId);
        }
    }


    // 세션에서 인증 정보 추출
    private Authentication getAuthenticationFromSession(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            log.info("세션 속성이 없습니다");
            return null;
        }

        Object securityContextObj = sessionAttributes.get("SPRING_SECURITY_CONTEXT");
        if (securityContextObj instanceof SecurityContext) {
            SecurityContext securityContext = (SecurityContext) securityContextObj;
            Authentication auth = securityContext.getAuthentication();
            log.info("세션에서 인증 정보 추출: {}", auth != null ? auth.getName() : "null");
            return auth;
        }

        log.info("세션에 Security Context가 없습니다");
        return null;
    }

    // 세션에서 추출한 인증 정보 검증
    private void validateAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.info("인증 실패: 인증 객체가 null이거나 인증되지 않음");
            throw new AuthenticationServiceException("로그인이 필요합니다.");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUser)) {
            log.info("사용자 정보 형식 오류: {}", principal != null ? principal.getClass().getName() : "null");
            throw new AuthenticationServiceException("사용자 정보 파싱 실패");
        }

        log.info("인증된 사용자: {}", auth.getName());
    }

    // 채팅방 접근 권한 검증
    private void validateChatRoomAccess(StompHeaderAccessor accessor, Authentication auth) {
        try {
            CustomUser user = (CustomUser) auth.getPrincipal();
            Long userId = user.getUserId();

            String destination = accessor.getDestination();
            if (destination == null) {
                throw new AuthenticationServiceException("구독 대상이 지정되지 않았습니다");
            }

            String[] parts = destination.split("/");
            if (parts.length < 3) {
                throw new AuthenticationServiceException("잘못된 구독 경로: " + destination);
            }

            Long roomId = Long.parseLong(parts[2]);

            if (!chatService.isRoomParticipant(userId, roomId)) {
                log.info("사용자 {}가 채팅방 {}에 접근 권한이 없습니다", userId, roomId);
                throw new AuthenticationServiceException("해당 채팅방에 접근 권한이 없습니다.");
            }

            log.info("사용자 {}의 채팅방 {} 접근 승인", userId, roomId);
        } catch (NumberFormatException e) {
            throw new AuthenticationServiceException("잘못된 채팅방 ID 형식입니다", e);
        } catch (ClassCastException e) {
            throw new AuthenticationServiceException("사용자 정보 변환 실패", e);
        }
    }

    // 사용자별 알림 구독 검증
    private void validateUserNotificationAccess(String destination, Authentication auth) {
        try {
            CustomUser user = (CustomUser) auth.getPrincipal();
            Long userId = user.getUserId();

            // /topic/chat-update/5 형식에서 5 추출
            String[] parts = destination.split("/");
            if (parts.length < 4) {
                throw new AuthenticationServiceException("잘못된 구독 경로: " + destination);
            }

            Long subscribedUserId = Long.parseLong(parts[3]);

            // 자신의 알림만 구독 가능
            if (!userId.equals(subscribedUserId)) {
                log.info("사용자 {}가 다른 사용자(ID: {})의 알림에 접근 시도", userId, subscribedUserId);
                throw new AuthenticationServiceException("본인의 알림만 구독할 수 있습니다");
            }

            log.info("사용자 {}의 알림 구독 승인", userId);
        } catch (NumberFormatException e) {
            throw new AuthenticationServiceException("잘못된 사용자 ID 형식입니다", e);
        }
    }

    // 애플리케이션 종료 시 ExecutorService 정리
    @PreDestroy
    public void destroy() {
        try {
            log.info("[STOMP] ScheduledExecutorService 종료");
            scheduledExecutor.shutdown();
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduledExecutor.shutdownNow();
        }
    }
}