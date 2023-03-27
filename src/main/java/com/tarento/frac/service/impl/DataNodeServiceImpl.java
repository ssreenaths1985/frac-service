package com.tarento.frac.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.CommonDao;
import com.tarento.frac.dao.FRACDao;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeKeys;
import com.tarento.frac.models.NodeLogs;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeSource;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.service.ActivityService;
import com.tarento.frac.service.DataNodeService;
import com.tarento.frac.service.FRACDictionaryService;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.DateUtils;
import com.tarento.frac.utils.TaggingConstants;

@Service
public class DataNodeServiceImpl implements DataNodeService {
	public static final Logger LOGGER = LoggerFactory.getLogger(DataNodeServiceImpl.class);

	@Autowired
	private FRACDao fracDao;

	@Autowired
	private ConfigurationPanel configurationPanel;

	@Autowired
	private FRACDictionaryService dictionaryService;

	@Autowired
	private ActivityService activityService;

	@Autowired
	private CommonDao commonDao;

	@Autowired
	private ApplicationProperties appProperties;

	@Override
	public DataNode addDataNode(DataNode dataNode, UserInfo userInfo) {
		try {
			Map<String, Object> nodeMap = setDataNodeDetailsBeforeInsertionOrUpdation(dataNode, userInfo);
			if (nodeMap.get(Constants.Parameters.OPERATION).equals(Constants.Actions.CREATE)) {
				fracDao.addDataNode(dataNode);
				fracDao.updateNodeLastInsertedCount(configurationPanel.getFRACKeys().get(dataNode.getType()));
			} else {
				fracDao.updateDataNode(dataNode);
			}
			activityService.updateNodeLogs(dataNode, userInfo, nodeMap.get(Constants.Parameters.ACTION).toString());
			// Update parent competency status to UNVERIFIED when its competency level gets
			// changed
			if (StringUtils.isNotBlank(dataNode.getType())
					&& dataNode.getType().equals(Entities.COMPETENCIESLEVEL.name())
					&& nodeMap.get(Constants.Parameters.ACTION).equals(Constants.Actions.UPDATE)) {
				activityService.updateParentNodeStatus(dataNode.getId(), userInfo);
			}

		} catch (Exception e) {
			LOGGER.error(String.format("Exception in addDataNode: %s", e.getMessage()));
			return null;
		}
		return dataNode;
	}

