package com.tarento.frac.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.UserInfo;

public interface ActivityService {

	/**
	 * Creates the log document of newly created, updated, deleted or reviewed node
	 * and pushes to elasticsearch && Updates the node changes in configuration
	 * panel
	 * 
	 * @param dataNode
	 *            DataNode
	 * @param userInfo
	 *            UserInfo
	 * @param action
	 *            String
	 */
	Boolean updateNodeLogs(DataNode dataNode, UserInfo userInfo, String action);

	/**
	 * Push activity logs on update of a child node
	 * 
	 * @param nodeMapping
	 *            nodeMapping
	 * @param userInfo
	 *            userInfo
	 */
	void childNodeLogs(NodeMapping nodeMapping, UserInfo userInfo);

	/**
	 * Updates parent node status to UNVERIFIED using childId on editing a
	 * competency level
	 * 
	 * @param childId
	 *            String
	 */
	void updateParentNodeStatus(String childId, UserInfo userInfo);

	/**
	 * Creates an asynchronous bulk indexing elastic operation to add the activity
	 * logs
	 * 
	 */
	void bulkUpdateNodeLogs(List<DataNode> dataNodes, Map<String, List<Map<String, Object>>> childMappings,
			Map<Integer, Map<String, Object>> parentObj, UserInfo userInfo);

	Callable updateAuditData(List<DataNodeVerification> verification, UserInfo userInfo, Boolean batchProcess);

}
