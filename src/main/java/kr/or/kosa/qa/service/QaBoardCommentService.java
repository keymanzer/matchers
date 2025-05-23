package kr.or.kosa.qa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kosa.comments.mapper.CommentsMapper;
import kr.or.kosa.qa.dto.QaBoardComment;
import kr.or.kosa.qa.mapper.QaBoardCommentMapper;

@Service
public class QaBoardCommentService {

	@Autowired
	private QaBoardCommentMapper qaBoardCommentMapper;
	
	@Autowired
    private CommentsMapper commentsMapper;
	
    // 게시글에 해당하는 전체 댓글 조회
    public List<QaBoardComment> getCommentListByPostId(Long postId) {
        return qaBoardCommentMapper.getCommentListByPostId(postId);
    }
    
    // 단일 댓글 조회
    public QaBoardComment getCommentById(Long commentId) {
    	return qaBoardCommentMapper.getCommentById(commentId);
    }
    
    // 댓글 추가
    public void insertComment(QaBoardComment comment) {
    	qaBoardCommentMapper.insertComment(comment);
    }

    // 댓글 수정
    public void updateComment(QaBoardComment comment) {
    	commentsMapper.updateComment(comment.getCommentId(), comment.getContent());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
    	qaBoardCommentMapper.deleteComment(commentId);
    	commentsMapper.deleteComment(commentId);
    }

	
}
