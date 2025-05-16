package kr.or.kosa.expert.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.expert.dto.Category;
import kr.or.kosa.expert.dto.ExpertDto;
import kr.or.kosa.expert.dto.Location;
import kr.or.kosa.mapper.ExpertMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpertServiceImpl implements ExpertService {

	private final ExpertMapper expertMapper;

	@Value("${spring.servlet.multipart.location}")
	private String uploadDirectory;

	@Override
	@Transactional
	public int expertReg(ExpertDto expert) {
		int result = 0;
		MultipartFile profileImgFile = expert.getProfileImgFile();

		if (profileImgFile != null && !profileImgFile.isEmpty()) {
			String profileImg = saveFile(profileImgFile);
			expert.setProfileImg(profileImg);
		}

		Map<String, Object> expertParams = new HashMap<>();
		expertParams.put("userId", expert.getUserId());
		expertParams.put("career", expert.getCareer());

		result += expertMapper.expertInfoReg(expertParams);

		Map<String, Object> userParams = new HashMap<>();
		userParams.put("userId", expert.getUserId());
		userParams.put("nickname", expert.getNickname());
		userParams.put("profileImg", expert.getProfileImg());

		result += expertMapper.userInfoUpdate(userParams);

		for (String location : expert.getLocations()) {
			Map<String, Object> locationParams = new HashMap<>();
			locationParams.put("userId", expert.getUserId());
			locationParams.put("location", location.split(" ")[1]);
			result += expertMapper.expertLocReg(locationParams);
		}

		for (String category : expert.getCategories()) {
			Map<String, Object> categoryParams = new HashMap<>();
			categoryParams.put("userId", expert.getUserId());
			categoryParams.put("category", category);
			result += expertMapper.expertCtgReg(categoryParams);
		}

		for (MultipartFile certImage : expert.getCertImages()) {
			if (certImage != null && !certImage.isEmpty()) {
				String fileName = saveFile(certImage);
				Map<String, Object> licenseParams = new HashMap<>();
				licenseParams.put("userId", expert.getUserId());
				licenseParams.put("name", fileName);
				licenseParams.put("path", uploadDirectory + "/" + fileName);
				result += expertMapper.expertLcsReg(licenseParams);
			}
		}

		return result;
	}

	public List<Location> getLocationList() {
		return expertMapper.getLocationList();
	}

	public List<Category> getCategoryList() {
		return expertMapper.getCategoryList();
	}

	private String saveFile(MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			Path path = Paths.get(uploadDirectory + File.separator + fileName);
			file.transferTo(path.toFile());
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("파일 업로드 실패", e);
		}
	}
}
