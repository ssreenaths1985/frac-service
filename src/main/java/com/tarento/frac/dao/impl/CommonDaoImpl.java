package com.tarento.frac.dao.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.CommonDao;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeKeys;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.StorageService;
import com.tarento.frac.repository.ElasticSearchRepository;
import com.tarento.frac.utils.CloudStore;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.Sql;

@Repository(Constants.ServiceRepositories.COMMON_DAO)
public class CommonDaoImpl implements CommonDao {

	public static final Logger Logger = LoggerFactory.getLogger(CommonDaoImpl.class);
	String daoImplMarker = Constants.ServiceRepositories.COMMON_DAO + Constants.Markers.DAO_IMPL;
	Marker marker = MarkerFactory.getMarker(daoImplMarker);
	private static final String EXCEPTION = "Exception in %s: %s";

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private ElasticSearchRepository elasticRepository;

	@Override
	public MultiSearchResponse executeMultiSearchRequest(SearchRequest searchRequest) {
		MultiSearchRequest multiRequest = new MultiSearchRequest();
		MultiSearchResponse response = null;
		RestHighLevelClient client = null;
		try {
			multiRequest.add(searchRequest);
			client = elasticRepository.connectToElasticSearch();
			if (client != null) {
				response = client.multiSearch(multiRequest);
				client.close();
			}
		} catch (IOException e) {
			Logger.error(String.format("Encountered an exception error while connecting :  %s", e.getMessage()));
		}
		return response;
	}

	@Override
	public MultiSearchResponse executeMultipleMultiSearchRequest(List<SearchRequest> searchRequestList) {
		MultiSearchRequest multiRequest = new MultiSearchRequest();
		MultiSearchResponse response = null;
		RestHighLevelClient client = null;
		try {
			for (SearchRequest searchRequest : searchRequestList) {
				multiRequest.add(searchRequest);
			}

			client = elasticRepository.connectToElasticSearch();
			if (client != null) {
				response = client.multiSearch(multiRequest);
				client.close();
			}
		} catch (IOException e) {
			Logger.error(String.format("Encountered an exception error while connecting :  %s", e.getMessage()));
		}
		return response;
	}

	@Override
	public Boolean executeDeleteRequest(DeleteRequest deleteRequest) {
		RestHighLevelClient client = null;
		try {
			client = elasticRepository.connectToElasticSearch();
			if (client != null) {
				client.delete(deleteRequest);
				client.close();
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		return Boolean.FALSE;
	}

	@Override
	public SearchResponse executeSearchRequest(SearchRequest searchRequest) {
		RestHighLevelClient client = null;
		try {
			client = elasticRepository.connectToElasticSearch();
			if (client != null) {
				SearchResponse response = client.search(searchRequest);
				client.close();
				return response;
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "executeSearchRequest", e.getMessage()));
		}
		return null;
	}

	@Override
	public SearchResponse executeScrollSearch(SearchScrollRequest searchRequest) {
		RestHighLevelClient client = null;
		try {
			client = elasticRepository.connectToElasticSearch();
			if (client != null) {
				SearchResponse response = client.searchScroll(searchRequest);
				client.close();
				return response;
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "executeScrollSearch", e.getMessage()));
		}
		return null;
	}

