package kr.or.kosa.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.community.dto.CommunityBoardComment;

@Mapper
public interface CommunityBoardCommentMapper {

    List<CommunityBoardComment> getCommentListByPostId(Long postId);

    CommunityBoardComment getCommentById(Long commentId);
    
    void insertComment(CommunityBoardComment comment);

    void updateComment(CommunityBoardComment comment);

    void deleteComment(Long commentId);

    // 부모 댓글에 달린 대댓글(자식 댓글) 리스트 조회
    List<CommunityBoardComment> getRepliesByParentId(Long parentCommentId);
}

