package kr.or.kosa.user.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.kosa.user.dto.Users;
import kr.or.kosa.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping({ "", "/" })
	public String home(Principal principal) {
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "user/login";
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
}
