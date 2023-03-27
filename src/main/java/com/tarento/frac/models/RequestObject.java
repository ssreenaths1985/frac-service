package com.tarento.frac.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestObject {
	private String type;
	private Boolean isDetail;
	private String status;
	private String department;
	private Boolean bookmark;
	private Boolean myRequest;
	private String userType;
	private String userId;
	private Integer limit;
	private Integer offset;
}
