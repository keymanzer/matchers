package kr.or.kosa.comments.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.comments.dto.Comments;

@Mapper
public interface CommentsMapper {

    List<Comments> getCommentsByPostId(Long postId);

    Comments getCommentById(Long commentId);

    // 댓글 등록
    void insertComment(Comments comment);

    // 댓글 수정
    void updateComment(@Param("commentId") Long postId, @Param("content") String content);

    // 댓글 삭제
    void deleteComment(Long commentId);

}

