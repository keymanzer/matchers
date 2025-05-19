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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.attachedFile.dto.AttachedFile;
import kr.or.kosa.attachedFile.service.AttachedFileService;
import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.service.CommunityBoardService;
import kr.or.kosa.user.dto.CustomUser;

@Controller
@RequestMapping("/user/community")
public class CommunityBoardController {

	@Autowired
	private CommunityBoardService communityBoardService;

	@Autowired
	private AttachedFileService attachedFileService;

	public void setCommunityBoardService(CommunityBoardService communityBoardService) {
		this.communityBoardService = communityBoardService;
	}

	public void setAttcheAttachedFileService(AttachedFileService attachedFileService) {
		this.attachedFileService = attachedFileService;
	}

	@GetMapping
	public String getPostList(Model model) {

		List<CommunityBoard> list = communityBoardService.getPostList();
		model.addAttribute("list", list);
		System.out.println("============전체목록조회 완료============");
		return "community/postlist";
	}

	@GetMapping("/{postId}")
	public String getPostbyId(@PathVariable("postId") Long postId, Model model) {

		CommunityBoard post = communityBoardService.getPostbyId(postId);
		model.addAttribute("post", post);
		System.out.println("============상세조회 완료============");
		return "community/postdetail";
	}

	@GetMapping("/insert")
	public String insertPost(Model model, Principal principal) {

		if (principal instanceof CustomUser) {
			CustomUser user = (CustomUser) principal;
		}
		CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();

		CommunityBoard post = new CommunityBoard();
		post.setUserId(user.getUserId());
		post.setUserNickname(user.getNickname());

		model.addAttribute("post", post);

		return "community/postinsert";
	}

	@PostMapping
	public String insertPost(@ModelAttribute CommunityBoard post,
			@RequestParam("attachedFile") MultipartFile[] attachedFiles) {

		// 제목이 Null이거나 빈값일 경우 default 제목은 "제목없음"
		String title = (post.getTitle() == null || post.getTitle().trim().isEmpty()) ? "제목없음" : post.getTitle();
		post.setTitle(title);
		communityBoardService.insertPost(post);

		// Board 객체 값에 있는 postId CommunityBoard 객체에 복사
		post.setPostId(post.getBoardSeq());

		// 첨부파일 업로드
		if (attachedFiles != null) {
			for (MultipartFile file : attachedFiles) {

				AttachedFile attachedFile = new AttachedFile();
				attachedFile.setName(file.getOriginalFilename()); // 파일 이름
				attachedFile.setPath("/upload/" + file.getOriginalFilename()); // 예시 경로 (파일 저장 위치)
				attachedFile.setPostId(post.getBoardSeq().intValue());

				attachedFileService.saveAttachedFileMetadata(attachedFile);

			}
		}
		System.out.println("============게시글 등록 완료============");

		return "redirect:/user/community";
	}

	@GetMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, Model model) {

		CommunityBoard post = communityBoardService.getPostbyId(postId);

		model.addAttribute("post", post);
		post.setTitle(post.getBoard().getTitle());
		post.setContent(post.getBoard().getContent());
		return "community/postupdate";
	}

    @PostMapping("/{postId}/update")
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute CommunityBoard post,
                             @RequestParam("attachedFile") MultipartFile[] attachedFiles,
                             @RequestParam(value = "existingFiles", required = false) List<String> existingFiles,
                             Principal principal) {

        CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
        CommunityBoard postCheck = communityBoardService.getPostbyId(postId);

        if (postCheck.getBoard().getUserId() != user.getUserId()) {
            System.out.println("사용자와 작성자 불일치: 수정불가");
            return "redirect:/user/community";
        }

        System.out.println("사용자와 작성자 일치: 수정가능합니다");
        
        // 전체 파일 삭제 후 다시 저장
        attachedFileService.deleteAttachedFilesByPostId(postId.intValue());

        // 기존 유지 파일 다시 등록
        if (existingFiles != null) {
            for (String fileName : existingFiles) {
                AttachedFile existingFile = new AttachedFile();
                existingFile.setName(fileName);
                existingFile.setPath("/upload/" + fileName);
                existingFile.setPostId(postId.intValue());
                attachedFileService.saveAttachedFileMetadata(existingFile);
            }
        }

        // 새로 업로드된 파일 등록
        if (attachedFiles != null) {
            for (MultipartFile file : attachedFiles) {
                if (!file.isEmpty()) {
                    AttachedFile newFile = new AttachedFile();
                    newFile.setName(file.getOriginalFilename());
                    newFile.setPath("/upload/" + file.getOriginalFilename());
                    newFile.setPostId(postId.intValue());
                    attachedFileService.saveAttachedFileMetadata(newFile);
                }
            }
        }

        // 제목 비었을 경우 기본값 처리
        String title = (post.getTitle() == null || post.getTitle().trim().isEmpty()) ? "제목없음" : post.getTitle();
        post.setTitle(title);

        post.setPostId(postId); // 누락 방지

        communityBoardService.updatePost(post);
        System.out.println("============게시글 수정 완료============");

        return "redirect:/user/community";
    }

	@PostMapping("/{postId}/delete")
	public String deletePost(@PathVariable("postId") Long postId, Principal principal) {

		if (principal instanceof CustomUser) {
			CustomUser user = (CustomUser) principal;
		}
		CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
		CommunityBoard postCheck = communityBoardService.getPostbyId(postId);

		if (postCheck.getBoard().getUserId() == user.getUserId()) {
			System.out.println("사용자와 작성자 일치: 삭제가능합니다");
		} else {
			System.out.println("사용자와 작성자 불일치: 삭제불가");
			return "redirect:/user/community";
		}

		communityBoardService.deletePost(postId);
		System.out.println("============게시글 삭제 완료============");

		return "redirect:/user/community";
	}

}
