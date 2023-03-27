package com.tarento.frac.dao;

import java.util.List;
import java.util.Map;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeFeedback;
import com.tarento.frac.models.NodeKeys;
import com.tarento.frac.models.NodeLogs;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.models.analytics.AggregateDto;

/**
 * This interface for Portfolio contains the method which executes the Search
 * Request and Delete Request on Elastic Search Repositories
 * 
 * @author Darshan Nagesh
 *
 */
public interface FRACDao {

	/**
	 * Adds data node and its property
	 * 
	 * @param dataNode
	 *            DataNode
	 * @return Boolean
	 */
	public Boolean addDataNode(DataNode dataNode);

	/**
	 * Updates data node and its property
	 * 
	 * @param dataNode
	 *            DataNode
	 * @return Boolean
	 */
	public Boolean updateDataNode(DataNode dataNode);

	/**
	 * Updates list of nodes and property
	 * 
	 * @param dataNodes
	 *            List<DataNode>
	 * @param userId
	 *            String
	 * @return Boolean
	 */
	public Boolean updateDataNodeBulk(List<DataNode> dataNodes);

	/**
	 * To update the status of the node by its Id
	 * 
	 * @param status
	 *            String
	 * @param id
	 *            String
	 * @return Boolean
	 */
	public Boolean updateNodeStatus(String status, String id);

	DataNode getNodeById(String id, Boolean showSimilar, Boolean isDetail, Boolean bookmarks, String userId);

	public Boolean setnodeMapping(NodeMapping nodeMapping);

	public List<DataNode> searchNodes(MultiSearch multiSearch);

	public List<DataNode> getBookmark(String userId, String type);

	public Boolean bookmarkDataNode(DataNode bookmarkNode, String userId);

	public Boolean verifyLevelOne(DataNodeVerification verification, String userId);

	public Boolean verifyLevelTwo(DataNodeVerification verification, String userId);

	Long getCountOfNodes(String type, String department, String status, String userType);

	public List<DataNode> filterDataNodes(FilterList filterList, String userId);

	public Map<String, Object> getAllDataNodes(RequestObject reqObject, String version);

	Boolean mapNodes(NodeMapping nodeMapping);

	Overview exploreAllNodes(String keyword, String department);

	Map<String, DataNode> getAllNodesOf(List<String> allIds, String keyword, String department);

	public DataNodesVerificationView getVerificationList(String type, String department, Boolean isDetail,
			String userType);

	public List<DataNode> getChildNodes(String parentId, String type);

	public List<DataNode> getParentNodes(String childId, String type);

	public Boolean deleteNode(String id);

	public List<DataNode> filterByMappings(List<String> idList, String type, String userId);

	public DataNodesVerificationView filterReviewNodes(FilterList filterList);

	public List<String> getSourceList(String type);

	List<NodeFeedback> getNodeFeedback(String type, String id, String userId);

	Boolean nodeFeedback(Extensions extension, String user);

	List<NodeLogs> getCollectionLogs(String id, String type);

	Extensions getNodeRatingAverage(String type, String id);

	List<DataNode> fetchCompetencyAreas();

	KeyValueList getCompetencyAreaListing(String department, String status);

	Overview exploreSearch(String keyword, String department);

	List<DataNode> getMapping(String id, String type, String returnType, Boolean isDetail);

	AggregateDto getMyGraphs(String visualizationCode, String userId);

	Boolean reviewMappings(MappingVerification mappingVerification);

	List<DataNode> filterByMappings(FilterList filterList, String userId);

	public Boolean changeNodeStatusAndComments(DataNode dataNode);

	public Boolean addDataNodeBulk(List<DataNode> toIndex);

	List<DataNode> getAllDataNodeByType(String type);

	List<DataNode> getPropertyNode(String type);

	public KeyValueList getPropertyCountList(String type);

	public Boolean updateNodeLastInsertedCount(NodeKeys nodeKey);

	public Boolean updateAllNodeLastInsertedCount();

}