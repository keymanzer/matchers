package kr.or.kosa.community.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.or.kosa.common.S3Service;
import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.dto.CommunityBoardComment;
import kr.or.kosa.community.service.CommunityBoardCommentService;
import kr.or.kosa.community.service.CommunityBoardService;
import kr.or.kosa.user.dto.CustomUser;

@Controller
@RequestMapping("/user/community")
public class CommunityBoardController {

	@Autowired
	private CommunityBoardService communityBoardService;

	@Autowired
	private AttachedFileService attachedFileService;

	@Autowired
	private CommunityBoardCommentService communityBoardCommentService;

	@Autowired
	private S3Service s3Service;

	public void setCommunityBoardService(CommunityBoardService communityBoardService) {
		this.communityBoardService = communityBoardService;
	}

	public void setAttachedFileService(AttachedFileService attachedFileService) {
		this.attachedFileService = attachedFileService;
	}

	public void setCommunityBoardCommentService(CommunityBoardCommentService communityBoardCommentService) {
		this.communityBoardCommentService = communityBoardCommentService;
	}

	public void setS3Service(S3Service s3Service) {
		this.s3Service = s3Service;
	}

	// 게시글 목록
	@GetMapping
	public String getPostList(Model model, @RequestParam(name = "page", defaultValue = "1") int page) {

		int pageSize = 10;
		List<CommunityBoard> allPosts = communityBoardService.getPostList();

		int totalItems = allPosts.size();
		int totalPages = (int) Math.ceil((double) totalItems / pageSize);

		if (page < 1)
			page = 1;
		if (page > totalPages && totalPages > 0)
			page = totalPages;

		int startIndex = (page - 1) * pageSize;
		int endIndex = Math.min(startIndex + pageSize, totalItems);

		List<CommunityBoard> currentPagePosts = new ArrayList<>();
		if (startIndex < totalItems) {
			currentPagePosts = allPosts.subList(startIndex, endIndex);
		}

		model.addAttribute("list", currentPagePosts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalItems", totalItems);

		return "community/postlist";
	}

	@GetMapping("/fragment/top5")
	public String getTop5PostsByViews(Model model) {
		List<CommunityBoard> allPosts = communityBoardService.getPostListByViews();
		List<CommunityBoard> top5 = allPosts.size() > 5 ? allPosts.subList(0, 5) : allPosts;
		model.addAttribute("list", top5);
		return "community/postlist :: topPostList";
	}

	// 게시글 상세 보기
	@GetMapping("/{postId}/detail")
	public String getPostbyId(@PathVariable("postId") Long postId, Model model, Principal principal) {

		// 조회수 증가
		communityBoardService.increaseViewCount(postId);

		CommunityBoard post = communityBoardService.getPostbyId(postId);
		List<CommunityBoardComment> allComments = communityBoardCommentService.getCommentListByPostId(postId);

		// 대댓글 그룹핑
		Map<Long, CommunityBoardComment> commentMap = new HashMap<>();
		List<CommunityBoardComment> topLevelComments = new ArrayList<>();

		for (CommunityBoardComment comment : allComments) {
			comment.setReplies(new ArrayList<>());
			commentMap.put(comment.getCommentId(), comment);
		}

		for (CommunityBoardComment comment : allComments) {
			if (comment.getParentCommentId() == null) {
				topLevelComments.add(comment);
			} else {
				CommunityBoardComment parent = commentMap.get(comment.getParentCommentId());
				if (parent != null) {
					parent.getReplies().add(comment);
				}
			}
		}

		model.addAttribute("post", post);
		model.addAttribute("comments", topLevelComments);

		if (principal instanceof Authentication) {
			CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
			model.addAttribute("loginUserId", user.getUserId());
			model.addAttribute("loginNickname", user.getNickname());
		}

		return "community/postdetail";
	}

	// 등록 폼 이동
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

	// 게시글 등록
	@PostMapping
	public String insertPost(@ModelAttribute CommunityBoard post,
			@RequestParam("attachedFile") MultipartFile[] attachedFiles) {

		communityBoardService.insertPost(post);

		// 첨부파일 업로드
		if (attachedFiles != null) {
			for (MultipartFile file : attachedFiles) {
				if (!file.isEmpty()) {
					// S3에 파일 업로드
					String fileUrl = null;
					try {
						// S3에 업로드
						fileUrl = s3Service.uploadFile(file, "community"); // "post"는 파일 폴더 구분용
					} catch (IOException e) {
						e.printStackTrace();
						continue; // 업로드 실패 시, 다음 파일로 넘어감
					}

					AttachedFile attachedFile = new AttachedFile();
					attachedFile.setPostId(post.getPostId());
					attachedFile.setName(file.getOriginalFilename()); // 파일 이름 저장
					attachedFile.setPath(fileUrl); // S3 URL 저장

					attachedFileService.saveAttachedFileMetadata(attachedFile);
				}
			}
		}

		return "redirect:/user/community";
	}

	// 수정 폼 이동
	@GetMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, Model model) {

		CommunityBoard post = communityBoardService.getPostbyId(postId);

		model.addAttribute("post", post);
		post.setTitle(post.getBoard().getTitle());
		post.setContent(post.getBoard().getContent());
		return "community/postupdate";
	}

	// 게시글 수정
	@PostMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, @ModelAttribute CommunityBoard post,
			@RequestParam(value = "attachedFile", required = false) MultipartFile[] attachedFiles,
			@RequestParam(value = "existingFiles", required = false) List<String> existingFiles, Principal principal) {

		CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
		CommunityBoard postCheck = communityBoardService.getPostbyId(postId);

		if (postCheck.getBoard().getUserId() != user.getUserId()) {
			System.out.println("사용자와 작성자 불일치: 수정불가");
			return "redirect:/user/community";
		}

		System.out.println("사용자와 작성자 일치: 수정가능합니다");

		// 새로 파일 업로드
		if (attachedFiles != null) {
			for (MultipartFile file : attachedFiles) {
				if (!file.isEmpty()) {
					// S3에 파일 업로드
					String fileUrl = null;
					try {
						// S3에 업로드
						fileUrl = s3Service.uploadFile(file, "community"); // "post"는 파일 폴더 구분용
					} catch (IOException e) {
						e.printStackTrace();
						continue; // 업로드 실패 시, 다음 파일로 넘어감
					}

					AttachedFile attachedFile = new AttachedFile();
					attachedFile.setPostId(post.getPostId());
					attachedFile.setName(file.getOriginalFilename()); // 파일 이름 저장
					attachedFile.setPath(fileUrl); // S3 URL 저장

					attachedFileService.saveAttachedFileMetadata(attachedFile);
				}
			}
		}

		post.setPostId(postId);
		communityBoardService.updatePost(post);

		return "redirect:/user/community";
	}

	// 게시글 삭제
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

		return "redirect:/user/community";
	}

}
