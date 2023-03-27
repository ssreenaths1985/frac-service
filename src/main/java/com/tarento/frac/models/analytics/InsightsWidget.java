package com.tarento.frac.models.analytics;

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
public class InsightsWidget {

	private String name;
	private Object value;
	private String indicator;
	private String colorCode;

}