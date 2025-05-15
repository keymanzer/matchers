package kr.or.kosa.community.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.service.CommunityBoardService;
import kr.or.kosa.user.dto.CustomUser;
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
		return "community/postlist";
	}
	
	@GetMapping("/{postId}")
	public String getPostbyId(@PathVariable("postId") Long postId, Model model) {
		CommunityBoard post = communityBoardService.getPostbyId(postId);
		model.addAttribute("post", post);
		return "community/postdetail";
	}
	
	@GetMapping("/insert")
	public String insertPost(Model model, Principal principal) {
	    if (principal instanceof CustomUser) {
	        CustomUser user = (CustomUser) principal;
	    }

	    CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();

	    String nickname = user.getNickname();
	    String email = user.getUsername();
		CommunityBoard post = new CommunityBoard();
	
	    model.addAttribute("nickname", nickname);
	    model.addAttribute("email", email);
		model.addAttribute("post", post);
	
		return "community/postinsert";
	}
	
	@PostMapping
	public String insertPost(@ModelAttribute CommunityBoard post) {
		communityBoardService.insertPost(post);
		return "redirect:/user/community";
	}
	
	@GetMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, Model model) {
		CommunityBoard post = communityBoardService.getPostbyId(postId);
		model.addAttribute("post", post);
		return "community/postupdate";
	}
	
	@PostMapping("/{postId}")
	public String updatePost(@PathVariable("postId") Long postId, @ModelAttribute CommunityBoard post) {
		post.setPostId(postId);
		communityBoardService.updatePost(post);
		return "redirect:/user/community";
	}
	
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable("postId") Long postId) {	
    	communityBoardService.deletePost(postId);
        return "redirect:/user/community";
    }

	
}
