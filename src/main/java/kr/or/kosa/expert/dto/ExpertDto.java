package kr.or.kosa.expert.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ExpertDto {

	private Long userId;
	private String nickname;
	private String profileImg;
	private MultipartFile profileImgFile;
	private List<MultipartFile> certImages;
	private List<String> categories;
	private List<String> locationIds;
	private String career;
}
