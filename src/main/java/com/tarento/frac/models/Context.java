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
@JsonPropertyOrder({ "channel", "pdata", "env" })
@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class Context {

	@JsonProperty("channel")
	public String channel;
	@JsonProperty("pdata")
	public Pdata pdata;
	@JsonProperty("env")
	public String env;

}
