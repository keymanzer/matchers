package kr.or.kosa.community.controller;

import java.security.Principal;
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

	    Long userId = user.getUserId();
	    String nickname = user.getNickname();
	    String email = user.getUsername();

		CommunityBoard post = new CommunityBoard();
		post.setUserId(userId);
		post.setUserNickname(nickname);
		System.out.println(post);
		
		model.addAttribute("post", post);
	
		return "community/postinsert";
	}
	
	@PostMapping
	public String insertPost(@ModelAttribute CommunityBoard post) {
		
		String title = (post.getTitle() == null) || (post.getTitle() == "") ? "제목없음" : post.getTitle();
		
		post.setTitle(title);
		communityBoardService.insertPost(post);
		return "redirect:/user/community";
	}
	
	@GetMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, Model model) {
		
		CommunityBoard post = communityBoardService.getPostbyId(postId);
	
		model.addAttribute("post", post);
		return "community/postupdate";
	}
	
	@PostMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, @ModelAttribute CommunityBoard post, Principal principal) {
		
		String title = (post.getTitle() == null) || (post.getTitle() == "") ? "제목없음" : post.getTitle();
		
		post.setTitle(title);
		post.setPostId(postId);
	    
 		if (principal instanceof CustomUser) {
	        CustomUser user = (CustomUser) principal;
	    }
	    CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
	    CommunityBoard postCheck = communityBoardService.getPostbyId(postId);
    
		if(postCheck.getBoard().getUserId() == user.getUserId()) {
			System.out.println("사용자와 작성자 일치: 수정가능합니다");
		} else {
			System.out.println("사용자와 작성자 불일치: 수정불가");
			return "redirect:/user/community";
		}

		communityBoardService.updatePost(post);
	    return "redirect:/user/community";
	}
	
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable("postId") Long postId, Principal principal) {
		
 		if (principal instanceof CustomUser) {
	        CustomUser user = (CustomUser) principal;
	    }
	    CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
	    CommunityBoard postCheck = communityBoardService.getPostbyId(postId);
    
		if(postCheck.getBoard().getUserId() == user.getUserId()) {
			System.out.println("사용자와 작성자 일치: 삭제가능합니다");
		} else {
			System.out.println("사용자와 작성자 불일치: 삭제불가");
			return "redirect:/user/community";
		}
		
    	communityBoardService.deletePost(postId);
        return "redirect:/user/community";
    }

	
}