	@Override
	public void loadAllDataNodes(String type) {
		try {
			List<Object> queryParams = new ArrayList<>();
			StringBuilder queryBuilder = new StringBuilder(
					Sql.DataNode.GET_ALL_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE + Sql.DataNode.ACTIVE_CONDITION);
			List<Map<String, Object>> result;

			if (StringUtils.isNotBlank(type) && !type.isEmpty()) {
				queryBuilder.append(Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
				queryParams.add(type);
				result = jdbcTemplate.queryForList(queryBuilder.toString(),
						queryParams.toArray(new Object[queryParams.size()]));
			} else {
				result = jdbcTemplate.queryForList(queryBuilder.toString(),
						queryParams.toArray(new Object[queryParams.size()]));
			}
			List<DataNode> dataNodes = nodeAndPropertyMappings(result);
			if (!CollectionUtils.isEmpty(dataNodes)) {
				for (DataNode node : dataNodes) {
					ConfigurationPanel.addDataNode(node, node.getType());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(String.format(EXCEPTION, "loadAllDataNodes", e.getMessage()));
		}
	}

	@Override
	public List<DataNode> nodeAndPropertyMappings(List<Map<String, Object>> result) {
		Map<String, DataNode> dataNodes = new HashMap<>();
		for (Map<String, Object> resultObj : result) {
			Object propertyValue = null;
			Map<String, Object> additionalProperty = new HashMap();
			if (dataNodes.containsKey(resultObj.get(Constants.Parameters.ID))) {
				if (dataNodes.get(resultObj.get(Constants.Parameters.ID)).getAdditionalProperties() != null) {
					additionalProperty = dataNodes.get(resultObj.get(Constants.Parameters.ID))
							.getAdditionalProperties();
					propertyValue = dataNodes.get(resultObj.get(Constants.Parameters.ID)).getAdditionalProperties()
							.get(resultObj.get(Constants.SqlParams.PROP_KEY));
				}
			}
			String propKey = (String) resultObj.get(Constants.SqlParams.PROP_KEY);
			if (StringUtils.isNotBlank(propKey)) {
				if (propKey.equals(Constants.Parameters.COMPETENCY_SOURCE) || propKey.equals(Constants.Parameters.URL)
						|| propKey.equals(Constants.Parameters.FILES)) {
					List<Map<String, Object>> valueList = new ArrayList<>();
					if (propertyValue != null) {
						valueList = (List<Map<String, Object>>) propertyValue;
					}
					Map<String, Object> valueMap = new HashMap<>();
					if (propKey.equals(Constants.Parameters.FILES)) {
						valueMap.put(Constants.Parameters.FILE_TYPE, resultObj.get(Constants.SqlParams.PROP_NAME));
					} else {
						valueMap.put(Constants.Parameters.NAME, resultObj.get(Constants.SqlParams.PROP_NAME));
					}
					valueMap.put(Constants.Parameters.VALUE, resultObj.get(Constants.SqlParams.PROP_VALUE));
					valueList.add(valueMap);
					propertyValue = valueList;
				} else {
					propertyValue = resultObj.get(Constants.SqlParams.PROP_VALUE);
				}
				additionalProperty.put(propKey, propertyValue);
			}

			resultObj.remove(Constants.SqlParams.PROP_KEY);
			resultObj.remove(Constants.SqlParams.PROP_VALUE);
			resultObj.remove(Constants.SqlParams.PROP_NAME);
			DataNode node = new ObjectMapper().convertValue(resultObj, DataNode.class);
			node.setAdditionalProperties(additionalProperty);
			dataNodes.put((String) resultObj.get(Constants.Parameters.ID), node);
		}

		return new ArrayList<DataNode>(dataNodes.values());
	}

	@Override
	public void loadAllNodeMappings() {
		try {
			List<NodeMapping> nodeMap = jdbcTemplate.query(Sql.DataNode.GET_ALL_NODE_MAPPINGS, new Object[] {},
					ConfigurationPanel.rowMapNodeMapping);

			if (nodeMap != null && !nodeMap.isEmpty()) {
				for (NodeMapping node : nodeMap) {
					String mapKey = node.getParentId() + "-" + node.getChild();
					if (ConfigurationPanel.getNodeMapping().containsKey(mapKey)) {
						ConfigurationPanel.getNodeMapping().get(mapKey).getChildIds().add(node.getChildId());
					} else {
						if (StringUtils.isNotBlank(node.getChildId())) {
							List<String> childIds = new ArrayList<>();
							childIds.add(node.getChildId());
							node.setChildIds(childIds);
							node.setChildId(null);
						} else {
							node.setChildIds(new ArrayList<>());
						}
						ConfigurationPanel.getNodeMapping().put(mapKey, node);
					}
				}
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "loadAllNodeMappings", e.getMessage()));
		}
	}

	@Override
	public StorageService getStorageService(String provider) {
		try {
			return jdbcTemplate.queryForObject(Sql.DataNode.GET_STORAGE_SERVICE, new Object[] { provider },
					BeanPropertyRowMapper.newInstance(StorageService.class));

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getStorageService", e.getMessage()));
			return null;
		}

	}

	@Override
	public void setKRResourceSignedURL(DataNode dataNode) {
		try {
			if (StringUtils.isNotBlank(dataNode.getType())
					&& dataNode.getType().equals(Entities.KNOWLEDGERESOURCE.name())) {
				// set file accessible url using file name
				if (dataNode.getAdditionalProperties() != null
						&& dataNode.getAdditionalProperties().get(Constants.Parameters.FILES) != null) {
					List<String> fileNameList = new ArrayList<>();
					List<Map<String, Object>> files = (ArrayList) dataNode.getAdditionalProperties()
							.get(Constants.Parameters.FILES);
					List<Map<String, Object>> uploadedFiles = new ArrayList<>();
					for (Map<String, Object> fileObj : files) {
						Map<String, Object> fileNameURL = new HashMap<>();
						String fileName = (String) fileObj.get(Constants.Parameters.VALUE);
						String fileURL = "";
						if (StringUtils.isNotBlank(fileName)) {
							if (!fileName.contains("https://")) {
								String signedUrl = CloudStore.getSignedURL(fileName);
								if (StringUtils.isNotBlank(signedUrl)) {
									fileURL = signedUrl;
								}
							}
							fileNameURL.put(Constants.Parameters.URL.toLowerCase(), fileURL);
							fileNameURL.put(Constants.Parameters.NAME.toLowerCase(), fileName);

							if (StringUtils.isBlank((String) fileObj.get(Constants.Parameters.FILE_TYPE))) {
								fileObj.put(Constants.Parameters.FILE_TYPE,
										(fileName.substring(fileName.lastIndexOf(".") + 1)).replaceAll("\\?.*", ""));
							}
							fileNameURL.put(Constants.Parameters.FILE_TYPE,
									fileObj.get(Constants.Parameters.FILE_TYPE));
							uploadedFiles.add(fileNameURL);
							fileNameList.add(fileName);
						}
					}
					dataNode.getAdditionalProperties().put(Constants.Parameters.FILES, fileNameList);
					dataNode.getAdditionalProperties().put("krFiles", uploadedFiles);
				}
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "setKRResourceSignedURL", e.getMessage()));
		}
	}

	@Override
	public String getDictionaryLabel(String type) {
		if (type.equals(Entities.POSITION.name())) {
			return "positions";
		} else if (type.equals(Entities.ROLE.name())) {
			return "roles";
		} else if (type.equals(Entities.ACTIVITY.name())) {
			return "activities";
		} else if (type.equals(Entities.COMPETENCY.name())) {
			return "competencies";
		}
		return "";
	}

	@Override
	public List<NodeKeys> getNodeKeys() {
		try {
			return jdbcTemplate.query(Sql.DataNode.GET_NODE_KEYS, ConfigurationPanel.rowMapNodeKeys);
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "setNodeKeys", e.getMessage()));
			return null;
		}
	}

}