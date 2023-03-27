package com.tarento.frac.dao.impl;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.Gson;
import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.CommonDao;
import com.tarento.frac.dao.FRACDao;
import com.tarento.frac.models.Association;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.ExploreNodesMapper;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.FilterMappings;
import com.tarento.frac.models.KeyValue;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeFeedback;
import com.tarento.frac.models.NodeFilter;
import com.tarento.frac.models.NodeKeys;
import com.tarento.frac.models.NodeLogs;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeSource;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.SearchBox;
import com.tarento.frac.models.analytics.AggregateDto;
import com.tarento.frac.models.analytics.ChartType;
import com.tarento.frac.models.analytics.Data;
import com.tarento.frac.models.analytics.Plot;
import com.tarento.frac.repository.ElasticSearchRepository;
import com.tarento.frac.service.impl.RetryTemplate;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.DateUtils;
import com.tarento.frac.utils.PathRoutes;
import com.tarento.frac.utils.Sql;
import com.tarento.frac.utils.TaggingConstants;

import scala.collection.mutable.StringBuilder;

@Repository(Constants.ServiceRepositories.FRAC_DAO)
public class FRACDaoImpl implements FRACDao {

	public static final Logger Logger = LoggerFactory.getLogger(FRACDaoImpl.class);
	String daoImplMarker = Constants.ServiceRepositories.FRAC_DAO + Constants.Markers.DAO_IMPL;
	Marker marker = MarkerFactory.getMarker(daoImplMarker);
	private static final String EXCEPTION = "Exception in %s: %s";
	Gson gson = new Gson();

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private RetryTemplate retryTemplate;

	@Autowired
	private CommonDao commonDao;

	@Autowired
	private ConfigurationPanel configurationPanel;

	@Autowired
	private ElasticSearchRepository elasticRepository;

	@Autowired
	private ApplicationProperties appProperties;

	@Override
	public Boolean addDataNode(DataNode dataNode) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(dataNode.getId());
			queryParams.add(dataNode.getType());
			queryParams.add(dataNode.getName());
			queryParams.add(dataNode.getDescription());
			queryParams.add(dataNode.getStatus());
			queryParams.add(dataNode.getSource());
			queryParams.add(dataNode.getLevel());
			queryParams.add(DateUtils.getCurrentDateTimeInUTC());
			queryParams.add(dataNode.getCreatedBy());

			jdbcTemplate.update(Sql.DataNode.ADD_DATA_NODE, queryParams.toArray(new Object[queryParams.size()]));

			if (dataNode.getAdditionalProperties() != null) {
				List<Object[]> property = new ArrayList<>();
				getAdditionalPropertyObj(dataNode, property);
				jdbcTemplate.batchUpdate(Sql.DataNode.ADD_NODE_PROPERTY, property);
			}

