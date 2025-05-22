package kr.or.kosa.chat.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

// WebSocket 세션으로의 SecurityContext 복사는 WebSocket 연결 인증용
// WebSocket 연결 시, HttpSession을 통해 세션 ID를 가져오고, SecurityContext를 WebSocket 속성으로 전달하는 인터셉터

@Component
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession();

            System.out.println("세션 ID : " + session.getId() );
            attributes.put("sessionId", session.getId());

            // Spring Security 컨텍스트 정보를 WebSocket 속성으로 전달 -> StompHandler 에서 사용
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                attributes.put("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

}
