package kr.or.kosa.quotationBoard.service;

import kr.or.kosa.attachedFile.service.AttachedFileService;
import kr.or.kosa.board.service.BoardService;
import kr.or.kosa.expert.dto.Category;
import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import kr.or.kosa.quotationBoard.dto.location;
import kr.or.kosa.quotationBoard.mapper.QuotationBoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuotationBoardService {

    @Autowired
    private QuotationBoardMapper quotationBoardMapper;
    @Autowired
    private AttachedFileService attachedFileService;
    @Autowired
    private BoardService boardService;

    // 게시판 생성
    public void createQuotationBoard(QuotationBoard quotationBoard) {
        quotationBoardMapper.insertQuotationBoard(quotationBoard);
    }
    public void addQuotationLocation(long postId, int locationId) {
        quotationBoardMapper.insertQuotationLocation(postId, locationId);
    }
    public void addQuotationLocations(long postId, List<Integer> locationIds) {
        quotationBoardMapper.insertQuotationLocations(postId, locationIds);
    }

    public int updateQuotationBoard(QuotationBoard quotationBoard) {
        return quotationBoardMapper.updateQuotationBoard(quotationBoard);
    }

    @Transactional
    public void deleteQuotationBoard(long postId) {
        System.out.println("postId = " + postId);
        // 1) 첨부파일 삭제
        attachedFileService.deleteAttachedFilesByPostId(postId);
        // 2) 지역 매핑 삭제
        quotationBoardMapper.deleteQuotationLocations(postId);
        // 3) quotation_board 삭제
        quotationBoardMapper.deleteQuotationBoard(postId);
        // 4) board 삭제
        boardService.deleteBoard(postId);
    }

    public List<QuotationBoard> findAllQuotationBoards(long userId,Long categoryId, Integer locationId) {
        List<QuotationBoard> list = quotationBoardMapper.findAll(userId,categoryId,locationId);
        System.out.println("Returned List Size: " + list.size());     // 3건 확인
        list.forEach(b -> System.out.println(
                "Post " + b.getPostId() + " locations: " + b.getQuotationLocations().size()
        ));
        return list;
    }

    public List<QuotationBoard> findMyRequests(long userId,String state) {
        return quotationBoardMapper.findMyRequests(userId,state);
    }

    public List<QuotationBoard> findMyQuotations(long userId,String state) {
        System.out.println("findMyQuotations userId = " + userId + " state = " + state + "quotation서비스 호출");
        List<QuotationBoard> list = quotationBoardMapper.findMyQuotations(userId,state);
        System.out.println(list);
        return list;
    }




    public QuotationBoard findByPostIdWithLocations(long postId) {
        return quotationBoardMapper.findByPostIdWithLocations(postId);
    }

    public List<location> findLocationsByUserId(long userId){
        return quotationBoardMapper.findLocationsByUserId(userId);
    }

    public List<Category> findCategoriesByUserId(long userId){
        return quotationBoardMapper.findCategoriesByUserId(userId);
    }

}
