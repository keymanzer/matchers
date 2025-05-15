package kr.or.kosa.quotationBoard.mapper;

import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuotationBoardMapper {

    // QuotationBoard 데이터 삽입
    void insertQuotationBoard(QuotationBoard quotationBoard);

    int updateQuotationBoard(QuotationBoard quotationBoard);
}
