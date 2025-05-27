package kr.or.kosa.user.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import kr.or.kosa.admin.service.AdminService;
import kr.or.kosa.user.dto.CustomUser;
import kr.or.kosa.user.dto.Users;
import kr.or.kosa.user.service.PasswordResetService;
import kr.or.kosa.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	private final AdminService adminService;

	private final PasswordResetService passwordResetService;

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

	@GetMapping("/user/mypage/expert")
	public String myPageExpert(Model model, @AuthenticationPrincipal CustomUser customUser) {
		Users updatedUser = userService.findUserById(customUser.getUsername());

		refreshAuthentication(updatedUser);

		long userId = customUser.getUserId();

		model.addAttribute("expertDetail", adminService.getExpertByUserId(userId));
		model.addAttribute("licenseList", adminService.getLicenses(userId));
		model.addAttribute("categoryList", adminService.getCategories(userId));
		model.addAttribute("locationList", adminService.getLocations(userId));

		return "user/myexpertpage";
	}

	// 다른 사용자가 전문가 정보를 조회할 때
	@GetMapping("/user/mypage/expert/{expertId}")
	public String myPageExpert(Model model, @PathVariable long expertId) {

		model.addAttribute("expertDetail", adminService.getExpertByUserId(expertId));
		model.addAttribute("licenseList", adminService.getLicenses(expertId));
		model.addAttribute("categoryList", adminService.getCategories(expertId));
		model.addAttribute("locationList", adminService.getLocations(expertId));

		return "expert/expertInfo";
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

	private void refreshAuthentication(Users updatedUser) {
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

	@PostMapping("/reset-password")
	@ResponseBody
	public ResponseEntity<String> processForgotPassword(@RequestParam("email") String email,
														HttpServletRequest request) {
		String resetLink = passwordResetService.generateResetLink(email, request);
		if (resetLink == null) {
			return ResponseEntity.badRequest().body("이메일이 존재하지 않습니다.");
		}
		return ResponseEntity.ok("이메일 전송 완료");
	}

	@GetMapping("/reset-password/confirm")
	public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
		if (!passwordResetService.isValidToken(token)) {
			return "user/invalid-token";
		}
		model.addAttribute("token", token);
		return "user/reset-confirm";
	}

	@PostMapping("/reset-password/confirm")
	public String resetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword,
								Model model) {
		if (!passwordResetService.isValidToken(token)) {
			return "user/invalid-token";
		}
		Long userId = passwordResetService.getUserIdByToken(token);
		userService.updatePassword(userId, newPassword);
		passwordResetService.invalidateToken(token);
		model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
		return "user/reset-success";
	}
}