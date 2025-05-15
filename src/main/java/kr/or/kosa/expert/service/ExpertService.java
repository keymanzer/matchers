package kr.or.kosa.expert.service;

import java.util.List;

import kr.or.kosa.expert.dto.Category;
import kr.or.kosa.expert.dto.ExpertDto;
import kr.or.kosa.expert.dto.Location;

public interface ExpertService {

	int expertReg(ExpertDto expert);

	List<Location> getLocationList();

	List<Category> getCategoryList();
}
