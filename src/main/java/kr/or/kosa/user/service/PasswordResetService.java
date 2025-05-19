package kr.or.kosa.user.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import kr.or.kosa.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

	private final UserMapper userMapper;

	private final MailService mailService;

	private static final int TOKEN_EXPIRATION_MINUTES = 30;

	public String generateResetLink(String email, HttpServletRequest request) {
		Long userId = findUserIdByEmail(email);
		if (userId == null)
			return null;

		String token = UUID.randomUUID().toString();
		tokenStore.put(token, new TokenInfo(userId, LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES)));

		String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
		String link = baseUrl + "/reset-password/confirm?token=" + token;
		sendResetEmail(email, link);
		return link;
	}

	public boolean isValidToken(String token) {
		TokenInfo info = tokenStore.get(token);
		return info != null && info.expiration.isAfter(LocalDateTime.now());
	}

	public Long getUserIdByToken(String token) {
		TokenInfo info = tokenStore.get(token);
		return (info != null) ? info.userId : null;
	}

	public void invalidateToken(String token) {
		tokenStore.remove(token);
	}

	private Long findUserIdByEmail(String email) {
		return userMapper.getUserId(email);
	}

	private void sendResetEmail(String toEmail, String link) {
		String subject = "비밀번호 재설정 안내";
		String content = "<p>비밀번호 재설정을 요청하셨습니다.</p>" + "<p>아래 링크를 클릭하여 새 비밀번호를 설정하세요. 이 링크는 30분간 유효합니다.</p>"
				+ "<p><a href='" + link + "'>비밀번호 재설정하기</a></p>";

		mailService.sendEmail(toEmail, subject, content);
	}

	private static class TokenInfo {
		Long userId;
		LocalDateTime expiration;

		TokenInfo(Long userId, LocalDateTime expiration) {
			this.userId = userId;
			this.expiration = expiration;
		}
	}
}
