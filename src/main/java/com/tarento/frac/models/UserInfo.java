package com.tarento.frac.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
	private String sub;
	private String name;
	@JsonProperty("preferred_username")
	private String preferredUserName;
	@JsonProperty("given_name")
	private String givenName;
	@JsonProperty("family_name")
	private String familyName;
	private String email;
	private List<String> roles;
	private String authToken;
}