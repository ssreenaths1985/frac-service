package com.tarento.frac.models.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(includeFieldNames = true)
@NoArgsConstructor
@AllArgsConstructor
public class Plot {

	private Object label;
	private String name;
	private Object value;
	private String valueLabel;
	private String symbol;
	private String parentName;
	private String parentLabel;

	public Plot(String name, Object value, String symbol) {
		this.name = name;
		this.value = value;
		this.symbol = symbol;
	}

	public Plot(String name, Object value, String symbol, String headerLabel, String valueLabel) {
		this.name = name;
		this.value = value;
		this.symbol = symbol;
		this.label = headerLabel;
		this.valueLabel = valueLabel;
	}

	public Plot(String name, Object value, String symbol, String parentName, String headerLabel, String valueLabel) {
		this.name = name;
		this.value = value;
		this.symbol = symbol;
		this.parentName = parentName;
		this.label = headerLabel;
		this.valueLabel = valueLabel;
	}

	public Plot(String name, Object value, String symbol, String parentName, String headerLabel, String valueLabel,
			String parentLabel) {
		this.name = name;
		this.value = value;
		this.symbol = symbol;
		this.parentName = parentName;
		this.label = headerLabel;
		this.valueLabel = valueLabel;
		this.parentLabel = parentLabel;
	}

}