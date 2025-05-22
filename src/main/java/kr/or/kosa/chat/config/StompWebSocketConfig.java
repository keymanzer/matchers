package kr.or.kosa.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final HttpHandshakeInterceptor httpHandshakeInterceptor;

    public StompWebSocketConfig(StompHandler stompHandler, HttpHandshakeInterceptor httpHandshakeInterceptor) {
        this.stompHandler = stompHandler;
        this.httpHandshakeInterceptor = httpHandshakeInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOriginPatterns("*")
                .addInterceptors(httpHandshakeInterceptor)
                // ws:// 가 아닌 http:// 엔드포인트를 사용할 수 있게 해주는 sockJs 라이브러리를 통한 요청을 허용하는 설정.
                .withSockJS()
                .setSessionCookieNeeded(true);


    }


    //  Stomp 사용을 위한 Message Broker 설정을 해주는 메소드
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // /publish/1 형태로 메시지 발행해야 함을 설정
        // /publish 로 시작하는 url 패턴으로 메시지가 발행되면 @Controller 객체의 @MessageMapping 메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");

        // /topic/1 형태로 메시지를 수신(subscribe)해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }


    // 웹 소켓 요청(connect, subscribe, disconnect)등의 요청시에는 http header 등 http 메시지를 넣어올 수 있고,
    // 이를 interceptor를 통해 가로채, 토큰 등을 검증할 수 있음.
    // 1. Filter → WebSocketConfig (핸드셰이크 수립) → WebSocket 연결 완료.
    // 2. STOMP 메시지 수신 -> StompHandler.preSend() (ChannelInterceptor) -> @MessageMapping Controller 실행

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }

}
