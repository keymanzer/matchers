package kr.or.kosa.quotationBoard.mapper;

import kr.or.kosa.expert.dto.Category;
import kr.or.kosa.quotationBoard.dto.QuotationBoard;
import kr.or.kosa.quotationBoard.dto.location;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuotationBoardMapper {

    // QuotationBoard 데이터 삽입
    void insertQuotationBoard(QuotationBoard quotationBoard);

    int updateQuotationBoard(QuotationBoard quotationBoard);

    void deleteQuotationBoard(long postId);

    List<QuotationBoard> findAll(long userId,
                                 @Param("categoryId") Long categoryId,
                                 @Param("locationId") Integer locationId);

    QuotationBoard findByPostIdWithLocations(long postId);

    List<QuotationBoard> findMyRequests(long userId,String state);

    List<QuotationBoard> findMyQuotations(long userId,String state);

    void insertQuotationLocation(@Param("postId") long postId,
                                 @Param("locationId") int locationId);

    void insertQuotationLocations(@Param("postId") long postId,
                                  @Param("locationIds") List<Integer> locationIds);

    void deleteQuotationLocations(long postId);

    List<location>findLocationsByUserId(long userId);

    List<Category>findCategoriesByUserId(long userId);

}
