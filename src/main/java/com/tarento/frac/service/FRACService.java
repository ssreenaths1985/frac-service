package com.tarento.frac.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeFeedback;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.models.analytics.AggregateDto;

public interface FRACService {

	/**
	 * Get All Data Nodes based on the Type which has been passed as a paramter
	 * 
	 * @param d
	 * @param isDetail
	 * @param myRequest
	 * @param userId
	 * @param userType
	 * @returnF
	 */
	List<DataNode> getAllDataNodes(DataNode d, Boolean isDetail, Boolean myRequest, String userId, String userType);

	List<DataNode> getChildNodes(String parentId, String type);

	List<DataNode> getParentNodes(String childId, String type);

	Long getCountOfNodes(String type, String department, String status, String userType);

	Overview exploreSearch(String keyword, String department);

	DataNodesVerificationView getVerificationList(String type, String department, Boolean isDetail, String userType);

	List<DataNode> filterDataNodes(FilterList filterList, String userId);

	KeyValueList getCompetencyAreaListing(String department, String status);

	boolean bookmarkDataNode(DataNode bookmarkNode, UserInfo userInfo);

	List<DataNode> filterByMappings(FilterList filterList, String userId);

	Overview exploreAllNodes(String keyword, String department);

	List<DataNode> getMapping(String id, String type, String returnType, Boolean isDetail);

	DataNode getNodeById(String id, Boolean showSimilar, Boolean isDetail, Boolean bookmarks, String sub);

	List<DataNode> searchNodes(MultiSearch multiSearch);

	List<DataNode> getBookmark(String sub, String type);

	Boolean reviewMappings(MappingVerification mappingVerification);

	DataNodesVerificationView filterReviewNodes(FilterList filterList);

	List<String> getSourceList(String type);

	Boolean flushReloadCache(String type, Boolean flush, Boolean reload);

	AggregateDto getMyGraphs(String visualizationCode, String sub);

	Boolean nodeFeedback(Extensions extension, String name);

	List<NodeFeedback> getNodeFeedback(String type, String id, String userId);

	Extensions getNodeRatingAverage(String type, String id);

	Object getCollectionLogs(String id, String type);

	KeyValueList getPropertyCountList(String type);

	Map<String, Object> getAllDataNodes(RequestObject reqObject, String version);

	List<DataNode> privateMethodToUpdateDB(Map<String, Object> request, String userId) throws IOException;

}