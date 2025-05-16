package kr.or.kosa.board.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.kosa.board.dto.Board;

@Mapper
public interface BoardMapper {

    // 게시글 등록
    void insertBoard(Board board);

    // 게시글 수정
    void updateBoard(@Param("postId") int postId, @Param("title") String title, @Param("content") String content);

    // 게시글 삭제
    void deleteBoard(int postId);

    int getNextBoardId();

}
