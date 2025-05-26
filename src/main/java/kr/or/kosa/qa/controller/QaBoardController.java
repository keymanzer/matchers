package kr.or.kosa.qa.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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
import kr.or.kosa.qa.dto.QaBoard;
import kr.or.kosa.qa.dto.QaBoardComment;
import kr.or.kosa.qa.service.QaBoardCommentService;
import kr.or.kosa.qa.service.QaBoardService;
import kr.or.kosa.user.dto.CustomUser;

@Controller
@RequestMapping("/user/qa")
public class QaBoardController {

	@Autowired
	private QaBoardService qaBoardService;

	@Autowired
	private AttachedFileService attachedFileService;

	@Autowired
	private QaBoardCommentService qaBoardCommentService;

	@Autowired
	private S3Service s3Service;

	public void setQaBoardService(QaBoardService qaBoardService) {
		this.qaBoardService = qaBoardService;
	}

	public void setAttachedFileService(AttachedFileService attachedFileService) {
		this.attachedFileService = attachedFileService;
	}

	public void setQaBoardCommentService(QaBoardCommentService qaBoardCommentService) {
		this.qaBoardCommentService = qaBoardCommentService;
	}

	public void setS3Service(S3Service s3Service) {
		this.s3Service = s3Service;
	}

	// 게시글 목록
	@GetMapping
	public String getPostList(Model model, @RequestParam(name = "page", defaultValue = "1") int page,
			Principal principal) {
		int pageSize = 10;
		List<QaBoard> allPosts = qaBoardService.getPostList();

		// 로그인 사용자
		CustomUser loginUser = null;
		boolean isAdmin = false;
		if (principal instanceof Authentication) {
			loginUser = (CustomUser) ((Authentication) principal).getPrincipal();
			isAdmin = loginUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
		}

		// 제목 가공: 비공개글은 작성자 or 관리자 외에는 "비공개글입니다"로 표시
		for (QaBoard post : allPosts) {
			if ("N".equals(post.getVisible())) {
				boolean isOwner = loginUser != null && loginUser.getUserId().equals(post.getUserId());
				if (!isOwner && !isAdmin) {
					post.getBoard().setTitle("🔒 비공개글입니다");
				}
			}
		}

		// 페이징 처리
		int totalItems = allPosts.size();
		int totalPages = (int) Math.ceil((double) totalItems / pageSize);

		if (page < 1)
			page = 1;
		if (page > totalPages && totalPages > 0)
			page = totalPages;

		int startIndex = (page - 1) * pageSize;
		int endIndex = Math.min(startIndex + pageSize, totalItems);

		List<QaBoard> currentPagePosts = new ArrayList<>();
		if (startIndex < totalItems) {
			currentPagePosts = allPosts.subList(startIndex, endIndex);
		}

		model.addAttribute("list", currentPagePosts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalItems", totalItems);

		return "qa/postlist";
	}

	@GetMapping("/fragment/top5")
	public String getTop5PostsByViews(Model model) {
		List<QaBoard> allPosts = qaBoardService.getPostListByViews();
		List<QaBoard> top5 = allPosts.size() > 5 ? allPosts.subList(0, 5) : allPosts;
		model.addAttribute("list", top5);
		return "community/postlist :: topPostList";
	}

	// 게시글 상세 보기
	@GetMapping("/{postId}/detail")
	public String getPostbyId(@PathVariable("postId") Long postId, Model model, Principal principal) {
	    QaBoard post = qaBoardService.getPostbyId(postId);

	    // 로그인 사용자 정보 조회
	    CustomUser loginUser = null;
	    boolean isAdmin = false;

	    if (principal instanceof Authentication) {
	        loginUser = (CustomUser) ((Authentication) principal).getPrincipal();
	        isAdmin = loginUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
	        model.addAttribute("loginUserId", loginUser.getUserId());
	        model.addAttribute("loginNickname", loginUser.getNickname());
	    }

	    // 비공개 게시글 접근 제한
	    if ("N".equals(post.getVisible())) {
	        boolean isOwner = loginUser != null && loginUser.getUserId().equals(post.getUserId());
	        if (!isOwner && !isAdmin) {
	            return "redirect:/user/qa";
	        }
	    }
	    //조회 수 증가
	    qaBoardService.increaseViewCount(postId);

	    List<QaBoardComment> comments = qaBoardCommentService.getCommentListByPostId(postId);
	    model.addAttribute("post", post);
	    model.addAttribute("comments", comments);

	    return "qa/postdetail";
	}

	// 등록 폼 이동
	@GetMapping("/insert")
	public String insertPost(Model model, Principal principal) {

		if (principal instanceof CustomUser) {
			CustomUser user = (CustomUser) principal;
		}
		CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();

		QaBoard post = new QaBoard();
		post.setUserId(user.getUserId());
		post.setUserNickname(user.getNickname());
		model.addAttribute("post", post);

		return "qa/postinsert";
	}

	// 게시글 등록
	@PostMapping
	public String insertPost(@ModelAttribute QaBoard post,
			@RequestParam("attachedFile") MultipartFile[] attachedFiles) {

		// 제목이 Null이거나 빈값일 경우 default 제목은 "제목없음"
		String title = (post.getTitle() == null || post.getTitle().trim().isEmpty()) ? "제목없음" : post.getTitle();
		post.setTitle(title);
		qaBoardService.insertPost(post);

		// 첨부파일 업로드
		if (attachedFiles != null) {
			for (MultipartFile file : attachedFiles) {
				if (!file.isEmpty()) {
					// S3에 파일 업로드
					String fileUrl = null;
					try {
						// S3에 업로드
						fileUrl = s3Service.uploadFile(file, "qa"); // "post"는 파일 폴더 구분용
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

		return "redirect:/user/qa";
	}

	// 수정 폼 이동
	@GetMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, Model model) {
		QaBoard post = qaBoardService.getPostbyId(postId);

		model.addAttribute("post", post);
		post.setTitle(post.getBoard().getTitle());
		post.setContent(post.getBoard().getContent());
		return "qa/postupdate";
	}

	// 게시글 수정
	@PostMapping("/{postId}/update")
	public String updatePost(@PathVariable("postId") Long postId, @ModelAttribute QaBoard post,
			@RequestParam("attachedFile") MultipartFile[] attachedFiles,
			@RequestParam(value = "existingFiles", required = false) List<String> existingFiles, Principal principal) {

		CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
		QaBoard postCheck = qaBoardService.getPostbyId(postId);

		if (postCheck.getBoard().getUserId() != user.getUserId()) {
			System.out.println("사용자와 작성자 불일치: 수정불가");
			return "redirect:/user/qa";
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
						fileUrl = s3Service.uploadFile(file, "qa"); // "post"는 파일 폴더 구분용
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

		// 제목 비었을 경우 기본값 처리
		String title = (post.getTitle() == null || post.getTitle().trim().isEmpty()) ? "제목없음" : post.getTitle();
		post.setTitle(title);
		post.setPostId(postId);
		qaBoardService.updatePost(post);

		return "redirect:/user/qa";
	}

	// 게시글 삭제
	@PostMapping("/{postId}/delete")
	public String deletePost(@PathVariable("postId") Long postId, Principal principal) {

		if (principal instanceof CustomUser) {
			CustomUser user = (CustomUser) principal;
		}
		CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
		QaBoard postCheck = qaBoardService.getPostbyId(postId);

		if (postCheck.getBoard().getUserId() == user.getUserId()) {
			System.out.println("사용자와 작성자 일치: 삭제가능합니다");
		} else {
			System.out.println("사용자와 작성자 불일치: 삭제불가");
		}

		qaBoardService.deletePost(postId);

		return "redirect:/user/qa";
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
