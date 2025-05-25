package kr.or.kosa.qa.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.qa.dto.QaBoardComment;

@Mapper
public interface QaBoardCommentMapper {

    List<QaBoardComment> getCommentListByPostId(@Param("postId") Long postId);

    QaBoardComment getCommentById(@Param("commentId") Long commentId);

    void insertComment(QaBoardComment comment);

    void updateComment(QaBoardComment comment);

    void deleteComment(@Param("commentId") Long commentId);
}