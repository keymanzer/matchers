package kr.or.kosa.qa.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.qa.dto.QaBoard;

@Mapper
public interface QaBoardMapper {
    List<QaBoard> getPostList();
    
    List<QaBoard> getPostListByViews();

    QaBoard getPostbyId(@Param("postId") Long postId);

    void insertPost(QaBoard qaBoard);

    void updatePost(QaBoard qaBoard);

    void increaseViewCount(@Param("postId") Long postId);

    void deletePost(@Param("postId") Long postId);
}

