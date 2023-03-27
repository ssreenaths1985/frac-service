package com.tarento.frac.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "actor", "eid", "edata", "ver", "ets", "context", "mid", "object" })
@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class Audit {

	@JsonProperty("actor")
	public Actor actor;
	@JsonProperty("eid")
	public String eid;
	@JsonProperty("edata")
	public Edata edata;
	@JsonProperty("ver")
	public String ver;
	@JsonProperty("ets")
	public Long ets;
	@JsonProperty("context")
	public Context context;
	@JsonProperty("mid")
	public String mid;
	@JsonProperty("object")
	public Objects object;

}
