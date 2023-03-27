package com.tarento.frac.models;

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
public class Comment {
	private String type; 
	private String id;
	private String user; 
	private String comment; 
	private Long date; 
}
