package kr.or.kosa.expert.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.kosa.expert.dto.ExpertDto;
import kr.or.kosa.expert.dto.Location;
import kr.or.kosa.expert.service.ExpertService;
import kr.or.kosa.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user/expert")
public class ExpertController {

	private final ExpertService expertService;

	@GetMapping("/register")
	public String expertReg(Model model) {
		List<Location> locations = expertService.getLocationList();
		model.addAttribute("locations", locations);

		List<String> cities = locations.stream().map(Location::getCity).distinct().collect(Collectors.toList());
		model.addAttribute("cities", cities);

		model.addAttribute("categoryList", expertService.getCategoryList());
		return "expert/register";
	}

	@PostMapping("/register")
	public String expertReg(ExpertDto expertDto, @AuthenticationPrincipal CustomUser customUser,
			RedirectAttributes redirectAttributes) {
		expertDto.setUserId(customUser.getUserId());

		int result = expertService.expertReg(expertDto);

		if (result > 0) {
			redirectAttributes.addFlashAttribute("expertRegSuccess", true);
		} else {
			redirectAttributes.addFlashAttribute("expertRegSuccess", false);
		}

		return "redirect:/";
	}
}
