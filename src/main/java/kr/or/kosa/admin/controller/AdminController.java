package kr.or.kosa.admin.controller;

import java.util.List;

import kr.or.kosa.user.dto.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.kosa.admin.dto.ExpertCategory;
import kr.or.kosa.admin.dto.ExpertDetail;
import kr.or.kosa.admin.dto.ExpertLicense;
import kr.or.kosa.admin.dto.ExpertLocation;
import kr.or.kosa.admin.service.AdminService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

	private final AdminService adminService;

	@GetMapping
	public String index() {
		return "admin/index";
	}

	@GetMapping("/list")
	@ResponseBody
	public List<ExpertDetail> list() {
		List<ExpertDetail> list = adminService.getExpertList();
		return list;
	}
	
	@GetMapping("/licenses")
	@ResponseBody
    public List<ExpertLicense> getLicenses(@RequestParam("userId") Long userId) {
        return adminService.getLicenses(userId);
    }

    @GetMapping("/categories")
    @ResponseBody
    public List<ExpertCategory> getCategories(@RequestParam("userId") Long userId) {
        return adminService.getCategories(userId);
    }

    @GetMapping("/locations")
    @ResponseBody
    public List<ExpertLocation> getLocations(@RequestParam("userId") Long userId) {
		return adminService.getLocations(userId);
    }

	@PostMapping("/approve")
	@ResponseBody
	public String approveExpert(@RequestParam("email") String email, @AuthenticationPrincipal CustomUser customUser) {
		Long senderId = customUser.getUserId();
		adminService.approveExpertByEmail(email, senderId);
		return "승인 완료";
	}
}
