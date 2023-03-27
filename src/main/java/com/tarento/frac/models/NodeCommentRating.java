package com.tarento.frac.models;

import java.util.List;

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
public class NodeCommentRating extends DataNode{
	private String user; 
	private int rating; 
	private int average; 
	private List<Comment> comments; 
}
