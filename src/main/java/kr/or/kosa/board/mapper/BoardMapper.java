package kr.or.kosa.board.mapper;
import kr.or.kosa.board.dto.Board;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardMapper {

    // 게시글 등록
    void insertBoard(Board board);

    // 게시글 수정
    void updateBoard(int postId, String title, String content);

    // 게시글 삭제
    void deleteBoard(int postId);

    int getNextBoardId();
}
