package com.tarento.frac.models;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NodeSource {
	ISTM("ISTM"), CBPPROVIDER("CBP PROVIDER");

	private String value;

	NodeSource(final String value) {
		this.value = value;
	}

	@Override
	@JsonValue
	public String toString() {
		return StringUtils.capitalize(name());
	}

	@JsonCreator
	public static NodeSource fromValue(final String passedValue) {
		for (final NodeSource obj : NodeSource.values()) {
			if (String.valueOf(obj.value).equalsIgnoreCase(passedValue)) {
				return obj;
			}
		}
		return null;
	}

}
