package kr.or.kosa.qa.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.kosa.qa.dto.QaBoardComment;
import kr.or.kosa.qa.service.QaBoardCommentService;
import kr.or.kosa.user.dto.CustomUser;

@Controller
@RequestMapping("/user/qa/comments")
public class QaBoardCommentController {

    @Autowired
    private QaBoardCommentService qaBoardCommentService;

    @PostMapping("/insert")
    public String insertComment(@ModelAttribute QaBoardComment comment, Principal principal) {

      if (principal instanceof CustomUser) {
         CustomUser user = (CustomUser) principal;
      }
      CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
        comment.setUserNickname(user.getNickname());
        qaBoardCommentService.insertComment(comment);
        System.out.println("=========댓글 또는 대댓글 등록했습니다========");

        return "redirect:/user/qa/" + comment.getPostId() + "/detail";
    }
    
    @PostMapping("/{commentId}/update")
    public String updateComment(@PathVariable("commentId") Long commentId,
                                @ModelAttribute QaBoardComment updatedComment,
                                Principal principal) {

       CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
       
       QaBoardComment existingComment = qaBoardCommentService.getCommentById(commentId);
        
        if (!existingComment.getUserNickname().equals(user.getNickname())) {
            System.out.println("사용자와 작성자 불일치: 수정불가");
            return "redirect:/user/qa";
        }
        
        System.out.println("사용자와 작성자 일치: 수정가능합니다");
        
        existingComment.setContent(updatedComment.getContent());
        
        qaBoardCommentService.updateComment(existingComment);

        return "redirect:/user/qa/" + updatedComment.getPostId() + "/detail";
    }
    
    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable("commentId") Long commentId,
                                 @RequestParam("postId") Long postId,
                                 Principal principal) {

        CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
        QaBoardComment comment = qaBoardCommentService.getCommentById(commentId);

        if (!comment.getUserNickname().equals(user.getNickname())) {
            System.out.println("사용자와 작성자 불일치: 수정불가");
            return "redirect:/user/qa";
        }
        
        System.out.println("사용자와 작성자 일치: 수정가능합니다");
       
       
        qaBoardCommentService.deleteComment(commentId);
        System.out.println("=========댓글 삭제했습니다========");

        return "redirect:/user/qa/" + postId + "/detail";
    }
	
}
