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
public class FilterList {
	private String type;
	private Boolean isDetail;
	private String status;
	private String department;
	private List<NodeFilter> filters;
	private List<FilterMappings> mappings;
	private String userType;
}
