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
public class DataNodeVerification extends DataNode {
	public String user;
	public Boolean verified;
	public String userType;

	public DataNode getDataNode(DataNodeVerification verification) {
		return new DataNode(verification.getType(), verification.getId(), verification.getName(),
				verification.getDescription(), verification.getStatus(), verification.getSecondaryStatus(),
				verification.getSource(), verification.getActive(), verification.getDepartment(),
				verification.getBookmark(), verification.getLevel(), verification.getReviewComments(),
				verification.getSecondaryReviewComments(), verification.getAdditionalProperties(),
				verification.getChildren(), verification.getChildCount(), verification.getSimilarities(),
				verification.getUserInfo(), verification.getLevelId(), verification.getCreatedDate(),
				verification.getCreatedBy(), verification.getUpdatedDate(), verification.getUpdatedBy(),
				verification.getReviewedDate(), verification.getReviewedBy());
	}
}