package com.tarento.frac.models;

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
public class SubCondition {

	private Long id;
	@JsonInclude(Include.NON_NULL)
	private String cmd;
}