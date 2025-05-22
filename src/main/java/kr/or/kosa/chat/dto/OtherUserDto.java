package kr.or.kosa.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUserDto {
    private Long userId;
    private String name; // 닉네임
    private String email;
}