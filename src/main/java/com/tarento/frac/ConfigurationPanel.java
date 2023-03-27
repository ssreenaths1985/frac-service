package com.tarento.frac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.tarento.frac.dao.CommonDao;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeKeys;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.StorageService;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.TaggingConstants;

@Component
public class ConfigurationPanel implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPanel.class);

	protected static final Map<String, Map<String, String>> configurationProfileMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> positionsMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> rolesMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> activitiesMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> competenciesMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> krMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> competenciesAreaMap = new ConcurrentHashMap<>();
	protected static final Map<String, DataNode> competencyLevels = new ConcurrentHashMap<>();
	protected static final Map<String, NodeMapping> nodeMappings = new ConcurrentHashMap<>();
	protected static Map<String, List<DataNode>> fracDictionary = new ConcurrentHashMap<>();
	protected static Map<String, NodeKeys> fracKeyMap = new ConcurrentHashMap<>();
	protected static StorageService storageService = new StorageService();

	// Row mappers
	public static final BeanPropertyRowMapper<NodeMapping> rowMapNodeMapping = new BeanPropertyRowMapper<>(
			NodeMapping.class);
	public static final BeanPropertyRowMapper<DataNode> rowMapDataNode = new BeanPropertyRowMapper<>(DataNode.class);
	public static final BeanPropertyRowMapper<NodeKeys> rowMapNodeKeys = new BeanPropertyRowMapper<>(NodeKeys.class);

	private static Gson gson = new Gson();

	@Autowired
	private CommonDao commonDao;

	@Autowired
	private ApplicationProperties appProperties;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		setStorageService();
		loadAllFRACKeys();
		loadAllNodesAndMappings();
		loadDictionary();
	}

	private void loadAllFRACKeys() {
		List<NodeKeys> keyList = commonDao.getNodeKeys();
		if (keyList != null) {
			fracKeyMap = keyList.stream().collect(Collectors.toMap(NodeKeys::getType, Function.identity()));
		}
	}

	private void loadDictionary() {
		try {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().size(100);
			MultiSearchResponse response = commonDao.executeMultiSearchRequest(
					new SearchRequest(appProperties.getDictionary()).source(searchSourceBuilder));
			SearchResponse searchResponse = response.getResponses()[0].getResponse();
			if (searchResponse != null) {
				for (SearchHit hit : searchResponse.getHits()) {
					fracDictionary = gson.fromJson(hit.getSourceAsString(), Map.class);
				}
			}
		} catch (Exception e) {
			LOGGER.error(String.format("Encountered an exception in  loadDictionary:  %s", e.getMessage()));
		}
	}

	private void loadAllNodesAndMappings() {
		commonDao.loadAllDataNodes("");
		commonDao.loadAllNodeMappings();
	}

	public Boolean flushCache(String type) {
		if (StringUtils.isBlank(type) || type.isEmpty()) {
			// flush all
			positionsMap.clear();
			rolesMap.clear();
			activitiesMap.clear();
			competenciesMap.clear();
			krMap.clear();
			competenciesAreaMap.clear();
			competencyLevels.clear();
			nodeMappings.clear();
			fracDictionary.clear();
			fracKeyMap = new ConcurrentHashMap();
			storageService = new StorageService();
		} else if (type.equals(Entities.POSITION.name())) {
			positionsMap.clear();
		} else if (type.equals(Entities.ROLE.name())) {
			rolesMap.clear();
		} else if (type.equals(Entities.ACTIVITY.name())) {
			activitiesMap.clear();
		} else if (type.equals(Entities.COMPETENCY.name())) {
			competenciesMap.clear();
		} else if (type.equals(Entities.KNOWLEDGERESOURCE.name())) {
			krMap.clear();
		} else if (type.equals(Entities.COMPETENCYAREA.name())) {
			competenciesAreaMap.clear();
		} else if (type.equals(Entities.COMPETENCIESLEVEL.name())) {
			competencyLevels.clear();
		} else if (type.equalsIgnoreCase("MAPPINGS")) {
			nodeMappings.clear();
		} else if (type.equalsIgnoreCase("DICTIONARY")) {
			fracDictionary.clear();
		} else if (type.equalsIgnoreCase("FRACKEYS")) {
			fracKeyMap = new ConcurrentHashMap<>();
		} else if (type.equalsIgnoreCase("CLOUD_STORAGE")) {
			storageService = new StorageService();
		} else {
			return Boolean.FALSE;
		}
		LOGGER.info("Data Flushed");
		return Boolean.TRUE;
	}

	public Boolean reloadCache(String type) {
		if (StringUtils.isBlank(type) || type.isEmpty()) {
			// reload all
			setStorageService();
			loadAllFRACKeys();
			loadAllNodesAndMappings();
			loadDictionary();
		} else if (Arrays.stream(Entities.values()).anyMatch(val -> val.name().equals(type))) {
			commonDao.loadAllDataNodes(type);
		} else if (type.equalsIgnoreCase("MAPPINGS")) {
			commonDao.loadAllNodeMappings();
		} else if (type.equalsIgnoreCase("DICTIONARY")) {
			loadDictionary();
		} else if (type.equalsIgnoreCase("FRACKEYS")) {
			loadAllFRACKeys();
		} else if (type.equalsIgnoreCase("CLOUD_STORAGE")) {
			setStorageService();
		} else {
			return Boolean.FALSE;
		}
		LOGGER.info("Data Reloaded");
		return Boolean.TRUE;
	}

	public Map<String, DataNode> getAllNodesFor(String type) {
		if (type.equals(Entities.POSITION.name())) {
			return positionsMap;
		}
		if (type.equals(Entities.ROLE.name())) {
			return rolesMap;
		}
		if (type.equals(Entities.COMPETENCY.name())) {
			return competenciesMap;
		}
		if (type.equals(Entities.ACTIVITY.name())) {
			return activitiesMap;
		}
		if (type.equals(Entities.KNOWLEDGERESOURCE.name())) {
			return krMap;
		}
		if (type.equals(Entities.COMPETENCYAREA.name())) {
			return competenciesAreaMap;
		}
		if (type.equals(Entities.COMPETENCIESLEVEL.name())) {
			return competencyLevels;
		}
		return new HashMap<>();
	}

	public static void addDataNode(DataNode node, String type) {
		if (type.equals(Entities.POSITION.name())) {
			positionsMap.put(node.getId(), node);
		}
		if (type.equals(Entities.ROLE.name())) {
			rolesMap.put(node.getId(), node);
		}
		if (type.equals(Entities.COMPETENCY.name())) {
			competenciesMap.put(node.getId(), node);
		}
		if (type.equals(Entities.ACTIVITY.name())) {
			activitiesMap.put(node.getId(), node);
		}
		if (type.equals(Entities.KNOWLEDGERESOURCE.name())) {
			krMap.put(node.getId(), node);
		}
		if (type.equals(Entities.COMPETENCYAREA.name())) {
			competenciesAreaMap.put(node.getId(), node);
		}
		if (type.equals(Entities.COMPETENCIESLEVEL.name())) {
			competencyLevels.put(node.getId(), node);
		}
	}

	public static void deleteDataNode(String id, String type) {
		if (type.equals(Entities.POSITION.name())) {
			positionsMap.remove(id);
		}
		if (type.equals(Entities.ROLE.name())) {
			rolesMap.remove(id);
		}
		if (type.equals(Entities.COMPETENCY.name())) {
			competenciesMap.remove(id);
		}
		if (type.equals(Entities.ACTIVITY.name())) {
			activitiesMap.remove(id);
		}
		if (type.equals(Entities.KNOWLEDGERESOURCE.name())) {
			krMap.remove(id);
		}
		if (type.equals(Entities.COMPETENCYAREA.name())) {
			competenciesAreaMap.remove(id);
		}
		if (type.equals(Entities.COMPETENCIESLEVEL.name())) {
			competencyLevels.remove(id);
		}
	}

	public List<DataNode> searchKeyword(String field, String keyword, String type) throws CloneNotSupportedException {
		List<DataNode> dataNodeList = new ArrayList<>();
		List<DataNode> searchList = new ArrayList<>();
		if (type.equals(Entities.POSITION.name()))
			searchList = positionsMap.values().stream().collect(Collectors.toList());
		if (type.equals(Entities.ROLE.name()))
			searchList = rolesMap.values().stream().collect(Collectors.toList());
		if (type.equals(Entities.COMPETENCY.name()))
			searchList = competenciesMap.values().stream().collect(Collectors.toList());
		if (type.equals(Entities.ACTIVITY.name()))
			searchList = activitiesMap.values().stream().collect(Collectors.toList());
		if (type.equals(Entities.KNOWLEDGERESOURCE.name()))
			searchList = krMap.values().stream().collect(Collectors.toList());
		if (type.equals(Entities.COMPETENCYAREA.name()))
			searchList = competenciesAreaMap.values().stream().collect(Collectors.toList());
		if (type.equals(Entities.COMPETENCIESLEVEL.name()))
			searchList = competencyLevels.values().stream().collect(Collectors.toList());

		for (DataNode eachNode : searchList) {
			if ((field.equals(Constants.Parameters.NAME) && StringUtils.isNotBlank(eachNode.getName())
					&& (eachNode.getName().contains(keyword.toUpperCase())
							|| eachNode.getName().contains(keyword.toLowerCase())
							|| eachNode.getName().contains(titleCaseConversion(keyword))
							|| eachNode.getName().contains(keyword)))
					|| (field.equals(Constants.Parameters.DESCRIPTION)
							&& StringUtils.isNotBlank(eachNode.getDescription())
							&& (eachNode.getDescription().contains(keyword.toUpperCase())
									|| eachNode.getDescription().contains(keyword.toLowerCase())
									|| eachNode.getDescription().contains(titleCaseConversion(keyword))
									|| eachNode.getDescription().contains(keyword)))) {
				eachNode.setType(type);
				dataNodeList.add(eachNode.clone());
			}

			if (type.equals(Entities.COMPETENCY.name()) && eachNode.getAdditionalProperties() != null) {
				if ((field.equals(Constants.Parameters.COMPETENCIES_AREA)
						&& eachNode.getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA) != null
						&& ((String) eachNode.getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA))
								.toLowerCase().contains(keyword.toLowerCase()))
						|| (field.equals(Constants.Parameters.COMPETENCY_TYPE)
								&& eachNode.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_TYPE) != null
								&& ((String) eachNode.getAdditionalProperties()
										.get(Constants.Parameters.COMPETENCY_TYPE)).toLowerCase()
												.contains(keyword.toLowerCase()))) {
					eachNode.setType(type);
					dataNodeList.add(eachNode.clone());
				}
			}

		}
		return dataNodeList;
	}

	public String titleCaseConversion(String inputString) {
		if (StringUtils.isBlank(inputString)) {
			return "";
		}

		if (StringUtils.length(inputString) == 1) {
			return inputString.toUpperCase();
		}

		StringBuilder resultPlaceHolder = new StringBuilder(inputString.length());

		Stream.of(inputString.split(" ")).forEach(stringPart -> {
			if (stringPart.length() > 1)
				resultPlaceHolder.append(stringPart.substring(0, 1).toUpperCase())
						.append(stringPart.substring(1).toLowerCase());
			else
				resultPlaceHolder.append(stringPart.toUpperCase());

			resultPlaceHolder.append(" ");
		});
		return StringUtils.trim(resultPlaceHolder.toString());
	}

	public synchronized Map<String, NodeKeys> getFRACKeys() {
		return fracKeyMap;
	}

	public Map<String, List<DataNode>> getDictionary() {
		return fracDictionary;
	}

	public void setDictionary(Map<String, List<DataNode>> dictionary) {
		fracDictionary = dictionary;
	}

	public static Map<String, NodeMapping> getNodeMapping() {
		return nodeMappings;
	}

	public Map<String, DataNode> getPositionsmap() {
		return positionsMap;
	}

	public Map<String, DataNode> getRolesmap() {
		return rolesMap;
	}

	public Map<String, DataNode> getActivitiesmap() {
		return activitiesMap;
	}

	public Map<String, DataNode> getCompetenciesmap() {
		return competenciesMap;
	}

	public Map<String, DataNode> getKrmap() {
		return krMap;
	}

	public Map<String, DataNode> getCompetenciesareamap() {
		return competenciesAreaMap;
	}

	public Map<String, DataNode> getCompetencylevels() {
		return competencyLevels;
	}

	private void setStorageService() {
		StorageService services = commonDao.getStorageService(appProperties.getCloudProvider());
		if (services != null) {
			storageService = services;
		}
	}

	public static StorageService getStorageService() {
		return storageService;
	}

	public static String getFRACURLByType(String type, String id) {
		if (Entities.POSITION.name().equals(type)) {
			return "collection-positions/" + id;
		} else if (Entities.ROLE.name().equals(type)) {
			return "collection-roles/" + id;
		} else if (Entities.ACTIVITY.name().equals(type)) {
			return "collection-activities/" + id;
		} else if (Entities.COMPETENCY.name().equals(type)) {
			return "collection-competencies/" + id;
		} else {
			return "collections";
		}
	}

	/**
	 * Appends all the child nodes for the parent node
	 * 
	 * @param dataNode
	 *            DataNode
	 * @param nodeMaps
	 *            Map<String, List<NodeMapping>>
	 */
	public void setChildNodes(DataNode dataNode, Map<String, List<NodeMapping>> nodeMaps, Boolean setDefault) {
		try {
			List<DataNode> childNodeList = new ArrayList<>();
			if (nodeMaps.containsKey(dataNode.getId())) {
				List<NodeMapping> nodeMappingList = nodeMaps.get(dataNode.getId());
				for (NodeMapping nodeMapObj : nodeMappingList) {
					for (String childId : nodeMapObj.getChildIds()) {
						if (getAllNodesFor(nodeMapObj.getChild()) != null) {
							DataNode childNode = getAllNodesFor(nodeMapObj.getChild()).get(childId);
							if (childNode != null) {
								childNode.setType(nodeMapObj.getChild());
								childNodeList.add(childNode.clone());
							}
						}
					}
				}
			}
			if (setDefault && childNodeList.isEmpty() && StringUtils.isNotBlank(dataNode.getType())
					&& dataNode.getType().equals(Entities.COMPETENCY.name())) {
				// set default levels
				childNodeList = TaggingConstants.getDefaultCompetencyLevels();
			}
			// Sort competency level
			if (dataNode.getType().equals(Entities.COMPETENCY.name())) {
				DataNode[] nodeList = childNodeList.toArray(new DataNode[childNodeList.size()]);
				Arrays.sort(nodeList, new Comparator<DataNode>() {
					@Override
					public int compare(DataNode node1, DataNode node2) {
						if (node1.getLevel() != null && node2.getLevel() != null) {
							return node1.getLevel().compareTo(node2.getLevel());
						}
						return 0;
					}
				});
				childNodeList = Arrays.asList(nodeList);
			}
			dataNode.setChildren(childNodeList);
		} catch (Exception e) {
			LOGGER.error(String.format("Exception in %s : %s", "setChildNodes", e.getMessage()));
		}
	}

	/**
	 * Returns a map of node-mapping parentId as key and array of mappings as values
	 * 
	 */
	public Map<String, List<NodeMapping>> getChildMappingList() {
		Map<String, NodeMapping> nodeMapping = getNodeMapping();
		Map<String, List<NodeMapping>> nodeMaps = new HashMap<>();
		for (Map.Entry<String, NodeMapping> entry : nodeMapping.entrySet()) {
			String key = entry.getKey().split("-")[0];
			if (!nodeMaps.containsKey(key)) {
				nodeMaps.put(key, new ArrayList<>());
			}
			nodeMaps.get(key).add(entry.getValue());
		}
		return nodeMaps;
	}

}