	@Override
	public List<DataNode> addDataNodes(List<DataNode> dataNodeList, UserInfo userInfo) {
		List<Map<String, Object>> dataNodes = new ArrayList<>();
		try {
			List<DataNode> toIndex = new ArrayList<>();
			List<DataNode> toUpdate = new ArrayList<>();
			for (DataNode dataNode : dataNodeList) {
				if (Boolean.TRUE.equals(checkUserAccesstoEdit(dataNode, userInfo.getRoles()))) {
					Map<String, Object> mapObj = setDataNodeDetailsBeforeInsertionOrUpdation(dataNode, userInfo);
					if (mapObj.get(Constants.Parameters.OPERATION).equals(Constants.Actions.CREATE)) {
						toIndex.add(dataNode);
					} else if (mapObj.get(Constants.Parameters.OPERATION).equals(Constants.Actions.UPDATE)) {
						toUpdate.add(dataNode);
					}
					dataNodes.add(mapObj);
				}
			}
			Boolean added = fracDao.addDataNodeBulk(toIndex);
			Boolean updated = fracDao.updateDataNodeBulk(toUpdate);
			fracDao.updateAllNodeLastInsertedCount();
			if (added && updated) {
				for (Map<String, Object> node : dataNodes) {
					DataNode nodeObj = new ObjectMapper().convertValue(node.get(Constants.Parameters.DATA_NODE),
							DataNode.class);
					activityService.updateNodeLogs(nodeObj, userInfo, node.get(Constants.Parameters.ACTION).toString());
					// Update Parent Competency status
					if (StringUtils.isNotBlank(nodeObj.getType())
							&& nodeObj.getType().equals(Entities.COMPETENCIESLEVEL.name())
							&& node.get(Constants.Parameters.ACTION).toString()
									.equalsIgnoreCase(Constants.Actions.UPDATE)) {
						activityService.updateParentNodeStatus(nodeObj.getId(), userInfo);
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error(String.format("Exception in addDataNodes:  %s", e.getMessage()));
			return new ArrayList<>();
		}
		return dataNodeList;
	}

	@Override
	public DataNode addDataNodeBulk(DataNode dataNode, UserInfo userInfo) {
		List<DataNode> dataNodes = Arrays.asList(dataNode);
		addDataNodeBulkList(dataNodes, userInfo);
		return dataNodes.get(0);
	}

	@Override
	public List<DataNode> uploadDataNode(File file, UserInfo userInfo) {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			Workbook workbook = new XSSFWorkbook(inputStream);
			inputStream.close();

			Map<String, DataNode> nodeMap = new HashMap<>();
			DataFormatter formatter = new DataFormatter();
			Sheet sheet1 = workbook.getSheetAt(0);
			Iterator<Row> rowIterator1 = sheet1.iterator();
			// read competency data
			while (rowIterator1.hasNext()) {
				try {
					Row row = rowIterator1.next();
					if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(0)))) {
						DataNode dataNode = new DataNode();
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(1))))) {
							dataNode.setId(formatter.formatCellValue(row.getCell(1)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(2))))) {
							dataNode.setType(formatter.formatCellValue(row.getCell(2)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(3))))) {
							dataNode.setName(formatter.formatCellValue(row.getCell(3)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(4))))) {
							dataNode.setDescription(formatter.formatCellValue(row.getCell(4)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(5))))) {
							dataNode.setSource(formatter.formatCellValue(row.getCell(5)));
						}
						Map<String, Object> additionalProp = new HashMap<>();
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(6))))) {
							additionalProp.put(Constants.Parameters.COMPETENCY_TYPE,
									formatter.formatCellValue(row.getCell(6)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(7))))) {
							additionalProp.put(Constants.Parameters.COMPETENCIES_AREA,
									formatter.formatCellValue(row.getCell(7)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(8))))) {
							additionalProp.put(Constants.Parameters.COD, formatter.formatCellValue(row.getCell(8)));
						}
						if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(9))))) {
							additionalProp.put(Constants.Parameters.COMPETENCYSECTOR,
									formatter.formatCellValue(row.getCell(9)));
						}
						// set the existing competency source for the competency update operation
						if (StringUtils.isNotBlank(dataNode.getId())
								&& configurationPanel.getAllNodesFor(dataNode.getType()).get(dataNode.getId()) != null
								&& configurationPanel.getAllNodesFor(dataNode.getType()).get(dataNode.getId())
										.getAdditionalProperties() != null
								&& configurationPanel.getAllNodesFor(dataNode.getType()).get(dataNode.getId())
										.getAdditionalProperties()
										.containsKey(Constants.Parameters.COMPETENCY_SOURCE)) {
							additionalProp.put(Constants.Parameters.COMPETENCY_SOURCE,
									configurationPanel.getAllNodesFor(dataNode.getType()).get(dataNode.getId())
											.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_SOURCE));
						}
						dataNode.setAdditionalProperties(additionalProp);
						dataNode.setChildren(new ArrayList<>());
						nodeMap.put(formatter.formatCellValue(row.getCell(0)), dataNode);
					}
				} catch (Exception e) {
					LOGGER.error(String.format("Exception while reading the first sheet: %s ", e.getMessage()));
				}
			}

			if (workbook.getNumberOfSheets() > 1) {
				Sheet sheet2 = workbook.getSheetAt(1);
				Iterator<Row> rowIterator2 = sheet2.iterator();
				// read competency level data
				while (rowIterator2.hasNext()) {
					try {
						Row row = rowIterator2.next();
						if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(0)))) {
							DataNode level = new DataNode();
							if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(1))))) {
								level.setId(formatter.formatCellValue(row.getCell(1)));
							}
							if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(2))))) {
								level.setType(formatter.formatCellValue(row.getCell(2)));
							}
							if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(3))))) {
								level.setLevel(formatter.formatCellValue(row.getCell(3)));
							}
							if (StringUtils.isNotBlank((formatter.formatCellValue(row.getCell(4))))) {
								level.setName(formatter.formatCellValue(row.getCell(4)));
							}
							if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(5)))) {
								level.setDescription(formatter.formatCellValue(row.getCell(5)));
							}
							nodeMap.get(formatter.formatCellValue(row.getCell(0))).getChildren().add(level);
						}
					} catch (Exception e) {
						LOGGER.error(String.format("Exception while reading the second sheet : %s ", e.getMessage()));
					}
				}
			} else {
				// if competency level sheet is null, append the levels to the competency
				Map<String, List<NodeMapping>> mappingList = configurationPanel.getChildMappingList();
				for (Map.Entry<String, DataNode> entry : nodeMap.entrySet()) {
					configurationPanel.setChildNodes(entry.getValue(), mappingList, Boolean.FALSE);
				}
			}
			addDataNodeBulkList(new ArrayList<>(nodeMap.values()), userInfo);
			return new ArrayList<>(nodeMap.values());
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(String.format("Exception in uploadDataNode : %s ", e.getMessage()));
			return null;
		}

	}

	@Override
	public Boolean deleteNode(String id, String type, UserInfo userInfo) {
		if (StringUtils.isNotBlank(id)) {
			Boolean deleted = fracDao.deleteNode(id);
			if (deleted) {
				// Update activity logs
				DataNode dataNode = new DataNode();
				dataNode.setId(id);
				dataNode.setType(type);
				activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.DELETE);
				dataNode.setStatus(NodeStatus.UNVERIFIED.name());

				dictionaryService.initiateDictionaryPush(dataNode, userInfo);
			}
		}
		return true;

	}

	@Override
	public Boolean checkUserAccesstoEdit(DataNode dataNode, List<String> userRole) {
		if (StringUtils.isNotBlank(dataNode.getId())
				&& configurationPanel.getAllNodesFor(dataNode.getType()).containsKey(dataNode.getId())) {
			DataNode node = configurationPanel.getAllNodesFor(dataNode.getType()).get(dataNode.getId());
			if (StringUtils.isNotBlank(node.getSecondaryStatus())
					&& node.getSecondaryStatus().equals(NodeStatus.VERIFIED.name())) {
				if (userRole != null && !userRole.contains(Constants.UserType.REVIEWER_TWO)
						&& !userRole.contains(Constants.UserType.ADMIN)) {
					return Boolean.FALSE;
				}
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean changeNodeStatusAndComments(DataNode dataNode) {
		return fracDao.changeNodeStatusAndComments(dataNode);
	}

	@Override
	public Boolean mapNodes(NodeMapping nodeMapping, UserInfo userInfo) {
		if (StringUtils.isNotBlank(nodeMapping.getParentId()) && !nodeMapping.getParentId().equals("0")) {
			try {
				Boolean mapped = fracDao.mapNodes(nodeMapping);
				if (mapped) {
					activityService.childNodeLogs(nodeMapping, userInfo);
					return Boolean.TRUE;
				}
			} catch (Exception e) {
				LOGGER.error(String.format("Duplicate entry in mapNodes  :  %s", e.getMessage()));
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean appendMapNodes(NodeMapping newMapping, UserInfo userInfo) {
		try {
			// SearchSourceBuilder searchSourceBuilder = new
			// SearchSourceBuilder().size(1000);
			// NodeMapping existingMapping = new NodeMapping();
			// BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
			// if (StringUtils.isNotBlank(newMapping.getParentId())) {
			// boolBuilder.must()
			// .add(QueryBuilders.matchQuery(Constants.Parameters.PARENT_ID,
			// newMapping.getParentId()));
			// }
			// searchSourceBuilder.query(boolBuilder);
			// MultiSearchResponse response = commonDao
			// .executeMultiSearchRequest(new
			// SearchRequest(appProperties.getNodeMappingIndex())
			// .types(appProperties.getEsDocumentType()).source(searchSourceBuilder));
			// SearchResponse searchResponse = response.getResponses()[0].getResponse();
			// if (searchResponse != null && searchResponse.getHits() != null) {
			// for (SearchHit hit : searchResponse.getHits()) {
			// existingMapping = gson.fromJson(hit.getSourceAsString(), NodeMapping.class);
			// }
			// }
			//
			// for (String eachChildInNewMapping : newMapping.getChildIds()) {
			// existingMapping.getChildIds().add(eachChildInNewMapping);
			// }
			// mapNodes(existingMapping, userInfo);
		} catch (Exception e) {
			LOGGER.error(String.format("Encountered an error in appendMapNodes  :  %s", e.getMessage()));
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private List<DataNode> addDataNodeBulkList(List<DataNode> dataNodes, UserInfo userInfo) {
		try {
			List<DataNode> toIndex = new ArrayList<>();
			List<DataNode> toUpdate = new ArrayList<>();
			Map<String, NodeMapping> nodeMap = new HashMap<>();

			Map<String, List<Map<String, Object>>> childMappings = new HashMap<>();
			Map<Integer, Map<String, Object>> parentObj = new HashMap<>();

			for (int i = 0; i < dataNodes.size(); i++) {
				DataNode dataNode = dataNodes.get(i);
				List<Map<String, Object>> childMaps = new ArrayList<>();
				Map<String, Object> nodeObj = setDataNodeDetailsBeforeInsertionOrUpdation(dataNode, userInfo);
				if (nodeObj != null) {
					DataNode parentNode = dataNode.clone();
					parentNode.setChildren(null);
					nodeObj.put(Constants.Parameters.DATA_NODE, parentNode);
					if (nodeObj.get(Constants.Parameters.OPERATION).equals(Constants.Actions.CREATE)) {
						toIndex.add(parentNode);
					} else if (nodeObj.get(Constants.Parameters.OPERATION).equals(Constants.Actions.UPDATE)) {
						toUpdate.add(parentNode);
					}
					parentObj.put(i, nodeObj);
					// child Node
					if (dataNode.getChildren() != null && !dataNode.getChildren().isEmpty()) {
						for (DataNode childNode : dataNode.getChildren()) {
							Map<String, Object> childNodeObj = setDataNodeDetailsBeforeInsertionOrUpdation(childNode,
									userInfo);
							if (childNodeObj.get(Constants.Parameters.OPERATION).equals(Constants.Actions.CREATE)) {
								toIndex.add(childNode);
							} else if (childNodeObj.get(Constants.Parameters.OPERATION)
									.equals(Constants.Actions.UPDATE)) {
								toUpdate.add(childNode);
							}
							childMaps.add(childNodeObj);
							// Node Mappings
							String nodeMapKey = dataNode.getId() + "-" + childNode.getType();
							if (nodeMap.containsKey(nodeMapKey)) {
								nodeMap.get(nodeMapKey).getChildIds().add(childNode.getId());
							} else {
								NodeMapping nodeMapping = new NodeMapping();
								nodeMapping.setParent(dataNode.getType());
								nodeMapping.setParentId(dataNode.getId());
								nodeMapping.setChild(childNode.getType());
								List<String> childId = new ArrayList<>();
								childId.add(childNode.getId());
								nodeMapping.setChildIds(childId);
								nodeMap.put(nodeMapKey, nodeMapping);
							}
						}
					} else {
						// if child array is empty remove the old mappings
						for (String entity : TaggingConstants.getChildTaggingMap().get(dataNode.getType())) {
							String nodeMapKey = dataNode.getId() + "-" + entity;
							NodeMapping nodeMapping = new NodeMapping();
							nodeMapping.setParent(dataNode.getType());
							nodeMapping.setParentId(dataNode.getId());
							nodeMapping.setChild(entity);
							nodeMapping.setChildIds(new ArrayList<>());
							nodeMap.put(nodeMapKey, nodeMapping);
						}
					}
					childMappings.put(parentNode.getId(), childMaps);
				}
			}

			Boolean added = fracDao.addDataNodeBulk(toIndex);
			Boolean updated = fracDao.updateDataNodeBulk(toUpdate);
			fracDao.updateAllNodeLastInsertedCount();

			if (added || updated) {
				// update Activity logs
				List<DataNode> allNodes = new ArrayList<>();
				for (int i = 0; i < dataNodes.size(); i++) {
					DataNode dataNode = dataNodes.get(i);
					DataNode parentNode = dataNode.clone();
					parentNode.setChildren(new ArrayList<>());
					configurationPanel.setChildNodes(parentNode, configurationPanel.getChildMappingList(),
							Boolean.FALSE);
					allNodes.add(parentNode);
				}
				activityService.bulkUpdateNodeLogs(allNodes, childMappings, parentObj, userInfo);
				for (Map.Entry<String, NodeMapping> entry : nodeMap.entrySet()) {
					// update mappings
					fracDao.mapNodes(entry.getValue());
					ConfigurationPanel.getNodeMapping().put(entry.getKey(), entry.getValue());
				}
				// push audit logs
				List<DataNodeVerification> dv = new ArrayList<>();
				for (DataNode d : toIndex) {
					if (!d.getType().equalsIgnoreCase(Entities.COMPETENCIESLEVEL.toString())) {
						DataNodeVerification verification = new DataNodeVerification();
						verification.setType(d.getType());
						verification.setId(d.getId());
						verification.setName(d.getName());
						verification.setSecondaryStatus(d.getSecondaryStatus());
						dv.add(verification);
					}
				}
				Executors.newSingleThreadExecutor()
						.submit(activityService.updateAuditData(dv, userInfo, Boolean.FALSE));
			}

		} catch (Exception e) {
			LOGGER.error(String.format("Exception in addDataNodeBulk:  %s", e.getMessage()));
		}
		return dataNodes;
	}

	/**
	 * Method sets and returns the meta data for data node object
	 * 
	 * @param dataNode
	 *            DataNode
	 * @param userInfo
	 *            userInfo
	 * @return Map<String, Object>
	 */
	private Map<String, Object> setDataNodeDetailsBeforeInsertionOrUpdation(DataNode dataNode, UserInfo userInfo) {
		Map<String, Object> nodeObj = new HashMap<>();
		dataNode.setSource(
				StringUtils.isNotBlank(dataNode.getSource()) ? dataNode.getSource() : NodeSource.ISTM.name());
		setNodeStatus(dataNode);
		if (StringUtils.isBlank(dataNode.getId())) {
			LOGGER.info("Node Id is empty : " + dataNode.getId());
			dataNode.setId(getNewNodeId(dataNode.getType()));
			LOGGER.info("New node Id is  : " + dataNode.getId());
			dataNode.setCreatedDate(DateUtils.getCurrentDateTimeInUTC());
			dataNode.setCreatedBy(userInfo.getSub());

			if (StringUtils.isNotBlank(dataNode.getStatus()) && dataNode.getStatus().equals(NodeStatus.DRAFT.name())) {
				nodeObj.put(Constants.Parameters.ACTION, Constants.Actions.CREATE);
			} else {
				nodeObj.put(Constants.Parameters.ACTION, Constants.Actions.PUBLISH);
			}
			nodeObj.put(Constants.Parameters.OPERATION, Constants.Actions.CREATE);
			nodeObj.put(Constants.Parameters.DATA_NODE, dataNode);
			return nodeObj;
		} else {
			if (StringUtils.isNotBlank(dataNode.getType())
					&& configurationPanel.getAllNodesFor(dataNode.getType()) != null
					&& configurationPanel.getAllNodesFor(dataNode.getType()).containsKey(dataNode.getId())) {
				DataNode nodeMap = configurationPanel.getAllNodesFor(dataNode.getType()).get(dataNode.getId());
				dataNode.setUpdatedDate(DateUtils.getCurrentDateTimeInUTC());
				dataNode.setUpdatedBy(userInfo.getSub());
				nodeObj.put(Constants.Parameters.ACTION, Constants.Actions.UPDATE);
				if (StringUtils.isNotBlank(nodeMap.getStatus()) && StringUtils.isNotBlank(dataNode.getStatus())
						&& nodeMap.getStatus().equals(NodeStatus.DRAFT.name())
						&& !dataNode.getStatus().equals(nodeMap.getStatus())) {
					nodeObj.put(Constants.Parameters.ACTION, Constants.Actions.PUBLISH);
				}
				nodeObj.put(Constants.Parameters.DATA_NODE, dataNode);
				nodeObj.put(Constants.Parameters.OPERATION, Constants.Actions.UPDATE);
				return nodeObj;
			}
		}
		return null;
	}

	/**
	 * Set the status of the object to unverified(for new or updated node),
	 * draft(request to save as draft)
	 * 
	 * @param dataNode
	 */
	private void setNodeStatus(DataNode dataNode) {
		if (StringUtils.isNotBlank(dataNode.getType())) {
			if (StringUtils.isNotBlank(dataNode.getStatus())
					&& dataNode.getStatus().equalsIgnoreCase(NodeStatus.DRAFT.name())
					&& TaggingConstants.getDraftNodeMap().get(dataNode.getType())) {
				dataNode.setStatus(NodeStatus.DRAFT.name());
			} else if (StringUtils.isBlank(dataNode.getStatus())
					&& TaggingConstants.getTaggingMap().get(dataNode.getType())) {
				dataNode.setSecondaryStatus(NodeStatus.UNVERIFIED.name());
				dataNode.setStatus(NodeStatus.UNVERIFIED.name());
			} else if (StringUtils.isNotBlank(dataNode.getStatus())
					&& !dataNode.getStatus().equalsIgnoreCase(NodeStatus.DRAFT.name())
					&& TaggingConstants.getTaggingMap().get(dataNode.getType())) {
				dataNode.setSecondaryStatus(NodeStatus.UNVERIFIED.name());
				dataNode.setStatus(NodeStatus.UNVERIFIED.name());
			} else {
				dataNode.setStatus(null);
			}
		}
	}

	/**
	 * Returns the unique Id for new node
	 * 
	 * @param type
	 *            String
	 * @return String
	 */
	private String getNewNodeId(String type) {
		Map<String, NodeKeys> keys = configurationPanel.getFRACKeys();
		String prefix = "";
		Long nodeId = 0l;
		if (keys.containsKey(type)) {
			prefix = keys.get(type).getPrefix();
			nodeId = keys.get(type).getCount() + 1;
			keys.get(type).setCount(nodeId);
			LOGGER.info("*** New node id : " + nodeId);
		}

		return prefix + nodeId;
	}

	@Override
	public void getReviewedNode(Map<String, Object> search, UserInfo userInfo) {
		new Thread(() -> {
			try {
				// query builder
				if (search != null && search.size() > 0) {
					BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolBuilder).size(1000);
					for (Map.Entry<String, Object> entry : search.entrySet()) {
						if (entry.getValue() instanceof List) {
							for (Object object : (List) entry.getValue()) {
								boolBuilder.should().add(QueryBuilders.matchQuery(entry.getKey(), object));
							}
						} else {
							boolBuilder.should().add(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
						}
					}
					// search request
					SearchResponse searchResponse = commonDao.executeSearchRequest(
							new SearchRequest(appProperties.getCollectionLogs()).types(appProperties.getEsLogsType())
									.source(searchSourceBuilder).scroll(TimeValue.timeValueMinutes(10L)));
					SearchHit[] hits = searchResponse.getHits().getHits();
					LOGGER.info("No of documents to process : " + searchResponse.getHits().totalHits);
					// scroll search
					do {
						List<DataNodeVerification> verificationList = new ArrayList<>();
						for (SearchHit hit : hits) {
							NodeLogs obj = new ObjectMapper().convertValue(hit.getSourceAsMap(), NodeLogs.class);
							DataNodeVerification verificationObj = new DataNodeVerification();
							verificationObj.setId(obj.getId());
							verificationObj.setType(obj.getType());
							verificationObj.setVerified(obj.getChangeStatement().contains(NodeStatus.VERIFIED.name())
									&& !obj.getChangeStatement().contains(NodeStatus.UNVERIFIED.name()));
							verificationObj.setReviewComments(obj.getComments());
							verificationObj.setReviewedBy(obj.getUpdatedBy());
							verificationObj.setReviewedDate(obj.getUpdatedDate());
							verificationList.add(verificationObj);
						}
						// Telemetry event call
						Callable callableObj = activityService.updateAuditData(verificationList, userInfo,
								Boolean.TRUE);
						ExecutorService executorService = Executors.newSingleThreadExecutor();
						Future<Boolean> futureValue = executorService.submit(callableObj);
						LOGGER.info((futureValue != null && futureValue.equals(Boolean.TRUE) ? "Processed "
								: "Failed to process ") + searchResponse.getHits().getHits().length + " documents");
						// scroll search
						if (!StringUtils.isEmpty(searchResponse.getScrollId())) {
							searchResponse = commonDao.executeScrollSearch(new SearchScrollRequest()
									.scroll(TimeValue.timeValueMinutes(10L)).scrollId(searchResponse.getScrollId()));
						}

					} while (searchResponse.getHits().getHits().length != 0);

				}
			} catch (Exception e) {
				LOGGER.error(String.format("Encountered an error in triggerAuditEvent  :  %s", e.getMessage()));
			}
		}).start();
	}
}