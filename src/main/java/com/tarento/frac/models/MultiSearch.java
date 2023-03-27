package com.tarento.frac.models;

import java.util.List;

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
public class MultiSearch {
	private List<SearchBox> searches;
	private List<NodeFilter> filter;
	private Boolean childCount;
	private Boolean childNodes;
	private String userType;
	private String tool;
	private String sort;
}
