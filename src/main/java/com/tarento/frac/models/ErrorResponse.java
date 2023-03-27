package com.tarento.frac.models;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class ErrorResponse {
	@JsonProperty("error")
	private String error; 
	
	@JsonProperty("error_description")
	private String errorDescription;
	
}
