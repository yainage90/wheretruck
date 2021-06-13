package com.gamakdragons.wheretruck.domain.truck.dto;

import com.gamakdragons.wheretruck.domain.truck.entity.Truck;

import org.springframework.web.multipart.MultipartFile;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TruckUpdateRequestDto {
	
	private String id;
	private String name;
    private String description;
    private String userId;
    private MultipartFile image;

	public Truck toEntity() {
		
		return new Truck(
			getId(),
			getName(),
			null,
			getDescription(),
			false,
			getUserId(),
			0,
			0.0f,
			null,
			null,
			null
		);
	}
}
