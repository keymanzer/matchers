package kr.or.kosa.findedExpert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertCardDto {
    private Long userId;
    private String nickname;
    private String profileImg;
    private String career;
    private Long completedProjects;
    private List<String> categories;
    private List<String> locations;
    private double rating;
}
