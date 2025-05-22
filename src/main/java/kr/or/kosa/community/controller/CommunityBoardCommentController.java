package kr.or.kosa.community.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.kosa.community.dto.CommunityBoardComment;
import kr.or.kosa.community.service.CommunityBoardCommentService;
import kr.or.kosa.user.dto.CustomUser;

@Controller
@RequestMapping("/user/community/comments")
public class CommunityBoardCommentController {

    @Autowired
    private CommunityBoardCommentService communityBoardCommentService;

    @PostMapping("/insert")
    public String insertComment(@ModelAttribute CommunityBoardComment comment, Principal principal) {

      if (principal instanceof CustomUser) {
         CustomUser user = (CustomUser) principal;
      }
      CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
        comment.setUserNickname(user.getNickname());
        communityBoardCommentService.insertComment(comment);
        System.out.println("=========댓글 또는 대댓글 등록했습니다========");

        return "redirect:/user/community/" + comment.getPostId();
    }
    
    @PostMapping("/{commentId}/update")
    public String updateComment(@PathVariable("commentId") Long commentId,
                                @ModelAttribute CommunityBoardComment updatedComment,
                                Principal principal) {

       CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
       
        CommunityBoardComment existingComment = communityBoardCommentService.getCommentById(commentId);
        
        if (!existingComment.getUserNickname().equals(user.getNickname())) {
            System.out.println("사용자와 작성자 불일치: 수정불가");
            return "redirect:/user/community";
        }
        
        System.out.println("사용자와 작성자 일치: 수정가능합니다");
        
        existingComment.setContent(updatedComment.getContent());
        
        communityBoardCommentService.updateComment(existingComment);

        return "redirect:/user/community/" + updatedComment.getPostId();
    }
    
    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable("commentId") Long commentId,
                                 @RequestParam("postId") Long postId,
                                 Principal principal) {

        CustomUser user = (CustomUser) ((Authentication) principal).getPrincipal();
        CommunityBoardComment comment = communityBoardCommentService.getCommentById(commentId);

        if (!comment.getUserNickname().equals(user.getNickname())) {
            System.out.println("사용자와 작성자 불일치: 수정불가");
            return "redirect:/user/community";
        }
        
        System.out.println("사용자와 작성자 일치: 수정가능합니다");
       
       
       communityBoardCommentService.deleteComment(commentId);
        System.out.println("=========댓글 삭제했습니다========");

        return "redirect:/user/community/" + postId;
    }
    
}
