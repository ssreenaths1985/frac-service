package com.tarento.frac.models;

import java.util.HashMap;
import java.util.Map;

public class ExploreNodesMapper {
	Map<String, DataNode> positionMap = new HashMap<>();
	Map<String, DataNode> roleMap = new HashMap<>();
	Map<String, DataNode> competencyMap = new HashMap<>();
	Map<String, DataNode> krMap = new HashMap<>();
	Map<String, DataNode> activityMap = new HashMap<>();
	public Map<String, DataNode> getPositionMap() {
		return positionMap;
	}
	public void setPositionMap(Map<String, DataNode> positionMap) {
		this.positionMap = positionMap;
	}
	public Map<String, DataNode> getRoleMap() {
		return roleMap;
	}
	public void setRoleMap(Map<String, DataNode> roleMap) {
		this.roleMap = roleMap;
	}
	public Map<String, DataNode> getCompetencyMap() {
		return competencyMap;
	}
	public void setCompetencyMap(Map<String, DataNode> competencyMap) {
		this.competencyMap = competencyMap;
	}
	public Map<String, DataNode> getKrMap() {
		return krMap;
	}
	public void setKrMap(Map<String, DataNode> krMap) {
		this.krMap = krMap;
	}
	public Map<String, DataNode> getActivityMap() {
		return activityMap;
	}
	public void setActivityMap(Map<String, DataNode> activityMap) {
		this.activityMap = activityMap;
	}
	
	
}
