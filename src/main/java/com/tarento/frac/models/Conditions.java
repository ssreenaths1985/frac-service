package com.tarento.frac.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class Conditions {

	private Long id;
	@JsonInclude(Include.NON_NULL)
	private String name;
	@JsonInclude(Include.NON_NULL)
	private String description;
	@JsonInclude(Include.NON_NULL)
	private List<SubCondition> subCondition;
}