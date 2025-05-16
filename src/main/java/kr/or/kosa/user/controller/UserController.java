package kr.or.kosa.user.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.user.dto.CustomUser;
import kr.or.kosa.user.dto.Users;
import kr.or.kosa.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping({ "", "/" })
	public String home() {
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "user/login";
	}

	@GetMapping("/user/mypage")
	public String myPage(Model model, @AuthenticationPrincipal CustomUser customUser) {
		model.addAttribute("user", customUser);
		return "user/mypage";
	}

	@PostMapping("/user/mypage/profile")
	@ResponseBody
	public Map<String, Object> uploadProfileImage(@RequestParam("profileImage") MultipartFile file,
			@AuthenticationPrincipal CustomUser customUser) {
		Map<String, Object> result = new HashMap<>();
		try {
			String newFileName = userService.saveProfileImage(file, customUser.getUserId());

			Users updatedUser = userService.findUserById(customUser.getUsername());

			refreshAuthentication(updatedUser);

			result.put("success", true);
			result.put("filename", newFileName);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "업로드 중 오류가 발생했습니다.");
		}
		return result;
	}

	@PostMapping("/user/mypage/nickname")
	@ResponseBody
	public Map<String, Object> updateNickname(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal CustomUser customUser) {

		String newNickname = payload.get("nickname");
		Map<String, Object> result = new HashMap<>();

		try {
			userService.updateNickname(customUser.getUserId(), newNickname);

			Users updatedUser = userService.findUserById(customUser.getUsername());
			refreshAuthentication(updatedUser);

			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "닉네임 변경 중 오류가 발생했습니다.");
		}

		return result;
	}

	@GetMapping("/signup")
	public String signUp() {
		return "user/signup";
	}

	@PostMapping("/signup")
	public String signUp(Users user) {
		userService.signUp(user);
		return "redirect:/login";
	}

	@GetMapping("/check-email")
	@ResponseBody
	public Map<String, Boolean> checkEmailDuplicate(@RequestParam("email") String email) {
		boolean exists = userService.emailExists(email) > 0;
		return Collections.singletonMap("duplicate", exists);
	}

	public void refreshAuthentication(Users updatedUser) {
		CustomUser newCustomUser = new CustomUser(updatedUser);

		Authentication newAuth = new UsernamePasswordAuthenticationToken(newCustomUser, newCustomUser.getPassword(),
				newCustomUser.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}

	@PostMapping("/user/mypage/verify-password")
	@ResponseBody
	public Map<String, Object> verifyPassword(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal CustomUser customUser) {
		String password = payload.get("password");
		Map<String, Object> result = new HashMap<>();

		try {
			boolean verify = userService.verifyPassword(customUser.getUserId(), password);

			if (verify) {
				result.put("success", true);
			} else {
				result.put("success", false);
				result.put("message", "현재 비밀번호가 올바르지 않습니다.");
			}
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "비밀번호가 틀립니다.");
		}
		return result;
	}

	@PostMapping("/user/mypage/change-password")
	@ResponseBody
	public Map<String, Object> changePassword(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal CustomUser customUser) {
		String newPassword = payload.get("password");
		Map<String, Object> result = new HashMap<>();

		try {
			userService.updatePassword(customUser.getUserId(), newPassword);
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
		}
		return result;
	}

	@PostMapping("/user/mypage/withdraw")
	@ResponseBody
	public Map<String, Object> withdraw(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal CustomUser customUser) {
		String password = payload.get("password");
		Map<String, Object> result = new HashMap<>();

		try {
			boolean verified = userService.verifyPassword(customUser.getUserId(), password);
			if (!verified) {
				result.put("success", false);
				result.put("message", "비밀번호가 올바르지 않습니다.");
				return result;
			}

			userService.withdrawUser(customUser.getUserId());
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "회원 탈퇴 처리 중 오류가 발생했습니다.");
		}

		return result;
	}
}
