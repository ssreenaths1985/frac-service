package com.tarento.frac.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"type",
"properties"
})
@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
public class Items {

@JsonProperty("type")
public String type;
@JsonProperty("properties")
public Properties properties;

}
