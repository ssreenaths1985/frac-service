package com.tarento.frac.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
@JsonInclude(Include.NON_NULL)
public class DataNode implements Cloneable {
	private String type;
	private String id;
	private String name;
	private String description;
	private String status;
	private String secondaryStatus;
	private String source;
	private Boolean active;
	private String department;
	private Boolean bookmark;
	private String level;
	private String reviewComments;
	private String secondaryReviewComments;
	private Map<String, Object> additionalProperties;
	private List<DataNode> children;
	private Map<String, Integer> childCount;
	private List<DataNode> similarities;
	private UserInfo userInfo;
	private Long levelId;

	private String createdDate;
	private String createdBy;
	private String updatedDate;
	private String updatedBy;
	private String reviewedDate;
	private String reviewedBy;

	public DataNode(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public DataNode(String id, String name, String description, String level) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.level = level;
	}

	public DataNode clone() throws CloneNotSupportedException {
		Gson gson = new Gson();
		String propertyMapString = gson.toJson(this.additionalProperties);
		Map<String, Object> additionalProperties = gson.fromJson(propertyMapString, Map.class);
		DataNode clonedNode = (DataNode) super.clone();
		clonedNode.setAdditionalProperties(additionalProperties);
		return clonedNode;
	}
}