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
public class NodeMapping {
	private Long id;
	private String parent;
	private String parentId;
	private String child;
	private String childId;
	private List<String> childIds;
	private String status;
}
