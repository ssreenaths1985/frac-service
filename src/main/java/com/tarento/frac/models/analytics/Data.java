package com.tarento.frac.models.analytics;

import java.util.ArrayList;
import java.util.List;

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
public class Data {

	private String headerName;
	private Object headerValue;
	private String headerSymbol;
	private String colorPaletteCode;
	private Long colorPaletteId;

	public Data(String name, Object value, String symbol) {
		this.headerName = name;
		this.headerValue = value;
		this.headerSymbol = symbol;
	}

	public Data(String name, Object value, String symbol, List<Plot> plots) {
		this.headerName = name;
		this.headerValue = value;
		this.headerSymbol = symbol;
		this.plots = plots;
	}

	private List<Plot> plots = new ArrayList<>();
	private InsightsWidget insight;
	private Boolean isDecimal;

}