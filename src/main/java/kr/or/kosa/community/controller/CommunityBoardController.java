package kr.or.kosa.community.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.service.CommunityBoardService;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user/community")
public class CommunityBoardController {
	
	@Autowired
	private CommunityBoardService communityBoardService;

	public void setCommunityBoardService(CommunityBoardService communityBoardService) {
		this.communityBoardService = communityBoardService;
	}
	
	@GetMapping
	public String getPostList(Model model) {
		List<CommunityBoard> list = communityBoardService.getPostList();
		model.addAttribute("list", list);
		return "";
	}
	
}
