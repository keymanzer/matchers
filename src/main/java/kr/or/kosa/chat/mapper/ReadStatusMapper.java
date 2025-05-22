package kr.or.kosa.chat.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReadStatusMapper {
    void markAllAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);

    Long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

    void createUnreadStatus(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("messageId") Long messageId
    );
}