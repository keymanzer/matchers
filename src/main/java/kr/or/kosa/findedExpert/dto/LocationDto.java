package kr.or.kosa.findedExpert.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    private Long locationId;
    private String city;
    private String district;
    
    public String getFullName() {
        return city + " " + district;
    }
}
