package com.tarento.frac.models;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Entities {

	POSITION("POSITION"), ROLE("ROLE"), ACTIVITY("ACTIVITY"), COMPETENCY("COMPETENCY"), KNOWLEDGERESOURCE(
			"KNOWLEDGERESOURCE"), COMPETENCIESLEVEL("COMPETENCIESLEVEL"), COMPETENCYAREA(
					"COMPETENCYAREA"), COMPETENCYTYPE("COMPETENCYTYPE"), SECTOR("SECTOR");

	private String value;

	Entities(final String value) {
		this.value = value;
	}

	@Override
	@JsonValue
	public String toString() {
		return StringUtils.capitalize(name());
	}

	@JsonCreator
	public static Entities fromValue(final String passedValue) {
		for (final Entities obj : Entities.values()) {
			if (String.valueOf(obj.value).equalsIgnoreCase(passedValue)) {
				return obj;
			}
		}
		return null;
	}

}
