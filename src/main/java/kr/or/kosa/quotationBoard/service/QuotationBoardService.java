package kr.or.kosa.quotationBoard.service;

import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import kr.or.kosa.quotationBoard.mapper.QuotationBoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuotationBoardService {

    @Autowired
    private QuotationBoardMapper quotationBoardMapper;

    // 게시판 생성
    public void createQuotationBoard(QuotationBoard quotationBoard) {
        quotationBoardMapper.insertQuotationBoard(quotationBoard);
    }

    public int updateQuotationBoard(QuotationBoard quotationBoard) {
        return quotationBoardMapper.updateQuotationBoard(quotationBoard);
    }
}
