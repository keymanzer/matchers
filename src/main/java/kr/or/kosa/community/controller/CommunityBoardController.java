package kr.or.kosa.community.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	
	// 업로드 파일 다운로드
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId) {
        try {
            // 파일 정보 조회
            AttachedFile file = attachedFileService.findByAttachedFileId(fileId);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            String s3Url = file.getPath(); // S3 URL
            String originalFileName = file.getName(); // DB에 저장된 원본 파일명

            // 파일 이름을 URL 인코딩 처리
            String encodedFileName = URLEncoder.encode(originalFileName, "UTF-8")
                    .replaceAll("\\+", "%20"); // '+'를 공백으로 변경

            // S3 URL에서 key 추출
            String s3Key = extractS3KeyFromUrl(s3Url);

            // S3에서 파일 스트림 가져오기
            Resource resource = s3Service.downloadFile(s3Key);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(resource);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * S3 URL에서 key 부분을 추출
     * 예: https://inswave-2th-project-bucket.s3.ap-northeast-2.amazonaws.com/quotation_Board/파일명
     * -> quotation_Board/파일명
     */
    private String extractS3KeyFromUrl(String s3Url) {
        try {
            String[] parts = s3Url.split(".amazonaws.com/");
            if (parts.length > 1) {
                return parts[1];
            }
            throw new IllegalArgumentException("Invalid S3 URL format");
        } catch (Exception e) {
            throw new RuntimeException("S3 URL 파싱 실패: " + s3Url, e);
        }
    }

}
