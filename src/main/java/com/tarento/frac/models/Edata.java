package com.tarento.frac.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
"state",
"props",
"cb_object"
})
@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class Edata {

	@JsonProperty("state")
	public String state;
	@JsonProperty("props")
	public List<String> props = null;
	@JsonProperty("cb_object")
	public CbObject cbObject;
}
