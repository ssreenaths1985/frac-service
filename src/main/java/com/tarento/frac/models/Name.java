package com.tarento.frac.models;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "name", "type" })
@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")

public class Name {

	@JsonProperty("name")
	public String name;
	@JsonProperty("type")
	public String type;

}
