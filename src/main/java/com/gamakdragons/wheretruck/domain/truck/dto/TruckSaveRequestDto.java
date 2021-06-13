package com.gamakdragons.wheretruck.domain.truck.dto;

import java.util.Collections;
import java.util.UUID;

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
public class TruckSaveRequestDto {

	private String id;
    private String name;
    private String description;
    private String userId;
    private MultipartFile image;

	public Truck toSaveEntity() {
		
		return new Truck(
			UUID.randomUUID().toString(),
			getName(),
			null,
			getDescription(),
			false,
			getUserId(),
			0,
			0.0f,
			null,
			Collections.emptyList(),
			Collections.emptyList()
		);
	}

	public Truck toUpdateEntity() {
		
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
