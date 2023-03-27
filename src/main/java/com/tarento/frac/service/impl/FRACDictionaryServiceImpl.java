package com.tarento.frac.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.CommonDao;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Dictionary;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.repository.ElasticSearchRepository;
import com.tarento.frac.service.DataNodeService;
import com.tarento.frac.service.FRACDictionaryService;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.Sql;
import com.tarento.frac.utils.TaggingConstants;

import scala.collection.mutable.StringBuilder;

@Service
public class FRACDictionaryServiceImpl implements FRACDictionaryService {

	@Autowired
	private ConfigurationPanel configurationPanel;

	@Autowired
	private ElasticSearchRepository elasticRepository;

	@Autowired
	private CommonDao commonDao;

	@Autowired
	private ApplicationProperties appProperties;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DataNodeService dataNodeService;

	public static final Logger Logger = LoggerFactory.getLogger(FRACDictionaryServiceImpl.class);

	@Override
	public boolean initiateDictionaryPush(DataNode dataNode, UserInfo userInfo) {
		new Thread(() -> {
			try {
				Thread.sleep(1000);
				Thread.sleep(1000);
				Thread.sleep(1000);
				pushContentToDictionary(dataNode, userInfo);
				webhookCallbackInitialization();
			} catch (Exception e) {
				Logger.error(String.format(
						"Encountered an exception error while Updating / Verifying an Existing Data Node to Internal Storage :  %s",
						e.getMessage()));
			}
		}).start();
		return Boolean.TRUE;
	}

	public boolean pushContentToDictionary(DataNode verifiedNode, UserInfo userInfo) throws Exception {
		Dictionary dictionary = null;
		if (TaggingConstants.getDictionaryNodeMap().get(verifiedNode.getType())) {

			// Update node in the dictionary
			if (configurationPanel.getDictionary().containsKey(commonDao.getDictionaryLabel(verifiedNode.getType()))) {
				List<DataNode> nodesList = configurationPanel.getDictionary()
						.get(commonDao.getDictionaryLabel(verifiedNode.getType()));
				updateNodeList(nodesList, verifiedNode, userInfo);
				configurationPanel.getDictionary().put(commonDao.getDictionaryLabel(verifiedNode.getType()), nodesList);
			} else if (StringUtils.isNotBlank(verifiedNode.getSecondaryStatus())
					&& verifiedNode.getSecondaryStatus().equals(NodeStatus.VERIFIED.name())) {
				List<DataNode> nodes = new ArrayList<>();
				nodes.add(verifiedNode);
				configurationPanel.getDictionary().put(commonDao.getDictionaryLabel(verifiedNode.getType()), nodes);
			}

			// Bind the child node
			addChildNode(verifiedNode);
			dictionary = new ObjectMapper().convertValue(configurationPanel.getDictionary(), Dictionary.class);
			elasticRepository.writeObjectToElastic(dictionary, "2020001", appProperties.getDictionary(),
					appProperties.getEsDocumentType());

		}
		return Boolean.TRUE;

	}

	public DataNode getNodeById(String id) {
		try {
			List<Map<String, Object>> result = jdbcTemplate.queryForList(
					Sql.DataNode.GET_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE + Sql.DataNode.DATA_NODE_ID_CONDITION
							+ Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION,
					new Object[] { id });
			List<DataNode> dataNodes = commonDao.nodeAndPropertyMappings(result);
			if (dataNodes != null && dataNodes.size() > 0) {
				return dataNodes.get(0);
			}
		} catch (Exception e) {
			Logger.error(String.format("Encountered an exception in getNodeById :  %s", e.getMessage()));
		}
		return null;
	}

	/**
	 * Method adds or remove the source node using the node status to the dictionary
	 * index type
	 * 
	 * @param nodesList
	 *            List<DataNode>
	 * @param updatedNode
	 *            DataNode
	 */
	private void updateNodeList(List<DataNode> nodesList, DataNode verifiedNode, UserInfo userInfo) {
		try {
			Boolean nodeExist = Boolean.FALSE;
			for (int i = 0; i < nodesList.size(); i++) {
				DataNode nodeObj = new ObjectMapper().convertValue(nodesList.get(i), DataNode.class);
				if (nodeObj.getId().equals(verifiedNode.getId())) {

					if (StringUtils.isNotBlank(verifiedNode.getSecondaryStatus())
							&& verifiedNode.getSecondaryStatus().equals(NodeStatus.VERIFIED.name())) {
						nodesList.set(i, verifiedNode);
						nodeExist = Boolean.TRUE;
					} else if (StringUtils.isNotBlank(verifiedNode.getSecondaryStatus())
							&& verifiedNode.getSecondaryStatus().equals(NodeStatus.REJECTED.name())) {
						DataNode revertedNode = nodeObj.clone();
						// revert the node changes
						if (revertedNode.getType().equals(Entities.COMPETENCY.name())) {
							dataNodeService.addDataNodeBulk(revertedNode, userInfo);
						} else {
							dataNodeService.addDataNode(revertedNode, userInfo);
						}
						dataNodeService.changeNodeStatusAndComments(nodeObj);
					} else {
						nodesList.remove(i);
						break;
					}
				}
			}
			if (StringUtils.isNotBlank(verifiedNode.getSecondaryStatus())
					&& verifiedNode.getSecondaryStatus().equals(NodeStatus.VERIFIED.name()) && !nodeExist) {
				nodesList.add(verifiedNode);
			}
		} catch (Exception e) {
			Logger.error(String.format("Encountered an exception in updateNodeList :  %s", e.getMessage()));
		}

	}

