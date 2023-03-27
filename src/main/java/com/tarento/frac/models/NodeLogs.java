package com.tarento.frac.models;

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
public class NodeLogs {

	private String id;
	private String type;
	private String updatedBy;
	private String updatedByEmail;
	private String updatedDate;
	private String user;
	private DataNode earlierStage;
	private DataNode changedStage;
	private String changeStatement;
	private String reference;
	private String comments;
}
