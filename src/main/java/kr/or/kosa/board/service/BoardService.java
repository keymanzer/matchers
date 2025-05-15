package kr.or.kosa.board.service;

import kr.or.kosa.board.dto.Board;
import kr.or.kosa.board.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardMapper boardMapper;


    public void createBoard(Board board) {
        System.out.println(board);
        System.out.println("보드서비스테스트");
        boardMapper.insertBoard(board);

    }

    public void updateBoard(int postId, Board board) {
        boardMapper.updateBoard(postId, board.getTitle(), board.getContent());
    }

    public void deleteBoard(int postId) {
        boardMapper.deleteBoard(postId);
    }

    public int getNextBoardId() {
        return boardMapper.getNextBoardId();
    }
}