	/**
	 * Bind the child node array to parent node
	 * 
	 * @param verifiedNode
	 *            DataNode
	 * @param updatedNode
	 *            DataNode
	 */
	public void addChildNode(DataNode updatedNode) {
		if (updatedNode != null) {
			setAdditionalProperty(updatedNode);
			Map<String, List<NodeMapping>> nodeMap = configurationPanel.getChildMappingList();
			// to bind the child node object
			configurationPanel.setChildNodes(updatedNode, nodeMap, Boolean.TRUE);
			// set the level label and type if null for competency child node
			if (updatedNode.getType().equals(Entities.COMPETENCY.name()) && updatedNode.getChildren() != null) {
				for (DataNode childNode : updatedNode.getChildren()) {
					// set the level label and type if null
					if (StringUtils.isBlank(childNode.getType())) {
						childNode.setType(Entities.COMPETENCIESLEVEL.name());
					}
					if (StringUtils.isBlank(childNode.getLevel())) {
						childNode.setLevel("");
					}
				}
			}
		}
	}

	/**
	 * Set each additional property value of competency node to empty string if
	 * value is null
	 * 
	 * @param dataNode
	 *            DataNode
	 */
	private void setAdditionalProperty(DataNode dataNode) {
		if (dataNode.getAdditionalProperties() == null) {
			dataNode.setAdditionalProperties(new HashMap<>());
		}
		if (dataNode.getType().equals(Entities.COMPETENCY.name()) && dataNode.getAdditionalProperties() != null) {
			if (!dataNode.getAdditionalProperties().containsKey(Constants.Parameters.COMPETENCIES_AREA)) {
				dataNode.getAdditionalProperties().put(Constants.Parameters.COMPETENCIES_AREA, "");
			}
			if (!dataNode.getAdditionalProperties().containsKey(Constants.Parameters.COMPETENCY_TYPE)) {
				dataNode.getAdditionalProperties().put(Constants.Parameters.COMPETENCY_TYPE, "");
			}
			if (!dataNode.getAdditionalProperties().containsKey(Constants.Parameters.COD)) {
				dataNode.getAdditionalProperties().put(Constants.Parameters.COD, "");
			}
			if (!dataNode.getAdditionalProperties().containsKey(Constants.Parameters.COMPETENCYSECTOR)) {
				dataNode.getAdditionalProperties().put(Constants.Parameters.COMPETENCYSECTOR, "");
			}
		}
	}

	public Boolean webhookCallbackInitialization() {
		new Thread(() -> {
			Logger.info("Making a Web Hook Callback"+ " "+appProperties.getWebhookCallbackUrl());
			restTemplate.exchange(appProperties.getWebhookCallbackUrl(), HttpMethod.POST,
					new HttpEntity<>(new HashMap<>(), getHttpHeaders(null)), String.class);
		}).start();
		return Boolean.TRUE;
	}

	/**
	 * A helper method to create the headers for Rest Connection with authorization
	 * 
	 * @return HttpHeaders
	 */
	public HttpHeaders getHttpHeaders(String authorization) {
		final String AUTHORIZATION = Constants.Parameters.AUTHORIZATION;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.isNotBlank(authorization)) {
			headers.add(AUTHORIZATION, authorization);
		}
		return headers;
	}

	@Override
	public Boolean reloadDictionary() {
		try {
			Map<String, List<DataNode>> fracDictionary = new HashMap<>();
			List<DataNode> verifiedNodes = getAllVerifiedNodes();
			List<String> verifiedNodeId = new ArrayList<>();
			// push all verified items
			for (DataNode node : verifiedNodes) {
				String key = commonDao.getDictionaryLabel(node.getType());
				if (StringUtils.isNotBlank(key)) {
					verifiedNodeId.add(node.getId());
					if (!fracDictionary.containsKey(key)) {
						fracDictionary.put(key, new ArrayList<>());
					}
					fracDictionary.get(key).add(node);
				}
			}
			// check for unverified items in dictionary
			for (Map.Entry<String, List<DataNode>> entry : configurationPanel.getDictionary().entrySet()) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					DataNode node = new ObjectMapper().convertValue(entry.getValue().get(i), DataNode.class);
					DataNode editedNode = getNodeById(node.getId());
					if (!verifiedNodeId.contains(node.getId()) && editedNode != null
							&& StringUtils.isNotBlank(editedNode.getId())) {
						if (!fracDictionary.containsKey(entry.getKey())) {
							fracDictionary.put(entry.getKey(), new ArrayList<>());
						}
						fracDictionary.get(entry.getKey()).add(node);
					}
				}
			}

			// update dictionary index
			Dictionary dictionary = new ObjectMapper().convertValue(fracDictionary, Dictionary.class);
			elasticRepository.writeObjectToElastic(dictionary, "2020001", appProperties.getDictionary(),
					appProperties.getEsDocumentType());
			configurationPanel.setDictionary(fracDictionary);
			webhookCallbackInitialization();
			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an exception in reloadDictionary :  %s", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	/**
	 * Returns all the verified nodes with children
	 * 
	 * @return List<DataNode>
	 * @throws Exception
	 */
	public List<DataNode> getAllVerifiedNodes() throws Exception {
		StringBuilder queryBuilder = new StringBuilder(Sql.DataNode.GET_NODE_AND_PROPERTY
				+ Sql.Common.WHERE_NOT_DRAFT_CLAUSE + Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION
				+ Sql.Common.AND_CLAUSE + Constants.SqlParams.SECONDARY_STATUS + Sql.Common.APPEND_VALUE);
		Object[] params = new Object[] { NodeStatus.VERIFIED.name() };
		List<Map<String, Object>> result = jdbcTemplate.queryForList(queryBuilder.toString(), params);
		List<DataNode> dataNodes = commonDao.nodeAndPropertyMappings(result);
		// set child nodes
		for (DataNode nodeObj : dataNodes) {
			addChildNode(nodeObj);
		}

		return dataNodes;
	}

}
