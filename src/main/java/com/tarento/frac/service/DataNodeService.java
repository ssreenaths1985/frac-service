package com.tarento.frac.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.UserInfo;

public interface DataNodeService {

	/**
	 * Add a new Data Node based on the Type which has been mentioned in the Data
	 * Node Object
	 * 
	 * @param dataNode
	 * @param userInfo
	 * @return
	 */
	DataNode addDataNode(DataNode dataNode, UserInfo userInfo);

	/**
	 * This is a service method for receiving the list of data nodes and adding them
	 * into the database This return the same list with updated identifiers.
	 * 
	 * @param dataNodeList
	 * @param userInfo
	 * @return
	 */
	List<DataNode> addDataNodes(List<DataNode> dataNodeList, UserInfo userInfo);

	/**
	 * To add data node and child
	 * 
	 * @param dataNode
	 *            DataNode
	 * @param userInfo
	 *            UserInfo
	 * @return DataNode
	 */
	DataNode addDataNodeBulk(DataNode dataNode, UserInfo userInfo);

	/**
	 * Add or update data node from excel sheet
	 * 
	 * @param file
	 *            File
	 * @param userInfo
	 *            UserInfo
	 * @return List<DataNode>
	 */
	public List<DataNode> uploadDataNode(File file, UserInfo userInfo);

	/**
	 * Delete node
	 * 
	 * @param id
	 *            String
	 * @param type
	 *            String
	 * @param userInfo
	 *            String
	 * @return Boolean
	 */
	Boolean deleteNode(String id, String type, UserInfo userInfo);

	/**
	 * User authorization check if a user has access to edit verified competency
	 * 
	 * @param dataNode
	 *            DataNode
	 * @param userRole
	 *            List<String>
	 * @return Boolean
	 */
	Boolean checkUserAccesstoEdit(DataNode dataNode, List<String> userRole);

	/**
	 * Update node status and review comments
	 * 
	 * @param nodeObj
	 *            DataNode
	 */
	Boolean changeNodeStatusAndComments(DataNode nodeObj);

	/**
	 * Add child node to parent
	 * 
	 * @param nodeMapping
	 *            NodeMapping
	 * @param userInfo
	 *            UserInfo
	 * @return Boolean
	 */
	Boolean mapNodes(NodeMapping nodeMapping, UserInfo userInfo);

	Boolean appendMapNodes(NodeMapping newMapping, UserInfo userInfo);

	void getReviewedNode(Map<String, Object> search, UserInfo userInfo);

}
