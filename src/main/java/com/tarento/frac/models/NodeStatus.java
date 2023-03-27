package com.tarento.frac.models;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NodeStatus {
	UNVERIFIED("UNVERIFIED"), VERIFIED("VERIFIED"), REJECTED("REJECTED"), DRAFT("DRAFT");

	private String value;

	NodeStatus(final String value) {
		this.value = value;
	}

	@Override
	@JsonValue
	public String toString() {
		return StringUtils.capitalize(name());
	}

	@JsonCreator
	public static NodeStatus fromValue(final String passedValue) {
		for (final NodeStatus obj : NodeStatus.values()) {
			if (String.valueOf(obj.value).equalsIgnoreCase(passedValue)) {
				return obj;
			}
		}
		return null;
	}

}