			return true;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "addDataNode", e.getMessage()));
			return false;
		}
	}

	@Override
	public Boolean updateDataNode(DataNode dataNode) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(dataNode.getName());
			queryParams.add(dataNode.getDescription());
			queryParams.add(dataNode.getStatus());
			queryParams.add(dataNode.getSecondaryStatus());
			queryParams.add(dataNode.getSource());
			queryParams.add(dataNode.getLevel());
			queryParams.add(DateUtils.getCurrentDateTimeInUTC());
			queryParams.add(dataNode.getUpdatedBy());
			queryParams.add(dataNode.getId());
			jdbcTemplate.update(Sql.DataNode.UPDATE_DATA_NODE, queryParams.toArray(new Object[queryParams.size()]));

			jdbcTemplate.update(Sql.DataNode.DELETE_NODE_PROPERTY, dataNode.getId());
			if (dataNode.getAdditionalProperties() != null) {
				List<Object[]> property = new ArrayList<>();
				getAdditionalPropertyObj(dataNode, property);
				jdbcTemplate.batchUpdate(Sql.DataNode.ADD_NODE_PROPERTY, property);
			}

			return true;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "updateDataNode", e.getMessage()));
			return false;
		}
	}

	@Override
	public Boolean changeNodeStatusAndComments(DataNode dataNode) {
		try {
			Object[] params = new Object[] { dataNode.getStatus(), dataNode.getSecondaryStatus(),
					dataNode.getReviewComments(), dataNode.getSecondaryReviewComments(), dataNode.getId() };
			jdbcTemplate.update(Sql.DataNode.UPDATE_NODE_STATUS_AND_COMMENTS, params);
			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "changeNodeStatusAndComments", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	@Override
	public List<DataNode> fetchCompetencyAreas() {
		KeyValueList keyValueList = getCompetencyAreaListing("", "");
		List<DataNode> list = new ArrayList<>();
		for (KeyValue keyValue : keyValueList.getKeyValues()) {
			DataNode newDataNode = new DataNode();
			newDataNode.setId(keyValue.getValue().toString());
			newDataNode.setName(keyValue.getKey());
			list.add(newDataNode);
		}
		return list;
	}

	@Override
	public KeyValueList getCompetencyAreaListing(String department, String status) {
		List<KeyValue> keyValues = new LinkedList<>();
		try {
			Map<String, Integer> competencyAreaCountMap = new HashMap<>();
			Map<String, DataNode> competencyMap = configurationPanel.getAllNodesFor(Entities.COMPETENCY.name());
			for (Map.Entry<String, DataNode> entry : competencyMap.entrySet()) {
				if (entry.getValue().getAdditionalProperties() != null && entry.getValue().getAdditionalProperties()
						.get(Constants.Parameters.COMPETENCIES_AREA) != null) {
					if (competencyAreaCountMap.containsKey(
							entry.getValue().getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA))) {
						Integer count = competencyAreaCountMap.get(
								entry.getValue().getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA));
						count = count + 1;
						competencyAreaCountMap.put(entry.getValue().getAdditionalProperties()
								.get(Constants.Parameters.COMPETENCIES_AREA).toString(), count);
					} else {
						Integer count = 1;
						competencyAreaCountMap.put(entry.getValue().getAdditionalProperties()
								.get(Constants.Parameters.COMPETENCIES_AREA).toString(), count);
					}
				}
			}

			Map<String, DataNode> compAreaMap = configurationPanel.getAllNodesFor(Entities.COMPETENCYAREA.name());
			for (Map.Entry<String, DataNode> entry : compAreaMap.entrySet()) {
				if (!competencyAreaCountMap.containsKey(entry.getValue().getName())) {
					competencyAreaCountMap.put(entry.getValue().getName(), 0);
				}
			}

			for (Map.Entry<String, Integer> entry : competencyAreaCountMap.entrySet()) {
				keyValues.add(new KeyValue(entry.getKey(), entry.getValue()));
			}
		} catch (Exception e) {
			Logger.error(String.format("Encountered an exception in getCompetencyAreaListing :  %s", e.getMessage()));
		}
		return new KeyValueList(keyValues);
	}

	@Override
	public Overview exploreSearch(String keyword, String department) {
		try {
			Overview overview = new Overview();
			Map<String, List<NodeMapping>> allMappingsMap = new HashMap<>();
			List<NodeMapping> nodeMap = jdbcTemplate.query(Sql.DataNode.GET_ALL_NODE_MAPPINGS, new Object[] {},
					ConfigurationPanel.rowMapNodeMapping);
			if (nodeMap != null && !nodeMap.isEmpty()) {
				for (NodeMapping eachMapping : nodeMap) {
					if (allMappingsMap.containsKey(eachMapping.getParentId())) {
						List<NodeMapping> childMappingsForParent = allMappingsMap.get(eachMapping.getParentId());
						childMappingsForParent.add(eachMapping);
					} else {
						List<NodeMapping> childMappingsForParent = new ArrayList<>();
						childMappingsForParent.add(eachMapping);
						allMappingsMap.put(eachMapping.getParentId(), childMappingsForParent);
					}
				}
			}

			// Fetching all Individual Nodes for Position, Role, Activity, Competency and
			// Knowledge Resource
			List<String> allIds = new ArrayList<>();
			ExploreNodesMapper mapper = new ExploreNodesMapper();
			Map<String, DataNode> nodes = getAllNodesOf(allIds, keyword, department);
			Iterator<Map.Entry<String, DataNode>> itr = nodes.entrySet().iterator();
			List<DataNode> allNodes = new ArrayList<>();
			while (itr.hasNext()) {
				Entry<String, DataNode> entry = itr.next();
				if (entry.getValue().getType() != null) {
					if (entry.getValue().getType().equals(Entities.POSITION.name())) {
						mapper.getPositionMap().put(entry.getKey(), entry.getValue());
					} else if (entry.getValue().getType().equals(Entities.ROLE.name())) {
						mapper.getRoleMap().put(entry.getKey(), entry.getValue());
					} else if (entry.getValue().getType().equals(Entities.COMPETENCY.name())) {
						mapper.getCompetencyMap().put(entry.getKey(), entry.getValue());
					} else if (entry.getValue().getType().equals(Entities.ACTIVITY.name())) {
						mapper.getActivityMap().put(entry.getKey(), entry.getValue());
					} else if (entry.getValue().getType().equals(Entities.KNOWLEDGERESOURCE.name())) {
						mapper.getKrMap().put(entry.getKey(), entry.getValue());
					}
					allNodes.add(entry.getValue());
				}
			}

			// All Nodes for the response gets populated here
			collectEdgesAndVertices(mapper, allNodes, allMappingsMap, allIds);

			// All Associations for the response, gets populated here.
			List<Association> associations = new ArrayList<>();
			resolveAssociations(allMappingsMap, allIds, associations);
			overview.setDataNodes(allNodes);
			overview.setAssociations(associations);
			return overview;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in exploreSearch  :  %s", e.getMessage()));
			return null;
		}
	}

	void resolveAssociations(Map<String, List<NodeMapping>> allMappingsMap, List<String> allIds,
			List<Association> associations) {
		Iterator<Entry<String, List<NodeMapping>>> itr = allMappingsMap.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, List<NodeMapping>> entry = itr.next();
			for (NodeMapping eachMapping : entry.getValue()) {
				Association association = new Association();
				association.setSource(eachMapping.getParentId());
				association.setTarget(eachMapping.getChildId());
				// Adding a condition to remove the Competency Levels from Association Responses
				if (allIds.contains(eachMapping.getParentId()) && allIds.contains(eachMapping.getChildId())
				// && !eachMapping.getChildId().contains("CIL")
				) {
					associations.add(association);
				}
			}
		}
	}

	void collectEdgesAndVertices(ExploreNodesMapper mapper, List<DataNode> allNodes,
			Map<String, List<NodeMapping>> allMappingsMap, List<String> allIds) {
		for (DataNode eachNode : allNodes) {
			if (allMappingsMap.get(eachNode.getId()) != null) {
				List<NodeMapping> childNodeMappingForParent = allMappingsMap.get(eachNode.getId());
				for (NodeMapping eachMapping : childNodeMappingForParent) {
					if (eachMapping.getChild().equals(Entities.POSITION.name())
							&& mapper.getPositionMap().containsKey(eachMapping.getChildId())) {
						mapper.getPositionMap().get(eachMapping.getChildId());
					} else if (eachMapping.getChild().equals(Entities.ROLE.name())
							&& mapper.getRoleMap().containsKey(eachMapping.getChildId())) {
						mapper.getRoleMap().get(eachMapping.getChildId());
					} else if (eachMapping.getChild().equals(Entities.ACTIVITY.name())
							&& mapper.getActivityMap().containsKey(eachMapping.getChildId())) {
						mapper.getActivityMap().get(eachMapping.getChildId());
					} else if (eachMapping.getChild().equals(Entities.COMPETENCY.name())
							&& mapper.getCompetencyMap().containsKey(eachMapping.getChildId())) {
						mapper.getCompetencyMap().get(eachMapping.getChildId());
					} else if (eachMapping.getChild().equals(Entities.KNOWLEDGERESOURCE.name())
							&& mapper.getKrMap().containsKey(eachMapping.getChildId())) {
						mapper.getKrMap().get(eachMapping.getChildId());
					} else {
						DataNode childNode = getNodeById(eachMapping.getChildId(), Boolean.FALSE, Boolean.FALSE,
								Boolean.FALSE, null);
						if (childNode != null && childNode.getId() != null) {
							allIds.add(childNode.getId());
						}
						addChildNodeToRespectiveList(mapper, childNode, eachMapping.getChild());
					}

				}
			}
		}
	}

	void addChildNodeToRespectiveList(ExploreNodesMapper mapper, DataNode newNode, String type) {
		if (newNode != null && type.equals(Entities.POSITION.name())) {
			mapper.getPositionMap().put(newNode.getId(), newNode);
		} else if (newNode != null && type.equals(Entities.ROLE.name())) {
			mapper.getRoleMap().put(newNode.getId(), newNode);
		} else if (newNode != null && type.equals(Entities.ACTIVITY.name())) {
			mapper.getActivityMap().put(newNode.getId(), newNode);
		} else if (newNode != null && type.equals(Entities.COMPETENCY.name())) {
			mapper.getCompetencyMap().put(newNode.getId(), newNode);
		} else if (newNode != null && type.equals(Entities.KNOWLEDGERESOURCE.name())) {
			mapper.getKrMap().put(newNode.getId(), newNode);
		}
	}

	@Override
	public Boolean reviewMappings(MappingVerification mappingVerification) {
		try {
			String id = mappingVerification.getParentId();
			NodeMapping mapping = ConfigurationPanel.getNodeMapping().get(id);
			if (mappingVerification.getVerified() != null) {
				mapping.setStatus(NodeStatus.VERIFIED.name());
				List<Object> queryParams = new ArrayList<>();
				queryParams.add(mapping.getParent());
				queryParams.add(mapping.getParentId());
				queryParams.add(mapping.getChild());
				queryParams.add(mapping.getChildId());
				queryParams.add(mapping.getStatus());
				jdbcTemplate.update(Sql.DataNode.ADD_MAPPINGS, queryParams.toArray(new Object[queryParams.size()]));
			}
			ConfigurationPanel.getNodeMapping().put(id, mapping);
			return true;
		} catch (Exception e) {
			Logger.error(String.format("Exception in reviewMappings: %s", e.getMessage()));
		}

		return false;
	}

	private HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, "Basic ZWxhc3RpYzpFbGFzdGljMTIz");
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> mediaTypes = new ArrayList<>();
		mediaTypes.add(MediaType.APPLICATION_JSON);
		headers.setAccept(mediaTypes);
		return headers;
	}

	@Override
	public List<DataNode> filterByMappings(FilterList filterList, String userId) {
		List<DataNode> dataNodes = new ArrayList<>();
		List<String> idList = new ArrayList<>();
		try {
			if (StringUtils.isNotBlank(filterList.getType()) && filterList.getMappings() != null) {
				for (FilterMappings mapping : filterList.getMappings()) {
					List<String> filteredIds = new ArrayList<>();
					List<String> filteredTypes = new ArrayList<>();
					List<String> ids = new ArrayList<>();
					List<String> types = new ArrayList<>();
					if (StringUtils.isBlank(mapping.getId()) && StringUtils.isNotBlank(mapping.getName())) {
						mapping.setId(getNodeIdFromName(mapping.getType(), mapping.getName()));
					}
					ids.add(mapping.getId());
					types.add(mapping.getType());
					Boolean firstIteration = true;

					while ((!filteredTypes.contains(filterList.getType()) || types.isEmpty())
							&& (firstIteration || !filteredIds.isEmpty())) {
						if (!firstIteration) {
							ids = filteredIds;
							types = filteredTypes;
						}
						firstIteration = false;
						filteredIds = new ArrayList<>();
						filteredTypes = new ArrayList<>();
						checkNodemappings(mapping.getRelation(), ids, types, filteredIds, filteredTypes, userId);
					}
					idList.addAll(filteredIds);
				}
				dataNodes = filterByMappings(idList, filterList.getType(), userId);

				// get bookmarks detail
				List<DataNode> bookmarksNode = getBookmark(userId, filterList.getType());
				for (DataNode nodesObj : dataNodes) {
					nodesObj.setBookmark(Boolean.FALSE);
					for (DataNode bookmarkObj : bookmarksNode) {
						if (bookmarkObj.getId().equals(nodesObj.getId())) {
							nodesObj.setBookmark(Boolean.TRUE);
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.error(String.format("Exception in filterByMappings: %s", e.getMessage()));
		}
		return dataNodes;
	}

	/**
	 * Returns node Id from name
	 * 
	 * @param type
	 *            String
	 * @param name
	 *            String
	 * @return String
	 * @throws Exception
	 */
	private String getNodeIdFromName(String type, String name) throws Exception {
		String nodeId = "";
		if (configurationPanel.getAllNodesFor(type) != null) {
			Map<String, DataNode> dataNodemap = configurationPanel.getAllNodesFor(type);
			for (Map.Entry<String, DataNode> entry : dataNodemap.entrySet()) {
				if (entry.getValue().getName().equals(name)) {
					nodeId = entry.getKey();
					break;
				}
			}
		}
		return nodeId;
	}

	/**
	 * Returns nodes Id by iterating the dataList
	 */
	private void checkNodemappings(String relation, List<String> ids, List<String> types, List<String> filteredIds,
			List<String> filteredTypes, String userId) {
		Map<String, NodeMapping> nodeMappings = ConfigurationPanel.getNodeMapping();
		for (Map.Entry<String, NodeMapping> nodeMap : nodeMappings.entrySet()) {
			NodeMapping node = nodeMap.getValue();
			String filterType = null;
			if (relation.equals(Constants.Parameters.PARENT) && types.contains(node.getParent())
					&& ids.contains(node.getParentId())) {
				filterType = node.getChild();
				Map<String, DataNode> dataNodesMap = configurationPanel.getAllNodesFor(node.getChild());
				for (String id : node.getChildIds()) {
					if (!filteredIds.contains(id) && dataNodesMap != null && dataNodesMap.containsKey(id)
							&& isUsersDraft(dataNodesMap.get(id), userId)) {
						filteredIds.add(id);
					}
				}
			} else if (relation.equals(Constants.Parameters.CHILD) && types.contains(node.getChild())) {
				Map<String, DataNode> dataNodesMap = configurationPanel.getAllNodesFor(node.getParent());
				filterType = node.getParent();
				for (String id : ids) {
					if (node.getChildIds().contains(id) && (!filteredIds.contains(node.getParentId())
							&& dataNodesMap != null && dataNodesMap.containsKey(node.getParentId())
							&& isUsersDraft(dataNodesMap.get(node.getParentId()), userId))) {
						filteredIds.add(node.getParentId());
					}
				}
			}

			if (StringUtils.isNotBlank(filterType) && !filteredTypes.contains(filterType)) {
				filteredTypes.add(filterType);
			}
		}
	}

	/**
	 * Method to check if the draft node created by the loggedInUser
	 */
	private Boolean isUsersDraft(DataNode node, String userId) {
		return ((StringUtils.isBlank(node.getStatus()) || !node.getStatus().equals(NodeStatus.DRAFT.name()))
				|| (StringUtils.isNotBlank(node.getStatus()) && node.getStatus().equals(NodeStatus.DRAFT.name())
						&& StringUtils.isNotBlank(node.getCreatedBy()) && node.getCreatedBy().equals(userId)))
								? Boolean.TRUE
								: Boolean.FALSE;
	}

	@Override
	public AggregateDto getMyGraphs(String visualizationCode, String userId) {

		if (visualizationCode.equals("myReviewStatus")) {
			String url = appProperties.getElasticFullUrl() + "frac-collection-logs/_search";
			HttpHeaders headers = getHttpHeaders();
			String query = "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"updatedBy.keyword\":\"" + userId
					+ "\"}},{\"wildcard\":{\"reference.keyword\":\"*Review*\"}}]}},\"aggs\":{\"Status Wise\":{\"terms\":{\"field\":\"changeStatement.keyword\",\"size\":10}}}}";
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(query, headers);
			try {
				ResponseEntity<Object> response = retryTemplate.postForEntity(url, requestEntity);
				JsonNode responseNode = new ObjectMapper().convertValue(response.getBody(), JsonNode.class);
				JsonNode aggregationNode = responseNode.get("aggregations");
				ArrayNode bucketNodes = (ArrayNode) aggregationNode.get("Status Wise").get("buckets");
				List<Plot> plotList = new LinkedList<>();
				bucketNodes.forEach(eachNode -> {
					eachNode.toString();
					String status = eachNode.get("key").asText();
					status = status.substring(9, status.length());
					Long count = eachNode.get("doc_count").asLong();
					Plot plot = new Plot();
					plot.setLabel("header");
					plot.setName(status);
					plot.setValue(count);
					plot.setValueLabel("Value");
					plot.setSymbol("number");
					plotList.add(plot);
				});

				List<Data> dataList = new ArrayList<>();
				Data data = new Data();
				data.setHeaderName("Status");
				data.setPlots(plotList);
				dataList.add(data);

				AggregateDto aggregateDto = new AggregateDto();
				aggregateDto.setChartType(ChartType.PIE);
				aggregateDto.setVisualizationCode("myReviewStatus");
				aggregateDto.setDrillDownChartId("none");
				aggregateDto.setData(dataList);
				return aggregateDto;
			} catch (HttpClientErrorException e) {
				Logger.error(String.format("Error in Get My Graphs : %s", e.getMessage()));
			}

		} else if (visualizationCode.equals("subordinateReviewStatus")) {
			String url = appProperties.getElasticFullUrl() + "frac-collection-logs/_search";
			HttpHeaders headers = getHttpHeaders();
			String query = "{\"query\":{\"bool\":{\"must\":[{\"wildcard\":{\"reference.keyword\":\"*Review*\"}}],\"must_not\":[{\"match\":{\"updatedBy.keyword\":\""
					+ userId
					+ "\"}}]}},\"aggs\":{\"Status Wise\":{\"terms\":{\"field\":\"changeStatement.keyword\",\"size\":10}}}}";
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> requestEntity = new HttpEntity<>(query, headers);

			JsonNode responseNode = null;
			try {
				ResponseEntity<Object> response = retryTemplate.postForEntity(url, requestEntity);
				responseNode = new ObjectMapper().convertValue(response.getBody(), JsonNode.class);
				JsonNode aggregationNode = responseNode.get("aggregations");
				ArrayNode bucketNodes = (ArrayNode) aggregationNode.get("Status Wise").get("buckets");
				List<Plot> plotList = new LinkedList<>();
				bucketNodes.forEach(eachNode -> {
					eachNode.toString();
					String status = eachNode.get("key").asText();
					status = status.substring(9, status.length());
					Long count = eachNode.get("doc_count").asLong();
					Plot plot = new Plot();
					plot.setLabel("header");
					plot.setName(status);
					plot.setValue(count);
					plot.setValueLabel("Value");
					plot.setSymbol("number");
					plotList.add(plot);
				});

				List<Data> dataList = new ArrayList<>();
				Data data = new Data();
				data.setHeaderName("Status");
				data.setPlots(plotList);
				dataList.add(data);

				AggregateDto aggregateDto = new AggregateDto();
				aggregateDto.setChartType(ChartType.PIE);
				aggregateDto.setVisualizationCode("subordinateReviewStatus");
				aggregateDto.setDrillDownChartId("none");
				aggregateDto.setData(dataList);

				return aggregateDto;
			} catch (HttpClientErrorException e) {
				Logger.error(String.format("Error in Get My Graphs : %s", e.getMessage()));
			}
		}
		return null;
	}

	@Override
	public List<DataNode> getMapping(String id, String type, String returnType, Boolean isDetail) {
		List<DataNode> nodes = new ArrayList<>();
		List<String> ids = new ArrayList<>(Arrays.asList(id));
		while (!ids.isEmpty()) {
			List<NodeMapping> nodeMap = jdbcTemplate.query(
					"select parent_id as parentId, parent, child_id as childId,"
							+ " child from node_mapping where status = 'verified' and  parent_id in " + getIdQuery(ids),
					new Object[] {}, ConfigurationPanel.rowMapNodeMapping);
			ids.clear();
			if (nodeMap != null && !nodeMap.isEmpty()) {
				ids = nodeMap.stream().map(NodeMapping::getChildId).collect(Collectors.toList());
				for (int i = 0; i < nodeMap.size(); i++) {
					if (nodeMap.get(i).getChild().equalsIgnoreCase(returnType)) {
						if (isDetail) {
							Map<String, DataNode> map = configurationPanel.getAllNodesFor(nodeMap.get(0).getChild());
							if (map.get(nodeMap.get(i).getChildId()) != null)
								nodes.add(map.get(nodeMap.get(i).getChildId()));
						} else {
							DataNode dn = new DataNode();
							dn.setId(nodeMap.get(i).getChildId());
							nodes.add(dn);
						}
					}
				}
				if (!nodes.isEmpty())
					return nodes;
			}
		}
		ids.add(id);
		while (!ids.isEmpty()) {
			List<NodeMapping> nodeMap = jdbcTemplate.query(
					"select parent_id as parentId, parent, child_id as childId,"
							+ " child from node_mapping where status = 'verified' and  child_id in " + getIdQuery(ids),
					new Object[] {}, ConfigurationPanel.rowMapNodeMapping);
			ids.clear();
			if (nodeMap != null && !nodeMap.isEmpty()) {
				ids = nodeMap.stream().map(NodeMapping::getParentId).collect(Collectors.toList());
				for (int i = 0; i < nodeMap.size(); i++) {
					if (nodeMap.get(i).getParent().equalsIgnoreCase(returnType)) {
						if (isDetail) {
							Map<String, DataNode> map = configurationPanel.getAllNodesFor(nodeMap.get(0).getParent());
							if (map.get(nodeMap.get(i).getParentId()) != null)
								nodes.add(map.get(nodeMap.get(i).getParentId()));
						} else {
							DataNode dn = new DataNode();
							dn.setId(nodeMap.get(i).getParentId());
							nodes.add(dn);
						}
					}
				}
				if (!nodes.isEmpty())
					return nodes;
			}
		}
		return new ArrayList<>();
	}

	@Override
	public Boolean addDataNodeBulk(List<DataNode> dataNodes) {
		try {
			if (CollectionUtils.isNotEmpty(dataNodes) && !dataNodes.isEmpty()) {
				List<Object[]> nodes = new ArrayList<>();
				List<Object[]> property = new ArrayList<>();

				for (DataNode dataNode : dataNodes) {
					Object[] nodeObj = { dataNode.getId(), dataNode.getType(), dataNode.getName(),
							dataNode.getDescription(), dataNode.getStatus(), dataNode.getSource(), dataNode.getLevel(),
							DateUtils.getCurrentDateTimeInUTC(), dataNode.getCreatedBy() };
					nodes.add(nodeObj);
					getAdditionalPropertyObj(dataNode, property);
				}
				jdbcTemplate.batchUpdate(Sql.DataNode.ADD_DATA_NODE, nodes);
				jdbcTemplate.batchUpdate(Sql.DataNode.ADD_NODE_PROPERTY, property);
				return true;
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "addDataNodeBulk", e.getMessage()));
		}
		return false;
	}

	@Override
	public Boolean updateDataNodeBulk(List<DataNode> dataNodes) {
		try {
			if (CollectionUtils.isNotEmpty(dataNodes) && !dataNodes.isEmpty()) {
				List<Object[]> nodes = new ArrayList<>();
				List<Object[]> property = new ArrayList<>();
				List<Object[]> nodeId = new ArrayList<>();

				for (DataNode dataNode : dataNodes) {
					Object[] nodeObj = { dataNode.getName(), dataNode.getDescription(), dataNode.getStatus(),
							dataNode.getSecondaryStatus(), dataNode.getSource(), dataNode.getLevel(),
							DateUtils.getCurrentDateTimeInUTC(), dataNode.getUpdatedBy(), dataNode.getId() };
					nodes.add(nodeObj);
					getAdditionalPropertyObj(dataNode, property);
					nodeId.add(new Object[] { dataNode.getId() });

				}

				jdbcTemplate.batchUpdate(Sql.DataNode.UPDATE_DATA_NODE, nodes);
				jdbcTemplate.batchUpdate(Sql.DataNode.DELETE_NODE_PROPERTY, nodeId);
				jdbcTemplate.batchUpdate(Sql.DataNode.ADD_NODE_PROPERTY, property);
				return true;
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "updateDataNodeBulk", e.getMessage()));
		}
		return false;
	}

	/**
	 * Adds the additional property value of the node to an array list
	 * 
	 * @param dataNode
	 *            DataNode
	 * @param property
	 *            List<Object[]>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void getAdditionalPropertyObj(DataNode dataNode, List<Object[]> property) throws Exception {
		if (dataNode.getAdditionalProperties() != null) {
			Map<String, Object> nodeProperties = dataNode.getAdditionalProperties();
			for (Map.Entry<String, Object> entry : nodeProperties.entrySet()) {
				if (entry.getValue() != null && entry.getValue().getClass().equals(String.class)) {
					Object[] propertyObj = { dataNode.getId(), entry.getKey(), null, entry.getValue() };
					property.add(propertyObj);
				} else if (entry.getValue() != null && entry.getValue().getClass().equals(ArrayList.class)
						&& !((ArrayList<Object>) entry.getValue()).isEmpty()) {
					ArrayList<Object> entryValue = (ArrayList<Object>) entry.getValue();
					for (Object value : entryValue) {
						if (value.getClass().equals(String.class)) {
							Object[] propertyObj = { dataNode.getId(), entry.getKey(), null, value };
							property.add(propertyObj);
						} else if (value instanceof Map) {
							Map<String, Object> valueMap = new ObjectMapper().convertValue(value, Map.class);
							if (entry.getKey().equals(Constants.Parameters.FILES)) {
								Object[] propertyObj = { dataNode.getId(), entry.getKey(),
										valueMap.get(Constants.Parameters.FILE_TYPE),
										valueMap.get(Constants.Parameters.NAME) };
								property.add(propertyObj);
							} else {
								Object[] propertyObj = { dataNode.getId(), entry.getKey(),
										valueMap.get(Constants.Parameters.NAME),
										valueMap.get(Constants.Parameters.VALUE) };
								property.add(propertyObj);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Boolean updateNodeStatus(String status, String id) {
		try {
			List<Object> params = new ArrayList<>();
			params.add(status);
			params.add(id);
			jdbcTemplate.update(Sql.DataNode.UPDATE_NODE_STATUS, params.toArray(new Object[params.size()]));
			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "updateNodeStatus", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean setnodeMapping(NodeMapping nodeMapping) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(nodeMapping.getParentId());
			queryParams.add(nodeMapping.getChild());
			jdbcTemplate.update(Sql.DataNode.DELETE_MAPPINGS, queryParams.toArray(new Object[queryParams.size()]));
			List<Object[]> mappings = new ArrayList<>();
			for (String childId : nodeMapping.getChildIds()) {
				Object[] mapObj = { nodeMapping.getParent(), nodeMapping.getParentId(), nodeMapping.getChild(), childId,
						nodeMapping.getStatus() };
				mappings.add(mapObj);
			}
			jdbcTemplate.batchUpdate(Sql.DataNode.ADD_MAPPINGS, mappings);

			return true;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "nodeMapping", e.getMessage()));
			return false;
		}
	}

	@Override
	public DataNode getNodeById(String id, Boolean showSimilar, Boolean isDetail, Boolean bookmarks, String userId) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(id);
			List<Map<String, Object>> result = jdbcTemplate.queryForList(
					Sql.DataNode.GET_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE + Sql.DataNode.DATA_NODE_ID_CONDITION,
					queryParams.toArray(new Object[queryParams.size()]));

			List<DataNode> dataNodes = commonDao.nodeAndPropertyMappings(result);
			if (dataNodes != null && !dataNodes.isEmpty() && dataNodes.get(0) != null
					&& dataNodes.get(0).getId() != null) {
				DataNode node = dataNodes.get(0);
				// Set default source and status if values are null
				if (TaggingConstants.getTaggingMap().get(node.getType()) && StringUtils.isBlank(node.getStatus())) {
					node.setStatus(NodeStatus.UNVERIFIED.name());
				}
				if (StringUtils.isBlank(node.getSource())) {
					node.setSource(NodeSource.ISTM.name());
				}
				if (isDetail != null && isDetail) {
					Map<String, List<NodeMapping>> nodesMap = configurationPanel.getChildMappingList();
					configurationPanel.setChildNodes(node, nodesMap, Boolean.TRUE);
				}
				// bookmark details
				if (bookmarks != null && bookmarks) {
					List<DataNode> bookmarksNode = getBookmark(userId, node.getType());
					node.setBookmark(Boolean.FALSE);
					for (DataNode bookmarkObj : bookmarksNode) {
						if (bookmarkObj.getId().equalsIgnoreCase(id)) {
							node.setBookmark(Boolean.TRUE);
						}
					}
				}
				// Similar node
				if (showSimilar != null && showSimilar) {
					getSimilarNodesForThisNode(node, node.getType());
				}
				commonDao.setKRResourceSignedURL(node);
				return node;
			}
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getNodeById", e.getMessage()));
		}
		return null;
	}

	/**
	 * Sets the additional property values for a node
	 * 
	 * @param dataNode
	 *            DataNode
	 */
	public void getNodeAdditionalProperties(DataNode dataNode) {
		List<Object> queryParams = new ArrayList<>();
		queryParams.add(dataNode.getId());
		try {
			List<Map<String, Object>> properties = jdbcTemplate.queryForList(
					Sql.DataNode.GET_NODE_PROPERTY + Sql.Common.WHERE_CLAUSE + " node_id = ? ",
					queryParams.toArray(new Object[queryParams.size()]));
			Map<String, Object> nodeProperty = new HashMap<>();
			for (Map<String, Object> property : properties) {
				if (!StringUtils.isEmpty((String) property.get(Constants.SqlParams.PROP_KEY))
						&& property.get(Constants.SqlParams.PROP_KEY) != null) {
					nodeProperty.put((String) property.get(Constants.SqlParams.PROP_KEY),
							property.get(Constants.SqlParams.PROP_VALUE));
				}
			}
			dataNode.setAdditionalProperties(nodeProperty);
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getNodeAdditionalProperties", e.getMessage()));
		}
	}

	@Override
	public Long getCountOfNodes(String type, String department, String status, String userType) {
		try {
			StringBuilder query = new StringBuilder(Sql.DataNode.GET_NODE);
			List<Object> params = new ArrayList<>();
			// exclude draft node
			query.append(Sql.Common.WHERE_NOT_DRAFT_CLAUSE);
			// active condition
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION);
			// add type condition
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
			params.add(type);

			// department & status filter
			subQueryBuilderDepartment(department, type, query, params);
			subQueryBuilderReviewerTwo(userType, query, params);
			subQueryBuilderStatus(userType, status, query, params, Sql.Common.AND_CLAUSE);
			subQueryBuilderSecondaryStatus(status, userType, query, params, Sql.Common.AND_CLAUSE);

			List<DataNode> dataNodes = jdbcTemplate.query(query.toString(), params.toArray(new Object[params.size()]),
					ConfigurationPanel.rowMapDataNode);
			return Long.valueOf(dataNodes.size());

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getCountOfNodes", e.getMessage()));
			return null;
		}
	}

	/**
	 * Returns the sub query(with appended AND clause) to filter by department
	 * 
	 * @param department
	 *            String
	 * @param type
	 *            String
	 * @param query
	 *            StringBuilder
	 * @param queryParams
	 *            List<Object>
	 */
	private void subQueryBuilderDepartment(String department, String type, StringBuilder query,
			List<Object> queryParams) {
		if (StringUtils.isNotBlank(department) && !department.equalsIgnoreCase("All departments")
				&& !department.equalsIgnoreCase("All MDO") && StringUtils.isNotBlank(type)
				&& type.equals(Entities.POSITION.name())) {
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.DEPARTMENT_CONDITION);
			queryParams.add(department);
		}
	}

	/**
	 * Sub query(with user type condition check) to filter by primary status
	 * 
	 * @param status
	 *            String
	 * @param query
	 *            StringBuilder
	 * @param queryParams
	 *            List<Object>
	 */
	private void subQueryBuilderStatus(String userType, String status, StringBuilder query, List<Object> queryParams,
			String operator) {
		if (StringUtils.isNotBlank(status)
				&& (StringUtils.isNotBlank(userType) && userType.equals(Constants.UserType.REVIEWER_ONE))) {
			if (status.equals(NodeStatus.UNVERIFIED.name())) {
				query.append(operator + Sql.Common.OPEN_BRACE + Sql.DataNode.STATUS_CONDITION + Sql.Common.OR_CLAUSE
						+ " status IS NULL " + Sql.Common.CLOSE_BRACE);
			} else {
				query.append(operator + Sql.DataNode.STATUS_CONDITION);
			}
			queryParams.add(status);
		}
	}

	/**
	 * Sub query condition to get only the nodes assigned to the fracReviewerTwo
	 * 
	 * @param userType
	 *            String
	 * @param status
	 *            String
	 * @param query
	 *            StringBuilder
	 * @param queryParams
	 *            List<Object>
	 */
	private void subQueryBuilderReviewerTwo(String userType, StringBuilder query, List<Object> queryParams) {
		if (StringUtils.isNotBlank(userType) && userType.equals(Constants.UserType.REVIEWER_TWO)) {
			query.append(Sql.Common.AND_CLAUSE + Sql.Common.OPEN_BRACE + Sql.DataNode.STATUS_CONDITION
					+ Sql.Common.OR_CLAUSE + Sql.Common.OPEN_BRACE + Constants.SqlParams.SECONDARY_STATUS
					+ " IS NOT NULL " + Sql.Common.AND_CLAUSE + Constants.SqlParams.SECONDARY_STATUS + " != ? "
					+ Sql.Common.CLOSE_BRACE + Sql.Common.CLOSE_BRACE);
			queryParams.add(NodeStatus.VERIFIED.name());
			queryParams.add(NodeStatus.UNVERIFIED.name());
		}
	}

	/**
	 * Sub query condition to filter nodes by secondary status
	 * 
	 * @param userType
	 *            String
	 * @param status
	 *            String
	 * @param query
	 *            StringBuilder
	 * @param queryParams
	 *            List<Object>
	 * @throws Exception
	 */
	private void subQueryBuilderSecondaryStatus(String status, String userType, StringBuilder query,
			List<Object> queryParams, String operator) {
		if (StringUtils.isNotBlank(status)
				&& (StringUtils.isBlank(userType) || userType.equals(Constants.UserType.REVIEWER_TWO))) {

			if (status.equals(NodeStatus.DRAFT.name())) {
				subQueryBuilderStatus(Constants.UserType.REVIEWER_ONE, status, query, queryParams, operator);
			} else if (status.equals(NodeStatus.REJECTED.name()) && StringUtils.isBlank(userType)) {
				query.append(operator + Sql.Common.OPEN_BRACE + Sql.DataNode.STATUS_CONDITION + Sql.Common.OR_CLAUSE
						+ Constants.SqlParams.SECONDARY_STATUS + Sql.Common.APPEND_VALUE + Sql.Common.CLOSE_BRACE);
				queryParams.add(status);
				queryParams.add(status);
			} else {
				if (status.equals(NodeStatus.UNVERIFIED.name())) {
					query.append(operator + Sql.Common.OPEN_BRACE + Sql.DataNode.STATUS_CONDITION
							+ Sql.Common.AND_CLAUSE + Sql.Common.OPEN_BRACE + Constants.SqlParams.SECONDARY_STATUS
							+ Sql.Common.APPEND_VALUE + Sql.Common.OR_CLAUSE + Constants.SqlParams.SECONDARY_STATUS
							+ " IS NULL " + Sql.Common.CLOSE_BRACE + Sql.Common.CLOSE_BRACE);
					queryParams.add(NodeStatus.VERIFIED.name());
					queryParams.add(NodeStatus.UNVERIFIED.name());
				} else {
					query.append(operator + Constants.SqlParams.SECONDARY_STATUS + Sql.Common.APPEND_VALUE);
					queryParams.add(status);
				}
			}

		}
	}

	private void getSimilarNodesForThisNode(DataNode node, String type) {
		String nameOfTheNode = node.getName();
		if (nameOfTheNode != null) {
			String[] stringArray = nameOfTheNode.split(" ");
			List<String> splitValues = Arrays.asList(stringArray);
			if (splitValues.isEmpty())
				splitValues.add(nameOfTheNode);
			MultiSearch multiSearch = new MultiSearch();
			List<SearchBox> boxList = new ArrayList<>();
			for (String eachSplitValue : splitValues) {
				SearchBox box = new SearchBox();
				box.setField("name");
				box.setKeyword(eachSplitValue);
				box.setType(type);
				boxList.add(box);
			}
			multiSearch.setSearches(boxList);
			multiSearch.setTool("FRAC");
			List<DataNode> responseNodes = searchNodes(multiSearch);
			Map<String, DataNode> similarMap = new HashMap<>();
			for (DataNode eachNode : responseNodes) {
				if (!StringUtils.isEmpty(eachNode.getId()))
					similarMap.put(eachNode.getId(), eachNode);
			}
			similarMap.remove(node.getId());
			List<DataNode> similarOnes = similarMap.values().stream().collect(Collectors.toCollection(ArrayList::new));
			if (!similarOnes.isEmpty()) {
				node.setSimilarities(similarOnes);
			}
		}

	}

	@Override
	public List<DataNode> searchNodes(MultiSearch multiSearch) {
		try {
			Logger.info("Inside searchNodes DAO layer");
			List<DataNode> searchList = new ArrayList<>();
			List<DataNode> filteredList = new ArrayList<>();

			if (multiSearch.getSearches() != null) {
				// check for status field search
				Boolean statusCheck = Boolean.FALSE;
				String statusFilter = "";
				for (SearchBox box : multiSearch.getSearches()) {
					if (box.getField().equals(Constants.Parameters.STATUS)) {
						statusCheck = Boolean.TRUE;
						statusFilter = box.getKeyword();
					}
				}
				// name & description search
				for (SearchBox box : multiSearch.getSearches()) {
					List<DataNode> dataNodes = new ArrayList<>();
					if (StringUtils.isNotBlank(multiSearch.getTool()) && multiSearch.getTool().equals("FRAC")) {
						dataNodes = configurationPanel.getAllNodesFor(box.getType()).values().stream()
								.collect(Collectors.toList());
					} else {
						Map<String, List<DataNode>> dictionary = configurationPanel.getDictionary();
						if (dictionary.containsKey(commonDao.getDictionaryLabel(box.getType()))) {
							dataNodes = dictionary.get(commonDao.getDictionaryLabel(box.getType()));
						}
					}
					for (int i = 0; i < dataNodes.size(); i++) {
						DataNode node = (new ObjectMapper().convertValue(dataNodes.get(i), DataNode.class)).clone();
						Boolean name = (box.getField().equals(Constants.Parameters.NAME)
								&& StringUtils.isNotBlank(node.getName())
								&& (node.getName().toLowerCase().contains(box.getKeyword().toLowerCase())));
						Boolean description = (box.getField().equals(Constants.Parameters.DESCRIPTION)
								&& StringUtils.isNotBlank(node.getDescription())
								&& (node.getDescription().toLowerCase().contains(box.getKeyword().toLowerCase())));
						// status check
						Boolean status = Boolean.FALSE;
						if (statusCheck && (StringUtils.isBlank(multiSearch.getUserType())
								|| multiSearch.getUserType().equals(Constants.UserType.REVIEWER_TWO))) {
							if ((StringUtils.isNotBlank(node.getSecondaryStatus())
									&& node.getSecondaryStatus().equals(statusFilter))
									|| (StringUtils.isBlank(node.getSecondaryStatus())
											&& statusFilter.equals(NodeStatus.UNVERIFIED.name())
											&& StringUtils.isNotBlank(node.getStatus())
											&& node.getStatus().equals(NodeStatus.VERIFIED.name()))) {
								status = Boolean.TRUE;
							}
						} else {
							if (statusCheck && ((StringUtils.isNotBlank(node.getStatus())
									&& node.getStatus().equals(statusFilter))
									|| (StringUtils.isBlank(node.getStatus())
											&& statusFilter.equals(NodeStatus.UNVERIFIED.name())))) {
								status = Boolean.TRUE;
							}
						}

						if ((name || description) && (!statusCheck || (statusCheck && status))) {
							node.setType(box.getType());
							node.setChildren(new ArrayList<>());
							searchList.add(node);
						}
					}
				}
			}

			// filter search list
			if (multiSearch.getFilter() != null && multiSearch.getFilter().size() > 0) {
				for (int i = 0; i < multiSearch.getFilter().size(); i++) {
					if (i != 0) {
						searchList = filteredList;
						filteredList = new ArrayList<>();
					}
					for (DataNode node : searchList) {
						if (node.getType().equals(Entities.COMPETENCY.name()) && node.getAdditionalProperties() != null
								&& filter(node, multiSearch.getFilter().get(i))) {
							filteredList.add(node);
						}
					}
				}
			} else {
				filteredList = searchList;
			}

			if (multiSearch.getChildCount() != null && multiSearch.getChildCount()) {
				setChildNodeCount(filteredList);
			}
			if (multiSearch.getChildNodes() != null && multiSearch.getChildNodes()) {
				Map<String, List<NodeMapping>> nodesMap = configurationPanel.getChildMappingList();
				for (DataNode nodeObj : filteredList) {
					configurationPanel.setChildNodes(nodeObj, nodesMap, Boolean.TRUE);
				}
			}
			if (StringUtils.isNotBlank(multiSearch.getSort()) && filteredList.size() > 0) {
				DataNode[] nodeList = filteredList.toArray(new DataNode[filteredList.size()]);
				Arrays.sort(nodeList, new Comparator<DataNode>() {
					@Override
					public int compare(DataNode node1, DataNode node2) {
						if (multiSearch.getSort().equals("Descending")) {
							return node2.getName().compareTo(node1.getName());
						}
						return node1.getName().compareTo(node2.getName());
					}
				});
				filteredList = Arrays.asList(nodeList);
			}

			List<DataNode> responseList = new ArrayList<>();
			// remove duplicates from search results
			List<String> responseNodeId = new ArrayList<>();
			for (DataNode node : filteredList) {
				if (!responseNodeId.contains(node.getId())) {
					responseList.add(node);
					responseNodeId.add(node.getId());
				}
			}
			Logger.info("Search node result size : " + responseNodeId.size());
			return responseList;
		} catch (Exception e) {
			Logger.error(String.format("Exception in %s : %s", "searchNodes", e.getMessage()));
			return new ArrayList<>();
		}
	}

	/**
	 * Filter the search Node API response by competency area and type
	 */
	private Boolean filter(DataNode node, NodeFilter filter) {
		if (node.getType().equals(Entities.COMPETENCY.name()) && node.getAdditionalProperties() != null) {
			Boolean competencyArea = (filter.getField().equals(Constants.Parameters.COMPETENCIES_AREA)
					&& node.getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA) != null
					&& filter.getValues().contains(
							(String) node.getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA)));
			Boolean competencyType = (filter.getField().equals(Constants.Parameters.COMPETENCY_TYPE)
					&& node.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_TYPE) != null
					&& filter.getValues().contains(
							(String) node.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_TYPE)));

			if (competencyArea || competencyType) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Set the count of child nodes in node Object
	 * 
	 * @param dataNodes
	 *            List<DataNode>
	 */
	private void setChildNodeCount(List<DataNode> dataNodes) {
		Map<String, NodeMapping> nodeMap = ConfigurationPanel.getNodeMapping();
		for (Map.Entry<String, NodeMapping> entry : nodeMap.entrySet()) {
			String key = entry.getKey().split("-")[0];
			NodeMapping nodeMapping = entry.getValue();
			for (DataNode dataNode : dataNodes) {
				if (dataNode.getId().equalsIgnoreCase(key)) {
					Map<String, Integer> childCountMap = (dataNode.getChildCount() == null) ? new HashMap<>()
							: dataNode.getChildCount();
					childCountMap.put(nodeMapping.getChild(), nodeMapping.getChildIds().size());
					dataNode.setChildCount(childCountMap);
				}
			}
		}
	}

	@Override
	public List<DataNode> getBookmark(String userId, String type) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(userId);
			if (type == null) {
				return jdbcTemplate.query(Sql.Bookmark.GET_BOOKMARK,
						queryParams.toArray(new Object[queryParams.size()]), ConfigurationPanel.rowMapDataNode);
			} else {
				queryParams.add(type);
				return jdbcTemplate.query(
						Sql.Bookmark.GET_BOOKMARK + Sql.Common.AND_CLAUSE + Sql.DataNode.DATA_NODE_TYPE_CONDITION,
						queryParams.toArray(new Object[queryParams.size()]), ConfigurationPanel.rowMapDataNode);
			}

		} catch (Exception e) {
			Logger.error(String.format("Exception in getBookmark: %s", e.getMessage()));
			return new ArrayList<>();
		}
	}

	@Override
	public Boolean bookmarkDataNode(DataNode bookmarkNode, String userId) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(bookmarkNode.getId());
			if (bookmarkNode.getBookmark() == null || bookmarkNode.getBookmark()) {
				queryParams.add(bookmarkNode.getType());
				queryParams.add(userId);
				jdbcTemplate.update(Sql.Bookmark.ADD_BOOKMARK, queryParams.toArray(new Object[queryParams.size()]));
			} else {
				queryParams.add(userId);
				jdbcTemplate.update(Sql.Bookmark.DELETE_BOOKMARK, queryParams.toArray(new Object[queryParams.size()]));
			}
			return true;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "bookmarkDataNode", e.getMessage()));
			return false;
		}
	}

	@Override
	public Boolean verifyLevelOne(DataNodeVerification verification, String userId) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(verification.getStatus());
			queryParams.add(verification.getReviewComments());
			queryParams.add(verification.getSecondaryStatus());
			queryParams.add(userId);
			queryParams.add(DateUtils.getCurrentDateTimeInUTC());
			queryParams.add(verification.getId());

			jdbcTemplate.update(Sql.DataNode.LEVEL_ONE_REVIEW_WITH_SECONDARY,
					queryParams.toArray(new Object[queryParams.size()]));
			configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId())
					.setStatus(verification.getStatus());
			configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId())
					.setSecondaryStatus(verification.getSecondaryStatus());

			return Boolean.TRUE;

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "verifyLevelOne", e.getMessage()));
			return Boolean.FALSE;
		}
	}

	@Override
	public Boolean verifyLevelTwo(DataNodeVerification verification, String userId) {
		List<Object> queryParams = new ArrayList<>();
		try {
			if (StringUtils.isNotBlank(verification.getStatus())) {
				queryParams.add(verification.getStatus());
				queryParams.add(verification.getSecondaryStatus());
				queryParams.add(verification.getSecondaryReviewComments());
				queryParams.add(userId);
				queryParams.add(DateUtils.getCurrentDateTimeInUTC());
				queryParams.add(verification.getId());
				jdbcTemplate.update(Sql.DataNode.LEVEL_TWO_REVIEW_WITH_PRIMARY,
						queryParams.toArray(new Object[queryParams.size()]));
				configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId())
						.setStatus(verification.getStatus());
				configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId())
						.setSecondaryStatus(verification.getSecondaryStatus());
			} else {
				queryParams.add(verification.getSecondaryStatus());
				queryParams.add(verification.getSecondaryReviewComments());
				queryParams.add(userId);
				queryParams.add(DateUtils.getCurrentDateTimeInUTC());
				queryParams.add(verification.getId());
				jdbcTemplate.update(Sql.DataNode.LEVEL_TWO_REVIEW, queryParams.toArray(new Object[queryParams.size()]));
				configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId())
						.setSecondaryStatus(verification.getSecondaryStatus());
			}
			return Boolean.TRUE;

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "verifyLevelTwo", e.getMessage()));
			return Boolean.FALSE;
		}

	}

	@Override
	public List<DataNode> filterDataNodes(FilterList filterList, String userId) {
		try {
			StringBuilder queryBuilder = new StringBuilder(Sql.DataNode.GET_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE
					+ Sql.DataNode.ACTIVE_CONDITION + Sql.Common.AND_CLAUSE + Sql.DataNode.FILTER_BY_USERS_DRAFT);
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(userId);
			// filter by type
			if (StringUtils.isNotBlank(filterList.getType())) {
				queryBuilder.append(Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
				queryParams.add(filterList.getType());
			}
			// department & status filter
			subQueryBuilderDepartment(filterList.getDepartment(), filterList.getType(), queryBuilder, queryParams);
			subQueryBuilderReviewerTwo(filterList.getUserType(), queryBuilder, queryParams);
			subQueryBuilderSecondaryStatus(filterList.getStatus(), filterList.getUserType(), queryBuilder, queryParams,
					Sql.Common.AND_CLAUSE);
			subQueryBuilderStatus(filterList.getUserType(), filterList.getStatus(), queryBuilder, queryParams,
					Sql.Common.AND_CLAUSE);
			// sub filters
			filterListSubQuery(filterList, queryBuilder, queryParams);
			List<Map<String, Object>> result = jdbcTemplate.queryForList(queryBuilder.toString(),
					queryParams.toArray(new Object[queryParams.size()]));
			List<DataNode> dataNodes = commonDao.nodeAndPropertyMappings(result);

			// isDetail
			if (filterList.getIsDetail() != null && filterList.getIsDetail()) {
				Map<String, List<NodeMapping>> nodesMap = configurationPanel.getChildMappingList();
				for (DataNode nodeObj : dataNodes) {
					configurationPanel.setChildNodes(nodeObj, nodesMap, Boolean.TRUE);
				}
			}

			return dataNodes;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "filterDataNodes", e.getMessage()));
			return new ArrayList<>();
		}
	}

	public String getFilterLabel(String label) {
		if (label.equalsIgnoreCase("area")) {
			return Constants.Parameters.COMPETENCIES_AREA;
		} else if (label.equalsIgnoreCase("type")) {
			return Constants.Parameters.COMPETENCY_TYPE;
		} else if (label.equalsIgnoreCase(Constants.Parameters.COD)) {
			return Constants.Parameters.COD;
		} else if (label.equalsIgnoreCase(Constants.Parameters.STATUS)) {
			return Constants.Parameters.STATUS;
		}
		return label;
	}

	@Override
	public Map<String, Object> getAllDataNodes(RequestObject reqObject, String version) {
		try {
			Map<String, Object> responseMap = new HashMap<>();
			List<Object> queryParams = new ArrayList<>();
			StringBuilder queryConditions = new StringBuilder(
					Sql.Common.WHERE_CLAUSE + Sql.DataNode.ACTIVE_CONDITION + Sql.Common.AND_CLAUSE
							+ Sql.DataNode.FILTER_BY_USERS_DRAFT + Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
			queryParams.add(reqObject.getUserId());
			queryParams.add(reqObject.getType());

			// department, userType & status filter
			subQueryBuilderDepartment(reqObject.getDepartment(), reqObject.getType(), queryConditions, queryParams);
			subQueryBuilderReviewerTwo(reqObject.getUserType(), queryConditions, queryParams);
			subQueryBuilderStatus(reqObject.getUserType(), reqObject.getStatus(), queryConditions, queryParams,
					Sql.Common.AND_CLAUSE);
			subQueryBuilderSecondaryStatus(reqObject.getStatus(), reqObject.getUserType(), queryConditions, queryParams,
					Sql.Common.AND_CLAUSE);

			// fetch by created by Id
			if (reqObject.getMyRequest() != null && reqObject.getMyRequest()) {
				queryConditions.append(Sql.Common.AND_CLAUSE + Sql.DataNode.CREATED_BY_CONDITION);
				queryParams.add(reqObject.getUserId());
			}

			// version 2 changes
			if (StringUtils.isNotBlank(version) && version.equals(PathRoutes.EndpointsVersion.V2)) {
				// set total items count in response map
				Integer count = jdbcTemplate.queryForObject(Sql.DataNode.DATA_NODE_COUNT + queryConditions.toString(),
						queryParams.toArray(new Object[queryParams.size()]), Integer.class);
				responseMap.put(Constants.Parameters.COUNT, count);

				// set limit and offset
				queryConditions.append(Sql.Common.LIMIT);
				queryParams.add(
						(reqObject.getLimit() == null) ? Constants.Parameters.DEFAULT_LIMIT : reqObject.getLimit());
				if (reqObject.getOffset() != null) {
					queryConditions.append(Sql.Common.OFFSET);
					queryParams.add(reqObject.getOffset());
				}
			}

			List<Map<String, Object>> result = jdbcTemplate.queryForList(
					Sql.DataNode.GET_NODE_AND_PROPERTY + queryConditions.toString(),
					queryParams.toArray(new Object[queryParams.size()]));

			List<DataNode> dataNodes = commonDao.nodeAndPropertyMappings(result);
			// Set default node source and status if value is null
			for (DataNode node : dataNodes) {
				commonDao.setKRResourceSignedURL(node);
				node.setSource(StringUtils.isBlank(node.getSource()) ? NodeSource.ISTM.name() : node.getSource());
				if (TaggingConstants.getTaggingMap().get(reqObject.getType())
						&& StringUtils.isBlank(node.getStatus())) {
					node.setStatus(NodeStatus.UNVERIFIED.name());
				}
			}

			// In the case of Detailed API fetch, each Data Node and their children are
			// obtained by searching each node's children one by one
			if (reqObject.getIsDetail() != null && reqObject.getIsDetail()) {
				Map<String, List<NodeMapping>> nodesMap = configurationPanel.getChildMappingList();
				for (DataNode nodeObj : dataNodes) {
					configurationPanel.setChildNodes(nodeObj, nodesMap, Boolean.TRUE);
				}
			}
			// get bookmarked nodes
			if (reqObject.getBookmark() != null && reqObject.getBookmark()) {
				List<DataNode> bookmarksNode = getBookmark(reqObject.getUserId(), reqObject.getType());
				for (DataNode nodesObj : dataNodes) {
					nodesObj.setBookmark(Boolean.FALSE);
					for (DataNode bookmarkObj : bookmarksNode) {
						if (bookmarkObj.getId().equals(nodesObj.getId())) {
							nodesObj.setBookmark(Boolean.TRUE);
						}
					}
				}
			}
			responseMap.put(Constants.Parameters.DATA_NODE, dataNodes);
			return responseMap;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getAllDataNodes", e.getMessage()));
			return new HashMap<>();
		}
	}

	@Override
	public Overview exploreAllNodes(String keyword, String department) {
		try {
			// get all node mappings
			Overview overview = new Overview();
			List<NodeMapping> nodeMap = jdbcTemplate.query(Sql.DataNode.GET_MAPPINGS_BY_COLLECTION_TYPE,
					new Object[] {}, ConfigurationPanel.rowMapNodeMapping);
			List<String> nodeId = nodeMap.stream().distinct().map(NodeMapping::getParentId)
					.collect(Collectors.toList());
			nodeId.addAll(nodeMap.stream().distinct().map(NodeMapping::getChildId).collect(Collectors.toList()));
			List<String> nodeIds = nodeId.stream().distinct().collect(Collectors.toList());
			// get nodes by id
			String nodeQuery = Sql.DataNode.GET_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE
					+ Sql.DataNode.ACTIVE_CONDITION + Sql.Common.AND_CLAUSE + "data_node.id in (:idList)";
			MapSqlParameterSource paramMap = new MapSqlParameterSource();
			paramMap.addValue("idList", nodeIds);
			if (StringUtils.isNotBlank(keyword)) {
				nodeQuery = nodeQuery + Sql.Common.AND_CLAUSE + " name like :keyword";
				paramMap.addValue("keyword", "%" + keyword + "%");
			}
			if (StringUtils.isNotBlank(department)) {
				nodeQuery = nodeQuery + Sql.Common.AND_CLAUSE + Constants.SqlParams.PROP_KEY + " = :propKey"
						+ Sql.Common.AND_CLAUSE + Constants.SqlParams.PROP_VALUE + " = :propValue";
				paramMap.addValue("propKey", "Department");
				paramMap.addValue("propValue", department);
			}
			NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
			List<Map<String, Object>> result = namedJdbcTemplate.queryForList(nodeQuery, paramMap);
			List<DataNode> allNodes = commonDao.nodeAndPropertyMappings(result);

			List<String> allIds = allNodes.stream().map(DataNode::getId).collect(Collectors.toList());
			List<Association> associations = new LinkedList<>();
			List<String> newIds = new ArrayList<>();
			for (NodeMapping eachMapping : nodeMap) {
				if (allIds.contains(eachMapping.getParentId()) && allIds.contains(eachMapping.getChildId())) {
					Association association = new Association();
					association.setSource(eachMapping.getParentId());
					association.setTarget(eachMapping.getChildId());
					associations.add(association);
					if (!newIds.contains(eachMapping.getParentId()))
						newIds.add(eachMapping.getParentId());
					if (!newIds.contains(eachMapping.getChildId()))
						newIds.add(eachMapping.getChildId());
				}
			}
			Iterator<DataNode> i = allNodes.iterator();
			while (i.hasNext()) {
				if (!newIds.contains(i.next().getId())) {
					i.remove();
				}
			}
			overview.setDataNodes(allNodes);
			overview.setAssociations(associations);
			return overview;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "exploreAllNodes", e.getMessage()));
			return null;
		}

	}

	public String getIdQuery(final List<String> idList) {
		final StringBuilder query = new StringBuilder("(\'");
		if (!idList.isEmpty()) {
			query.append(idList.get(0) + "\'");
			for (int i = 1; i < idList.size(); i++) {
				query.append(", \'" + idList.get(i) + "\'");
			}
		}
		return query.append(")").toString();
	}

	@Override
	public Map<String, DataNode> getAllNodesOf(List<String> allIds, String keyword, String department) {
		try {
			Map<String, DataNode> allNodeMap = new HashMap<>();
			String query = Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE;
			List<DataNode> dataNodes;
			if (!allIds.isEmpty() && StringUtils.isNotBlank(keyword)) {
				query = query + " id in " + getIdQuery(allIds) + Sql.Common.AND_CLAUSE + " name like '%" + keyword
						+ "%' ";
			} else if ((allIds.isEmpty()) && StringUtils.isNotBlank(keyword)) {
				query = query + " name like '%" + keyword + "%' ";
			} else if (!allIds.isEmpty() && StringUtils.isBlank(keyword)) {
				query = query + " id in " + getIdQuery(allIds);
			}
			if (StringUtils.isNotBlank(department) && !department.equalsIgnoreCase("All departments")
					&& !department.equalsIgnoreCase("All MDO")) {
				List<Object> queryParams = new ArrayList<>();
				queryParams.add(department);
				if ((allIds.isEmpty()) && StringUtils.isBlank(keyword)) {
					query = query + Sql.DataNode.DEPARTMENT_CONDITION;
					dataNodes = jdbcTemplate.query(query, queryParams.toArray(new Object[queryParams.size()]),
							ConfigurationPanel.rowMapDataNode);
				} else {
					query = query + Sql.Common.AND_CLAUSE + Sql.DataNode.DEPARTMENT_CONDITION;
					dataNodes = jdbcTemplate.query(query, queryParams.toArray(new Object[queryParams.size()]),
							ConfigurationPanel.rowMapDataNode);
				}
			} else {
				dataNodes = jdbcTemplate.query(query, new Object[] {}, ConfigurationPanel.rowMapDataNode);
			}
			for (DataNode node : dataNodes) {
				allNodeMap.put(node.getId(), node);
			}
			return allNodeMap;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getAllNodesOf", e.getMessage()));
			return null;
		}

	}

	@Override
	public DataNodesVerificationView getVerificationList(String type, String department, Boolean isDetail,
			String userType) {
		try {
			StringBuilder query = new StringBuilder(Sql.DataNode.GET_NODE);
			// exclude draft node
			query.append(Sql.Common.WHERE_NOT_DRAFT_CLAUSE);
			// to get active nodes
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION);
			// type condition
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(type);
			// department filter
			subQueryBuilderDepartment(department, type, query, queryParams);
			subQueryBuilderReviewerTwo(userType, query, queryParams);
			List<DataNode> dataNodes = jdbcTemplate.query(query.toString(),
					queryParams.toArray(new String[queryParams.size()]), ConfigurationPanel.rowMapDataNode);
			if (dataNodes != null) {
				return getReviewNodes(dataNodes, type, isDetail, userType);
			}

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getVerificationList", e.getMessage()));
			return null;
		}
		return new DataNodesVerificationView();
	}

	/**
	 * To group list of data nodes based on the status
	 * 
	 * @param dataNodes
	 *            List<Datanode>
	 * @param type
	 *            String
	 * @param isDetail
	 *            Boolean
	 * @param userType
	 *            String
	 * @return DataNodesVerificationView
	 */
	private DataNodesVerificationView getReviewNodes(List<DataNode> dataNodes, String type, Boolean isDetail,
			String userType) {

		DataNodesVerificationView view = new DataNodesVerificationView();
		List<DataNode> unverified = new ArrayList<>();
		List<DataNode> verified = new ArrayList<>();
		List<DataNode> rejected = new ArrayList<>();
		// get node mappings
		Map<String, List<NodeMapping>> nodeMaps = null;
		if (isDetail != null && isDetail) {
			nodeMaps = configurationPanel.getChildMappingList();
		}
		// iterate the response to group by status based on the userType
		for (DataNode dataNode : dataNodes) {
			if (nodeMaps != null) {
				configurationPanel.setChildNodes(dataNode, nodeMaps, Boolean.TRUE);
			}
			dataNode.setType(type);
			dataNode.setSource(
					StringUtils.isBlank(dataNode.getSource()) ? NodeSource.ISTM.name() : dataNode.getSource());

			if (StringUtils.isBlank(userType) || userType.equals(Constants.UserType.REVIEWER_TWO)) {
				if (StringUtils.isNotBlank(dataNode.getSecondaryStatus())
						&& dataNode.getSecondaryStatus().equals(NodeStatus.VERIFIED.name())) {
					verified.add(dataNode);
				} else if (StringUtils.isNotBlank(dataNode.getSecondaryStatus())
						&& dataNode.getSecondaryStatus().equals(NodeStatus.REJECTED.name())) {
					rejected.add(dataNode);
				} else if (StringUtils.isBlank(userType) && StringUtils.isBlank(dataNode.getSecondaryStatus())
						&& StringUtils.isNotBlank(dataNode.getStatus())
						&& dataNode.getStatus().equals(NodeStatus.REJECTED.name())) {
					rejected.add(dataNode);
				} else {
					dataNode.setSecondaryStatus(NodeStatus.UNVERIFIED.name());
					unverified.add(dataNode);
					if (StringUtils.isBlank(dataNode.getStatus())) {
						dataNode.setStatus(NodeStatus.UNVERIFIED.name());
					}
				}
			} else if (userType.equals(Constants.UserType.REVIEWER_ONE)) {
				if (StringUtils.isNotBlank(dataNode.getStatus())
						&& dataNode.getStatus().equals(NodeStatus.VERIFIED.name())) {
					verified.add(dataNode);
				} else if (StringUtils.isNotBlank(dataNode.getStatus())
						&& dataNode.getStatus().equals(NodeStatus.REJECTED.name())) {
					rejected.add(dataNode);
				} else {
					dataNode.setStatus(NodeStatus.UNVERIFIED.name());
					unverified.add(dataNode);
				}
			}
		}

		view.setVerified(verified);
		view.setRejected(rejected);
		view.setUnverified(unverified);
		return view;
	}

	@Override
	public List<DataNode> getChildNodes(String parentId, String type) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(parentId);
			queryParams.add(type);

			return jdbcTemplate.query(Sql.DataNode.GET_ALL_CHILD_NODE_BY_TYPE,
					queryParams.toArray(new Object[queryParams.size()]), ConfigurationPanel.rowMapDataNode);
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getChildNodes", e.getMessage()));
		}
		return new ArrayList<>();
	}

	@Override
	public List<DataNode> getParentNodes(String childId, String type) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(childId);
			queryParams.add(type);

			return jdbcTemplate.query(Sql.DataNode.GET_ALL_PARENT_NODE_BY_TYPE,
					queryParams.toArray(new Object[queryParams.size()]), ConfigurationPanel.rowMapDataNode);
		} catch (Exception e) {
			Logger.error(String.format("Encountered Exception in %s: %s", "getChildNodes", e.getMessage()));
		}
		return new ArrayList<>();
	}

	@Override
	public Boolean deleteNode(String id) {
		try {
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(Boolean.FALSE);
			queryParams.add(id);
			// change data node as inactive
			jdbcTemplate.update(Sql.DataNode.UPDATE_NODE_ACTIVE_STATUS,
					queryParams.toArray(new Object[queryParams.size()]));

			queryParams.remove(0);
			new Thread(() -> {
				// delete node property
				jdbcTemplate.update(Sql.DataNode.DELETE_NODE_PROPERTY,
						queryParams.toArray(new Object[queryParams.size()]));
				// delete all bookmark under the id
				jdbcTemplate.update(Sql.Bookmark.DELETE_BOOKMARK_BY_NODE_ID,
						queryParams.toArray(new Object[queryParams.size()]));

				// Delete all mappings
				jdbcTemplate.update(Sql.DataNode.DELETE_CHILD_MAPPING_OF_PARENT,
						queryParams.toArray(new Object[queryParams.size()]));
				jdbcTemplate.update(Sql.DataNode.DELETE_NODE_PARENT_MAPPING,
						queryParams.toArray(new Object[queryParams.size()]));
				jdbcTemplate.update(Sql.DataNode.DELETE_NODE_CHILD_MAPPING,
						queryParams.toArray(new Object[queryParams.size()]));

				// update node mappings in cache
				Map<String, NodeMapping> nodeMappings = ConfigurationPanel.getNodeMapping();
				for (Map.Entry<String, NodeMapping> mapping : nodeMappings.entrySet()) {
					if (mapping.getValue().getParentId().equals(id)) {
						nodeMappings.remove(mapping.getKey());
					}
					if (mapping.getValue().getChildIds().contains(id)) {
						mapping.getValue().getChildIds().remove(id);
					}
				}
			}).start();

			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "deleteNode", e.getMessage()));
			return Boolean.FALSE;
		}
	}

	@Override
	public List<DataNode> filterByMappings(List<String> idList, String type, String userId) {
		try {
			if (!idList.isEmpty()) {
				StringBuilder queryBuilder = new StringBuilder(Sql.DataNode.GET_NODE_AND_PROPERTY);
				MapSqlParameterSource paramMap = new MapSqlParameterSource();

				// draft & active condition
				String draftFilter = Sql.DataNode.FILTER_BY_USERS_DRAFT;
				draftFilter = draftFilter.replace("created_by = ?", "created_by = :userId");
				queryBuilder.append(
						Sql.Common.WHERE_CLAUSE + draftFilter + Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION);
				paramMap.addValue("userId", userId);
				// type condition
				queryBuilder.append(Sql.Common.AND_CLAUSE + "type = :type ");
				paramMap.addValue(Constants.Parameters.TYPE, type);
				// filter by id

				queryBuilder.append(Sql.Common.AND_CLAUSE + "data_node.id in (:nodeIds)");
				paramMap.addValue("nodeIds", idList);

				NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
				List<Map<String, Object>> result = namedJdbcTemplate.queryForList(queryBuilder.toString(), paramMap);
				return commonDao.nodeAndPropertyMappings(result);
			}
			return new ArrayList<>();
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "filterByMappings", e.getMessage()));
			return new ArrayList<>();
		}
	}

	@Override
	public DataNodesVerificationView filterReviewNodes(FilterList filterList) {
		try {
			StringBuilder queryBuilder = new StringBuilder(Sql.DataNode.GET_NODE_AND_PROPERTY);
			List<Object> queryParams = new ArrayList<>();
			// exclude draft node
			queryBuilder.append(Sql.Common.WHERE_NOT_DRAFT_CLAUSE);
			// active condition
			queryBuilder.append(Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION);
			// add type condition
			queryBuilder.append(Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
			queryParams.add(filterList.getType());

			// department filter
			subQueryBuilderDepartment(filterList.getDepartment(), filterList.getType(), queryBuilder, queryParams);
			subQueryBuilderReviewerTwo(filterList.getUserType(), queryBuilder, queryParams);
			subQueryBuilderStatus(filterList.getUserType(), filterList.getStatus(), queryBuilder, queryParams,
					Sql.Common.AND_CLAUSE);
			subQueryBuilderSecondaryStatus(filterList.getStatus(), filterList.getUserType(), queryBuilder, queryParams,
					Sql.Common.AND_CLAUSE);

			filterListSubQuery(filterList, queryBuilder, queryParams);
			List<Map<String, Object>> result = jdbcTemplate.queryForList(queryBuilder.toString(),
					queryParams.toArray(new Object[queryParams.size()]));
			List<DataNode> dataNodes = commonDao.nodeAndPropertyMappings(result);

			if (dataNodes != null) {
				return getReviewNodes(dataNodes, filterList.getType(), filterList.getIsDetail(),
						filterList.getUserType());
			}

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "filterReviewNodes", e.getMessage()));
			return null;
		}
		return new DataNodesVerificationView();
	}

	private void filterListSubQuery(FilterList filterList, StringBuilder query, List<Object> queryParams)
			throws Exception {
		if (filterList.getFilters() != null && !filterList.getFilters().isEmpty()) {
			query.append(Sql.Common.AND_CLAUSE + Sql.Common.OPEN_BRACE);
			Boolean filterApplied = false;

			for (NodeFilter nodeFilter : filterList.getFilters()) {
				for (String value : nodeFilter.getValues()) {
					String operator = (filterApplied) ? Sql.Common.OR_CLAUSE : "";
					filterApplied = true;
					if (nodeFilter.getField().equals(Constants.Parameters.STATUS)) {
						subQueryBuilderStatus(filterList.getUserType(), value, query, queryParams, operator);
						subQueryBuilderSecondaryStatus(value, filterList.getUserType(), query, queryParams, operator);
					} else if (nodeFilter.getField().equals(Constants.Parameters.SOURCE)) {
						query.append(operator + Constants.Parameters.SOURCE + Sql.Common.APPEND_VALUE);
						queryParams.add(value);
					} else {
						query.append(operator + Sql.Common.OPEN_BRACE + Constants.SqlParams.PROP_KEY
								+ Sql.Common.APPEND_VALUE + Sql.Common.AND_CLAUSE + Constants.SqlParams.PROP_VALUE
								+ Sql.Common.APPEND_VALUE + Sql.Common.CLOSE_BRACE);
						queryParams.add(getFilterLabel(nodeFilter.getField()));
						queryParams.add(value);
					}
				}
			}
			query.append(Sql.Common.CLOSE_BRACE);
		}
	}

	@Override
	public List<String> getSourceList(String type) {
		try {
			StringBuilder queryBuilder = new StringBuilder(Sql.DataNode.GET_ALL_UNIQUE_SOURCE);
			queryBuilder.append(Sql.Common.WHERE_CLAUSE + Sql.DataNode.TYPE_CONDITION);
			List<Object> queryParams = new ArrayList<>();
			queryParams.add(type);
			return jdbcTemplate.queryForList(queryBuilder.toString(),
					queryParams.toArray(new Object[queryParams.size()]), String.class);

		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "getSourceList", e.getMessage()));
			return new ArrayList<>();
		}

	}

	@Override
	public Boolean mapNodes(NodeMapping nodeMapping) {
		try {
			nodeMapping.setStatus(NodeStatus.UNVERIFIED.name());
			String mapKey = nodeMapping.getParentId() + "-" + nodeMapping.getChild();
			if (ConfigurationPanel.getNodeMapping().containsKey(mapKey)) {
				List<Object> queryParams = new ArrayList<>();
				nodeMapping.setId(ConfigurationPanel.getNodeMapping().get(mapKey).getId());
				queryParams.add(nodeMapping.getId());
				// Flush all child mappings
				jdbcTemplate.update(Sql.DataNode.DELETE_CHILD_MAPPINGS,
						queryParams.toArray(new Object[queryParams.size()]));
			} else {
				KeyHolder keyHolder = new GeneratedKeyHolder();
				jdbcTemplate.update(new PreparedStatementCreator() {
					@Override
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						String[] returnValue = new String[] { Constants.Parameters.ID };
						PreparedStatement statement = connection.prepareStatement(Sql.DataNode.ADD_PARENT_MAPPINGS,
								returnValue);
						statement.setString(1, nodeMapping.getParent());
						statement.setString(2, nodeMapping.getParentId());
						statement.setString(3, nodeMapping.getChild());
						statement.setString(4, nodeMapping.getStatus());
						return statement;
					}
				}, keyHolder);
				nodeMapping.setId(keyHolder.getKey().longValue());
			}

			// To insert child mappings
			List<Object[]> childMapping = new ArrayList<>();
			for (String childId : nodeMapping.getChildIds()) {
				childMapping.add(new Object[] { nodeMapping.getId(), childId });
			}
			jdbcTemplate.batchUpdate(Sql.DataNode.ADD_CHILD_MAPPINGS, childMapping);

			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format(EXCEPTION, "mapNodes", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	public Extensions getRatingIfExists(String uniqueId) {
		try {
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolBuilder).size(1000);
			boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.UNDERSCORE_ID, uniqueId));

			MultiSearchResponse response = commonDao
					.executeMultiSearchRequest(new SearchRequest(appProperties.getCommentRatingIndex())
							.types(appProperties.getEsDocumentType()).source(searchSourceBuilder));
			SearchResponse searchResponse = response.getResponses()[0].getResponse();
			if (searchResponse != null && searchResponse.getHits() != null) {
				for (SearchHit hit : searchResponse.getHits()) {
					Extensions existingExtension = gson.fromJson(hit.getSourceAsString(), Extensions.class);
					if (existingExtension != null) {
						return existingExtension;
					}
				}
			}
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in getRatingIfExists  :  %s", e.getMessage()));
		}
		return null;
	}

	@Override
	public List<NodeFeedback> getNodeFeedback(String type, String id, String userId) {
		try {
			List<NodeFeedback> nodeFeedback = new ArrayList<>();
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolBuilder).size(1000);
			if (StringUtils.isNotBlank(userId)) {
				String uniqueId = userId.concat("_").concat(type).concat("_").concat(id);
				boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.UNDERSCORE_ID, uniqueId));
			} else {
				boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.ID_KEYWORD, id));
			}
			searchSourceBuilder.fetchSource(new String[] { Constants.Parameters.FEEDBACKS }, new String[] {});
			MultiSearchResponse response = commonDao
					.executeMultiSearchRequest(new SearchRequest(appProperties.getCommentRatingIndex())
							.types(appProperties.getEsDocumentType()).source(searchSourceBuilder));
			if (response != null) {
				SearchResponse searchResponse = response.getResponses()[0].getResponse();
				if (searchResponse != null && searchResponse.getHits() != null) {
					for (SearchHit hit : searchResponse.getHits()) {
						if (hit.getSourceAsMap().containsKey(Constants.Parameters.FEEDBACKS)) {
							NodeFeedback[] feed = new ObjectMapper().convertValue(hit.getSourceAsMap().get("feedbacks"),
									NodeFeedback[].class);
							nodeFeedback.addAll(Arrays.asList(feed));
						}

					}
				}
			}
			return nodeFeedback;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in getNodeFeedback  :  %s", e.getMessage()));
			return new ArrayList<>();
		}
	}

	@Override
	public Extensions getNodeRatingAverage(String type, String id) {
		try {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(0);
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(id)) {
				boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.ID, id));
				boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.TYPE, type));
			}
			String aggName = "avgRating";
			searchSourceBuilder.query(boolBuilder)
					.aggregation(AggregationBuilders.avg(aggName).field(Constants.Parameters.RATING));
			MultiSearchResponse response = commonDao
					.executeMultiSearchRequest(new SearchRequest(appProperties.getCommentRatingIndex())
							.types(appProperties.getEsDocumentType()).source(searchSourceBuilder));
			SearchResponse searchResponse = response.getResponses()[0].getResponse();
			Avg avgAggregation = null;
			if (searchResponse != null && searchResponse.getAggregations() != null) {
				avgAggregation = searchResponse.getAggregations().get(aggName);
			}
			Extensions averageRatingResponse = new Extensions();
			averageRatingResponse.setId(id);
			averageRatingResponse.setType(type);
			if (avgAggregation != null && NumberUtils.isNumber(String.valueOf(avgAggregation.getValue()))) {
				averageRatingResponse.setRating(avgAggregation.getValue());
			}
			return averageRatingResponse;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an exception in getNodeRatingAverage :  %s", e.getMessage()));
			return null;
		}
	}

	@Override
	public List<NodeLogs> getCollectionLogs(String id, String type) {
		try {
			List<NodeLogs> logs = new ArrayList<>();
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(1000);
			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.ID, id));
			if (type != null) {
				boolBuilder.must().add(QueryBuilders.matchQuery(Constants.Parameters.TYPE, type));
			}
			// set documents fields and the order to fetch
			searchSourceBuilder.query(boolBuilder)
					.fetchSource(new String[] {}, new String[] { "earlierStage", "changedStage" })
					.sort(SortBuilders
							.fieldSort(Constants.Parameters.UPDATED_DATE + Constants.Parameters.CONCAT_KEYWORD)
							.order(SortOrder.DESC));
			MultiSearchResponse response = commonDao
					.executeMultiSearchRequest(new SearchRequest(appProperties.getCollectionLogs())
							.types(appProperties.getEsLogsType()).source(searchSourceBuilder));
			if (response != null) {
				SearchResponse searchResponse = response.getResponses()[0].getResponse();
				if (searchResponse != null && searchResponse.getHits() != null) {
					for (SearchHit hit : searchResponse.getHits()) {
						logs.add(new ObjectMapper().convertValue(hit.getSourceAsMap(), NodeLogs.class));
					}
				}
			}
			return logs;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an exception in getCollectionLogs :  %s", e.getMessage()));
			return new ArrayList<>();
		}
	}

	@Override
	public Boolean nodeFeedback(Extensions extension, String user) {
		try {
			if (StringUtils.isNotBlank(extension.getId()) && StringUtils.isNotBlank(extension.getType())
					&& extension.getFeedback() != null) {

				String uniqueId = extension.getUserId().concat("_").concat(extension.getType()).concat("_")
						.concat(extension.getId());
				Extensions existingExtension = getRatingIfExists(uniqueId);
				List<NodeFeedback> nodeComments = new ArrayList<>();
				if (existingExtension != null && existingExtension.getFeedbacks() != null) {
					nodeComments = existingExtension.getFeedbacks();
				}

				NodeFeedback newFeedback = new NodeFeedback();
				newFeedback.setUpdatedDate(DateUtils.getCurrentDateTimeInUTC());
				newFeedback.setUser(user);
				if (StringUtils.isNotBlank(extension.getFeedback().getComments())) {
					newFeedback.setComments(extension.getFeedback().getComments());
				}
				if (extension.getFeedback().getRating() != 0) {
					newFeedback.setRating(extension.getFeedback().getRating());
					extension.setRating(extension.getFeedback().getRating());
				}
				nodeComments.add(newFeedback);
				extension.setFeedbacks(nodeComments);
				extension.setFeedback(null);
				elasticRepository.writeObjectToElastic(extension, uniqueId, appProperties.getCommentRatingIndex(),
						appProperties.getEsDocumentType());
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in nodeFeedback  :  %s", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	@Override
	public List<DataNode> getAllDataNodeByType(String type) {
		try {

			StringBuilder query = new StringBuilder(Sql.DataNode.GET_NODE);
			List<Object> params = new ArrayList<>();
			// exclude draft node
			query.append(Sql.Common.WHERE_NOT_DRAFT_CLAUSE);
			// active condition
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.ACTIVE_CONDITION);
			// add type condition
			query.append(Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION);
			params.add(type);

			return jdbcTemplate.query(query.toString(), params.toArray(new Object[params.size()]),
					ConfigurationPanel.rowMapDataNode);
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in verifyAllDataNode  :  %s", e.getMessage()));
			return null;
		}
	}

	@Override
	public List<DataNode> getPropertyNode(String type) {
		List<DataNode> propertyNodes = new ArrayList<>();
		try {
			List<DataNode> dataNodes = jdbcTemplate.query(Sql.DataNode.GET_PROPERTY_NODE, new Object[] { type, type },
					ConfigurationPanel.rowMapDataNode);
			// remove duplicates
			Map<String, Boolean> nodeMap = new HashMap<>();
			for (DataNode nodeObj : dataNodes) {
				if (!nodeMap.containsKey(nodeObj.getName()) && StringUtils.isNotBlank(nodeObj.getName())) {
					propertyNodes.add(nodeObj);
					nodeMap.put(nodeObj.getName(), Boolean.TRUE);
				}
			}
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in getPropertyNode  :  %s", e.getMessage()));
		}
		return propertyNodes;
	}

	@Override
	public KeyValueList getPropertyCountList(String type) {
		try {
			List<DataNode> dataNodes = jdbcTemplate.query(Sql.DataNode.GET_PROPERTY_NODE, new Object[] { type, type },
					ConfigurationPanel.rowMapDataNode);

			List<KeyValue> keyValues = new LinkedList<>();
			Map<String, Integer> nodeMap = new HashMap<>();
			for (DataNode nodeObj : dataNodes) {
				if (StringUtils.isNotBlank(nodeObj.getName())) {
					if (!nodeMap.containsKey(nodeObj.getName())) {
						nodeMap.put(nodeObj.getName(), nodeObj.getActive() ? 1 : 0);
					} else if (nodeMap.containsKey(nodeObj.getName()) && nodeObj.getActive()) {
						nodeMap.put(nodeObj.getName(), nodeMap.get(nodeObj.getName()) + 1);
					}
				}
			}
			for (Map.Entry<String, Integer> entry : nodeMap.entrySet()) {
				keyValues.add(new KeyValue(entry.getKey(), entry.getValue()));
			}

			return new KeyValueList(keyValues);
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in getPropertyCountList  :  %s", e.getMessage()));
		}
		return null;
	}

	@Override
	public Boolean updateNodeLastInsertedCount(NodeKeys nodeKey) {
		try {
			jdbcTemplate.update(Sql.DataNode.UPDATE_NODE_KEY_COUNT,
					new Object[] { nodeKey.getCount(), nodeKey.getId() });
			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in updateNodeLastInsertedCount  :  %s", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean updateAllNodeLastInsertedCount() {
		try {
			Map<String, NodeKeys> keyList = configurationPanel.getFRACKeys();
			List<Object[]> params = new ArrayList<>();
			for (Map.Entry<String, NodeKeys> nodeKey : keyList.entrySet()) {
				params.add(new Object[] { nodeKey.getValue().getCount(), nodeKey.getValue().getId() });
			}
			jdbcTemplate.batchUpdate(Sql.DataNode.UPDATE_NODE_KEY_COUNT, params);
			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in updateNodeLastInsertedCount  :  %s", e.getMessage()));
		}
		return Boolean.FALSE;
	}

}