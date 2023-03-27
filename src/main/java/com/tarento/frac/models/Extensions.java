package com.tarento.frac.models;

import java.util.List;

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
public class Extensions {
	private String type;
	private String id;
	private Double rating;
	private List<NodeFeedback> feedbacks;
	private String userId;

	@JsonInclude(Include.NON_NULL)
	private NodeFeedback feedback;

}
