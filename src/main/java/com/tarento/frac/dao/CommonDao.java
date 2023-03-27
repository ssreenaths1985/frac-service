package com.tarento.frac.dao;

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.NodeKeys;
import com.tarento.frac.models.StorageService;

public interface CommonDao {

	/**
	 * This method receives the Search Request which already contains a query to be
	 * searched with. Method will execute the Search Request by using the
	 * RestHighLevelClient and sends the reponse back
	 * 
	 * @param searchRequest
	 * @return
	 */

	public MultiSearchResponse executeMultiSearchRequest(SearchRequest searchRequest);

	public MultiSearchResponse executeMultipleMultiSearchRequest(List<SearchRequest> searchRequestList);

	public Boolean executeDeleteRequest(DeleteRequest deleteRequest);

	public SearchResponse executeSearchRequest(SearchRequest searchRequest);

	public SearchResponse executeScrollSearch(SearchScrollRequest searchRequest);

	/**
	 * Function to load all the data nodes in cache
	 * 
	 * @param type
	 *            String
	 */
	public void loadAllDataNodes(String type);

	/**
	 * To load all data node mappings
	 */
	public void loadAllNodeMappings();

	/**
	 * Method to get list of data nodes by mapping all the additional properties in
	 * to the data node
	 * 
	 * @param result
	 *            List<Map<String, Object>>
	 * @return List<DataNode>
	 */
	public List<DataNode> nodeAndPropertyMappings(List<Map<String, Object>> result);

	/**
	 * To get the cloud storage provider access details
	 * 
	 * @param provider
	 *            String
	 */
	public StorageService getStorageService(String provider);

	/**
	 * To get file URL for KR
	 * 
	 * @param dataNode
	 *            DataNode
	 */
	void setKRResourceSignedURL(DataNode dataNode);

	/**
	 * Returns the field name of the frac-dictionary index from the node type
	 * 
	 * @param type
	 *            String
	 * @return String
	 */
	String getDictionaryLabel(String type);

	/**
	 * Method to get the node prefix and last inserted count
	 */
	List<NodeKeys> getNodeKeys();

}