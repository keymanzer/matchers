package kr.or.kosa.qa.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.kosa.qa.dto.QaBoard;

@Mapper
public interface QaBoardMapper {

    List<QaBoard> getPostList();

    QaBoard getPostbyId(Long postId);

    void insertPost(QaBoard qaBoard);

    void updatePost(QaBoard qaBoard);

    void deletePost(Long postId);
    
}
