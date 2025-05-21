package kr.or.kosa.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.comments.mapper.CommentsMapper;
import kr.or.kosa.community.dto.CommunityBoard;
import kr.or.kosa.community.dto.CommunityBoardComment;
import kr.or.kosa.community.mapper.CommunityBoardCommentMapper;

@Service
public class CommunityBoardCommentService {

    @Autowired
    private CommunityBoardCommentMapper communityBoardCommentMapper;
    
    @Autowired
    private CommentsMapper commentsMapper;

    // 게시글에 해당하는 전체 댓글 조회
    public List<CommunityBoardComment> getCommentListByPostId(Long postId) {
        return communityBoardCommentMapper.getCommentListByPostId(postId);
    }
    
    // 단일 댓글 조회
    public CommunityBoardComment getCommentById(Long commentId) {
    	return communityBoardCommentMapper.getCommentById(commentId);
    }
    
    // 댓글 추가
    public void insertComment(CommunityBoardComment comment) {
    	communityBoardCommentMapper.insertComment(comment);
    }

    // 댓글 수정
    public void updateComment(CommunityBoardComment comment) {
    	commentsMapper.updateComment(comment.getCommentId(), comment.getContent());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        // 먼저 대댓글(자식) 여부 확인 후 재귀 또는 순차 삭제
        List<CommunityBoardComment> replies = communityBoardCommentMapper.getRepliesByParentId(commentId);
        for (CommunityBoardComment reply : replies) {
            communityBoardCommentMapper.deleteComment(reply.getCommentId());
            commentsMapper.deleteComment(reply.getCommentId());
        }
    	
    	communityBoardCommentMapper.deleteComment(commentId);
    	commentsMapper.deleteComment(commentId);
    }

    // 특정 댓글의 대댓글(자식 댓글) 리스트 조회
    public List<CommunityBoardComment> getRepliesByParentId(Long parentCommentId) {
        return communityBoardCommentMapper.getRepliesByParentId(parentCommentId);
    }
}
