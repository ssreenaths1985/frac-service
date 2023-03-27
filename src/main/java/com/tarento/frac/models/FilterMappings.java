package com.tarento.frac.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(includeFieldNames = true)
@NoArgsConstructor

@SuppressWarnings("all")
public class FilterMappings {

	private String type;
	private String id;
	private String name;
	private String relation;

}
