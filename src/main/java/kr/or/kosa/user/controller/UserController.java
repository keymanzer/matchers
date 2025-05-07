package kr.or.kosa.user.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import kr.or.kosa.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping({ "", "/" })
	public String home(Principal principal) {
		String loginId = principal != null ? principal.getName() : "guest";
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "user/login";
	}
}
