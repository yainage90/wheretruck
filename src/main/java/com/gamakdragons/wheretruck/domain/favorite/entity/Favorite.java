package com.gamakdragons.wheretruck.domain.favorite.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class Favorite {
	
	private String id;
	private String truckId;
	private String userId;
}
