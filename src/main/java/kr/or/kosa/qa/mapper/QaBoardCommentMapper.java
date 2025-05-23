package kr.or.kosa.qa.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.kosa.qa.dto.QaBoardComment;

@Mapper
public interface QaBoardCommentMapper {

    List<QaBoardComment> getCommentListByPostId(Long postId);

    QaBoardComment getCommentById(Long commentId);
    
    void insertComment(QaBoardComment comment);

    void updateComment(QaBoardComment comment);

    void deleteComment(Long commentId);
	
}
