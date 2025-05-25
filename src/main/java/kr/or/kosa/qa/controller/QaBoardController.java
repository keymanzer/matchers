package kr.or.kosa.qa.controller;

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
	public String getPostList(Model model, @RequestParam(name = "page", defaultValue = "1") int page) {

		int pageSize = 10;
		List<QaBoard> allPosts = qaBoardService.getPostList();

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
        List<QaBoardComment> comments = qaBoardCommentService.getCommentListByPostId(postId);
        
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        
        if (principal instanceof Authentication) {
            CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
            model.addAttribute("loginUserId", user.getUserId());
            model.addAttribute("loginNickname", user.getNickname());
        }
        
        System.out.println("============상세조회 완료============");
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
        
        System.out.println("============게시글 등록 완료============");
 
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
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute QaBoard post,
                             @RequestParam("attachedFile") MultipartFile[] attachedFiles,
                             @RequestParam(value = "existingFiles", required = false) List<String> existingFiles,
                             Principal principal) {

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
        
        System.out.println("============게시글 수정 완료============");

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
		 System.out.println("============게시글 삭제 완료============");
		
		 return "redirect:/user/qa";
    }
}
