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
public class RequestDate {
	private String targetDate;
	private String startDate;
	private String endDate;
	private String interval;
}