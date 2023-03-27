package com.tarento.frac.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(includeFieldNames = true)
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("all")
@JsonInclude(Include.NON_NULL)
public class NodeReview {

	private Long id;
	private String label;
	private int hierarchy;
	private String status;
	private String reviewComments;
	private String reviewedBy;

}
