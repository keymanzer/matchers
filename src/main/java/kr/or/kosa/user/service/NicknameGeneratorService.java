package kr.or.kosa.user.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class NicknameGeneratorService {
	private static final List<String> ADJECTIVES = List.of("화난", "배고픈", "게으른", "잠자는", "웃는", "우울한", "기쁜", "용감한", "어색한",
			"조용한", "활기찬", "슬픈", "천천한", "빠른", "신나는", "이상한", "쓸쓸한", "냉정한", "귀여운", "기묘한", "따뜻한", "차가운", "강력한", "약한", "고요한",
			"열정적인", "초조한", "둔한", "섬세한", "우아한");

	private static final List<String> NOUNS = List.of("비버", "토끼", "여우", "고양이", "늑대", "호랑이", "사자", "기린", "코끼리", "곰",
			"판다", "다람쥐", "부엉이", "올빼미", "앵무새", "독수리", "펭귄", "돌고래", "상어", "두더지", "다람쥐", "수달", "사슴", "개구리", "용", "해마",
			"햄스터", "고슴도치", "거북이", "두루미");

	private final Random random = new SecureRandom();

	public String generateNickname() {
		String nickname = buildNickname();

		// 닉네임 중복 검사

		return nickname;
	}

	private String buildNickname() {
		String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
		String noun = NOUNS.get(random.nextInt(NOUNS.size()));
		int suffix = random.nextInt(10000);

		return new StringBuilder().append(adjective).append(noun).append(suffix).toString();
	}
}