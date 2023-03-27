package com.tarento.frac.models;

import java.util.List;

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
@JsonPropertyOrder({ "id", "type", "ver", "name", "org", "sub_type" })
@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class CbObject {

	@JsonProperty("id")
	public String id;
	@JsonProperty("type")
	public String type;
	@JsonProperty("ver")
	public String ver;
	@JsonProperty("name")
	public String name;
	@JsonProperty("org")
	public String org;
	@JsonProperty("sub_type")
	public String subType;

}
