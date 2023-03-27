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
public class Dictionary {
	private List<DataNode> positions;
	private List<DataNode> roles;
	private List<DataNode> activities;
	private List<DataNode> competencies;
}
