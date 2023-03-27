package com.tarento.frac.models.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString(includeFieldNames = true)
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregateDto {

	private ChartType chartType;

	private String visualizationCode;

	private String chartFormat;

	private String drillDownChartId;

	private Object filterKeys;

	private Map<String, Object> customData;

	private RequestDate dates;

	private Object filter;

	private List<Data> data = new ArrayList<>();

}