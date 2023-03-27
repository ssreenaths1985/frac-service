package com.tarento.frac.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.FRACDao;
import com.tarento.frac.kafka.producer.KafkaProducer;
import com.tarento.frac.models.Actor;
import com.tarento.frac.models.Audit;
import com.tarento.frac.models.CbObject;
import com.tarento.frac.models.Context;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.Edata;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeLogs;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.Objects;
import com.tarento.frac.models.Pdata;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.repository.ElasticSearchRepository;
import com.tarento.frac.service.ActivityService;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.AuthUtil;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.DateUtils;

import scala.collection.mutable.StringBuilder;

@Service
public class ActivityServiceImpl implements ActivityService {

	public static final Logger Logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

	@Autowired
	private ConfigurationPanel configurationPanel;

	@Autowired
	private ApplicationProperties appProperties;

	@Autowired
	private ElasticSearchRepository elasticRepository;

	@Autowired
	private KafkaProducer kafkaProducer;

	@Autowired
	private AuthUtil authUtil;

	@Autowired
	private FRACDao fracDao;

	@Override
	public Boolean updateNodeLogs(DataNode dataNode, UserInfo userInfo, String action) {
		new Thread(() -> {
			try {
				if (StringUtils.isNotBlank(dataNode.getId()) && !dataNode.getId().equals("0")) {
					NodeLogs nodeLogs = new NodeLogs();
					nodeLogs.setId(dataNode.getId());
					nodeLogs.setType(dataNode.getType());
					nodeLogs.setUpdatedBy(userInfo.getSub());
					nodeLogs.setUpdatedByEmail(userInfo.getEmail());
					nodeLogs.setUpdatedDate(DateUtils.getCurrentDateTimeInUTC());
					nodeLogs.setUser(userInfo.getName());

					if (action.equals(Constants.Actions.UPDATE)) {
						Map<String, DataNode> nodeMap = configurationPanel.getAllNodesFor(dataNode.getType());
						if (nodeMap != null && nodeMap.containsKey(dataNode.getId())) {
							DataNode earlierStage = getAuditDataNode(nodeMap.get(dataNode.getId()));
							DataNode changedStage = getAuditDataNode(dataNode);
							nodeLogs.setEarlierStage(earlierStage);
							nodeLogs.setChangedStage(changedStage);
							findUpdatedField(earlierStage, changedStage, nodeLogs);
						}
						ConfigurationPanel.addDataNode(dataNode, dataNode.getType());
					} else if (action.equals(Constants.Actions.CREATE)) {
						nodeLogs.setReference(Constants.Actions.CREATE);
						nodeLogs.setChangeStatement(Constants.Parameters.ITEM_CREATED);
						ConfigurationPanel.addDataNode(dataNode, dataNode.getType());
					} else if (action.equals(Constants.Actions.PUBLISH)) {
						nodeLogs.setReference(Constants.Actions.PUBLISH);
						nodeLogs.setChangeStatement("Item published");
						if (StringUtils.isNotBlank(dataNode.getSource())) {
							nodeLogs.setChangeStatement(
									nodeLogs.getChangeStatement() + " from " + dataNode.getSource());
						}
						ConfigurationPanel.addDataNode(dataNode, dataNode.getType());
					} else if (action.equals(Constants.Actions.REVIEW_BOARD)
							|| action.equals(Constants.Actions.TECHNICAL_REVIEW)
							|| action.equals(Constants.Actions.REVIEW)) {
						nodeLogs.setReference(action);
						nodeLogs.setChangeStatement("Item " + dataNode.getStatus());
						nodeLogs.setComments(dataNode.getReviewComments());
						// configuration panel updated in dao layer
					} else if (action.equals(Constants.Actions.DELETE)) {
						nodeLogs.setReference(Constants.Actions.DELETE);
						nodeLogs.setChangeStatement("Item deleted");
						ConfigurationPanel.deleteDataNode(dataNode.getId(), dataNode.getType());
					}

					if (StringUtils.isNotBlank(nodeLogs.getChangeStatement())) {
						String id = dataNode.getId() + "_" + Calendar.getInstance().getTimeInMillis();
						elasticRepository.writeObjectToElastic(nodeLogs, id, appProperties.getCollectionLogs(),
								appProperties.getEsLogsType());
					}
				}
			} catch (Exception e) {
				Logger.error(String.format("Encountered an exception in updateNodeLogs method :  %s", e.getMessage()));
			}
		}).start();
		return Boolean.TRUE;
	}

	@Override
	public void childNodeLogs(NodeMapping nodeMapping, UserInfo userInfo) {
		new Thread(() -> {
			try {
				String nodeMapId = nodeMapping.getParentId() + "-" + nodeMapping.getChild();
				if (StringUtils.isNotBlank(nodeMapping.getParentId()) && !nodeMapping.getParentId().equals("0")
						&& (!nodeMapping.getChild().equalsIgnoreCase(Entities.COMPETENCIESLEVEL.name())
								|| ConfigurationPanel.getNodeMapping().containsKey(
										nodeMapping.getParentId() + "-" + Entities.COMPETENCIESLEVEL.name()))) {
					// Create nodelog document object
					NodeLogs nodeLogs = new NodeLogs();
					nodeLogs.setId(nodeMapping.getParentId());
					nodeLogs.setType(nodeMapping.getParent());
					nodeLogs.setUpdatedBy(userInfo.getSub());
					nodeLogs.setUpdatedByEmail(userInfo.getEmail());
					nodeLogs.setUpdatedDate(DateUtils.getCurrentDateTimeInUTC());
					nodeLogs.setUser(userInfo.getName());

					StringBuilder reference = new StringBuilder();
					StringBuilder changeStatement = new StringBuilder();

					if (nodeMapping.getChildIds() != null) {
						List<String> oldChildId = new ArrayList<>();
						if (ConfigurationPanel.getNodeMapping().containsKey(nodeMapId)) {
							oldChildId = ConfigurationPanel.getNodeMapping().get(nodeMapId).getChildIds();
							ConfigurationPanel.getNodeMapping().put(nodeMapId, nodeMapping);
							// To Check unmapped items
							for (String childId : oldChildId) {
								if (!nodeMapping.getChildIds().contains(childId)) {
									Map<String, DataNode> nodemap = configurationPanel
											.getAllNodesFor(nodeMapping.getChild());
									reference.append(getLabel(nodeMapping.getChild()));
									if (nodemap != null && nodemap.containsKey(childId)) {
										reference.append(" - ").append(nodemap.get(childId).getName());
									}
									reference.append(Constants.Parameters.SEPARATOR);
									changeStatement.append("Item ")
											.append(nodeMapping.getChild().equals(Entities.COMPETENCIESLEVEL.name())
													? "deleted"
													: "unmapped")
											.append(Constants.Parameters.SEPARATOR);
								}
							}
						}

						// To check mapped items
						for (String childId : nodeMapping.getChildIds()) {
							if (!oldChildId.contains(childId)) {
								Map<String, DataNode> nodemap = configurationPanel
										.getAllNodesFor(nodeMapping.getChild());
								reference.append(getLabel(nodeMapping.getChild()));
								if (nodemap != null && nodemap.containsKey(childId)) {
									reference.append(" - ").append(nodemap.get(childId).getName());
								}
								reference.append(Constants.Parameters.SEPARATOR);
								changeStatement.append("Item mapped").append(Constants.Parameters.SEPARATOR);
							}
						}
					}

					if (changeStatement.length() > 0) {
						nodeLogs.setReference(reference.toString());
						nodeLogs.setChangeStatement(changeStatement.toString());
						// Push activity logs to elasticsearch
						String id = nodeLogs.getId() + "_" + Calendar.getInstance().getTimeInMillis();
						elasticRepository.writeObjectToElastic(nodeLogs, id, appProperties.getCollectionLogs(),
								appProperties.getEsLogsType());
					}

					if (StringUtils.isNotBlank(nodeMapping.getChild())
							&& nodeMapping.getChild().equalsIgnoreCase(Entities.COMPETENCIESLEVEL.name())) {
						// Update parent node status
						updateParentNodeStatus(nodeMapping);
					}
				}
				ConfigurationPanel.getNodeMapping().put(nodeMapId, nodeMapping);

			} catch (IOException e) {
				Logger.error(String.format("Encountered an error in childNodeLogs  :  %s", e.getMessage()));
			}
		}).start();
	}

	/**
	 * Updates parent node status to unverified on mapping and unmapping competency
	 * levels
	 * 
	 * @param nodeMapping
	 *            NodeMapping
	 */
	private void updateParentNodeStatus(NodeMapping nodeMapping) {
		try {
			if (StringUtils.isNotBlank(nodeMapping.getParentId()) && StringUtils.isNotBlank(nodeMapping.getParent())) {
				DataNode dataNode = configurationPanel.getAllNodesFor(nodeMapping.getParent())
						.get(nodeMapping.getParentId());
				// Update Parent node status to unverified
				if (StringUtils.isBlank(dataNode.getStatus())
						|| !dataNode.getStatus().equals(NodeStatus.DRAFT.name())) {
					fracDao.updateNodeStatus(NodeStatus.UNVERIFIED.name(), nodeMapping.getParentId());
					dataNode.setStatus(NodeStatus.UNVERIFIED.name());
				}
			}
		} catch (Exception e) {
			Logger.error(
					String.format("Encountered an exception in updateParentNodeStatus method :  %s", e.getMessage()));
		}
	}

	@Override
	public void updateParentNodeStatus(String childId, UserInfo userInfo) {
		new Thread(() -> {
			try {
				List<NodeMapping> mappings = new ArrayList<>();
				// get all the mappings with the request childId
				Map<String, NodeMapping> nodeMaps = ConfigurationPanel.getNodeMapping();
				for (Map.Entry<String, NodeMapping> maps : nodeMaps.entrySet()) {
					if (CollectionUtils.isNotEmpty(maps.getValue().getChildIds())
							&& maps.getValue().getChildIds().contains(childId)) {
						mappings.add(maps.getValue());
					}
				}
				// update activity logs
				for (NodeMapping nodeMapping : mappings) {
					childNodeLogs(nodeMapping, userInfo);
				}
			} catch (Exception e) {
				Logger.error(String.format("Encountered an exception in updateParentNodeStatus method :  %s",
						e.getMessage()));
			}
		}).start();
	}

	@Override
	public void bulkUpdateNodeLogs(List<DataNode> dataNodes, Map<String, List<Map<String, Object>>> childMappings,
			Map<Integer, Map<String, Object>> parentObj, UserInfo userInfo) {
		new Thread(() -> {
			try {
				List<Map<String, Object>> toIndex = new ArrayList<>();
				for (int i = 0; i < dataNodes.size(); i++) {
					Map<String, Object> indexObj = new HashMap<>();
					DataNode parentNode = dataNodes.get(i);
					NodeLogs nodeLogs = getNodeLogs(parentNode, childMappings.get(parentNode.getId()), userInfo,
							parentObj.get(i).get(Constants.Parameters.ACTION).toString(), parentNode.getChildren());
					if (StringUtils.isNotBlank(nodeLogs.getChangeStatement())) {
						indexObj.put(Constants.Parameters.DATA_NODE,
								new ObjectMapper().convertValue(nodeLogs, Map.class));
						indexObj.put(Constants.Parameters.INDEX, appProperties.getCollectionLogs());
						indexObj.put(Constants.Parameters.TYPE, appProperties.getEsLogsType());
						indexObj.put(Constants.Parameters.ID,
								parentNode.getId() + "_" + Calendar.getInstance().getTimeInMillis());
						toIndex.add(indexObj);
					}
				}
				elasticRepository.writeBulkRequest(toIndex, new ArrayList<>(), new ArrayList<>());
			} catch (Exception e) {
				Logger.error(String.format("Exception in bulkUpdateNodeLogs: %s", e.getMessage()));
			}
		}).start();
	}

	@Override
	public Callable updateAuditData(List<DataNodeVerification> verification, UserInfo userInfo, Boolean batchProcess) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					if (!verification.isEmpty()) {
						for (DataNodeVerification v : verification)
							updateAuditDataNode(v, userInfo, batchProcess);
					}
					return Boolean.TRUE;
				} catch (Exception e) {
					Logger.error(String.format("Encountered an exception error while Updating Audit Data :  %s",
							e.getMessage()));
					e.printStackTrace();
					return Boolean.FALSE;
				}
			}
		};
	}

	private void updateAuditDataNode(DataNodeVerification verification, UserInfo userInfo, Boolean batchProcess)
			throws Exception {
		if (!(verification.getType().equalsIgnoreCase("KR")
				&& verification.getType().equalsIgnoreCase(Entities.KNOWLEDGERESOURCE.toString()))) {
			String uid = "";
			String userType = "";
			DataNode data = fracDao.getNodeById(verification.getId(), false, false, false, "");
			if (data != null && data.getCreatedBy() != null) {
				verification.setName(data.getName());
				verification.setSecondaryStatus(data.getSecondaryStatus());
				String[] subId = data.getCreatedBy().split(":");
				uid = subId.length == 3 ? subId[2] : subId[0];
				userType = "User";
			} else {
				userType = "Admin";
			}
			Actor actor = new Actor(uid, userType);
			Pdata pdata = new Pdata(appProperties.getPdataId(), appProperties.getPdataPid(),
					appProperties.getPdataVer());
			String sub = " ";
			String competencyType = " ";
			String channel = " ";
			String name = " ";
			if (data != null && verification.getType().equalsIgnoreCase(Entities.COMPETENCY.toString())) {
				name = data.getName();
				if (data.getAdditionalProperties() != null) {
					if (data.getAdditionalProperties().containsKey("cod"))
						sub = (String) data.getAdditionalProperties().get("cod");
					if (data.getAdditionalProperties().containsKey("competencyType"))
						competencyType = (String) data.getAdditionalProperties().get("competencyType");
				}
			}

			Map<String, Object> responseMap = authUtil
					.getUserDetails((batchProcess && verification.getReviewedBy() != null ? verification.getReviewedBy()
							: userInfo.getSub()), userInfo.getAuthToken());
			if (responseMap != null) {
				Map<String, Object> orgDetails = new ObjectMapper().convertValue(responseMap.get("rootOrg"),
						HashMap.class);
				if (orgDetails != null) {
					channel = (String) orgDetails.get("rootOrgId");
				}
				if (sub.equalsIgnoreCase(" ")) {
					sub = (String) orgDetails.get("orgName");
				}
			}

			if (verification.getName() == null)
				verification.setName(" ");
			CbObject cbObject = new CbObject(verification.getId(), convertToTitleCase(verification.getType()), "1.0",
					name, sub, convertToTitleCase(competencyType));
			Edata edata = new Edata(
					convertToTitleCase(verification.getSecondaryStatus() == null ? NodeStatus.UNVERIFIED.name()
							: verification.getSecondaryStatus()),
					new ArrayList<>(Arrays.asList(appProperties.getProps())), cbObject);
			Context c = new Context(channel, pdata, "FRAC");
			Objects obj = new Objects(verification.getId(), convertToTitleCase(verification.getType()));
			String mid = appProperties.getEid().concat(".").concat(generateUUID());
			Long ets = batchProcess && verification.getReviewedDate() != null
					? DateUtils.dateToTimestamp(verification.getReviewedDate())
					: DateUtils.getCurrentTimestamp();

			Audit audit = new Audit(actor, appProperties.getEid(), edata, appProperties.getAuditVer(), ets, c, mid,
					obj);
			kafkaProducer.sendMessage(appProperties.getAuditTopic(), audit);
			Logger.info(audit.toString());
		}
	}

	private NodeLogs getNodeLogs(DataNode parentNode, List<Map<String, Object>> childMaps, UserInfo userInfo,
			String action, List<DataNode> oldChildMappings) throws Exception {

		NodeLogs nodeLogs = new NodeLogs();
		nodeLogs.setId(parentNode.getId());
		nodeLogs.setType(parentNode.getType());
		nodeLogs.setUpdatedBy(userInfo.getSub());
		nodeLogs.setUpdatedByEmail(userInfo.getEmail());
		nodeLogs.setUpdatedDate(DateUtils.getCurrentDateTimeInUTC());
		nodeLogs.setUser(userInfo.getName());

		if (action.equals(Constants.Actions.UPDATE)) {
			Map<String, DataNode> parentNodeMap = configurationPanel.getAllNodesFor(parentNode.getType());
			if (parentNodeMap != null && parentNodeMap.containsKey(parentNode.getId())) {
				DataNode earlierStage = getAuditDataNode(parentNodeMap.get(parentNode.getId()));
				DataNode changedStage = getAuditDataNode(parentNode);
				nodeLogs.setEarlierStage(earlierStage);
				nodeLogs.setChangedStage(changedStage);
				findUpdatedField(earlierStage, changedStage, nodeLogs);
			}
		} else if (action.equals(Constants.Actions.CREATE)) {
			nodeLogs.setReference(Constants.Actions.CREATE);
			nodeLogs.setChangeStatement(Constants.Parameters.ITEM_CREATED);
		} else if (action.equals(Constants.Actions.PUBLISH)) {
			nodeLogs.setReference(Constants.Actions.PUBLISH);
			nodeLogs.setChangeStatement("Item published");
			if (StringUtils.isNotBlank(parentNode.getSource())) {
				nodeLogs.setChangeStatement(nodeLogs.getChangeStatement() + " from " + parentNode.getSource());
			}
		} else if (action.equals(Constants.Actions.UPLOAD)) {
			nodeLogs.setReference(Constants.Actions.UPLOAD);
			nodeLogs.setChangeStatement("Item uploaded");
			if (StringUtils.isNotBlank(parentNode.getSource())) {
				nodeLogs.setChangeStatement(nodeLogs.getChangeStatement() + " from " + parentNode.getSource());
			}
		}
		ConfigurationPanel.addDataNode(parentNode, parentNode.getType());
		// update child logs and cache
		List<String> newChildMappings = new ArrayList<>();
		StringBuilder levelStatement = new StringBuilder();
		StringBuilder childReference = new StringBuilder();
		if (childMaps != null) {
			for (Map<String, Object> childObj : childMaps) {
				DataNode childNode = new ObjectMapper().convertValue(childObj.get(Constants.Parameters.DATA_NODE),
						DataNode.class);
				newChildMappings.add(childNode.getId());
				if (action.equals(Constants.Actions.UPDATE)) {
					if (Constants.Actions.PUBLISH.equals(childObj.get(Constants.Parameters.ACTION).toString())) {
						childReference.append(getLabel(childNode.getType())).append(" - ").append(childNode.getName())
								.append(Constants.Parameters.SEPARATOR);
						levelStatement.append(Constants.Parameters.ITEM_CREATED).append(Constants.Parameters.SEPARATOR);
					} else if (Constants.Actions.UPDATE.equals(childObj.get(Constants.Parameters.ACTION).toString())) {
						Map<String, DataNode> allNodes = configurationPanel.getAllNodesFor(childNode.getType());
						NodeLogs childLogs = new NodeLogs();
						if (allNodes != null && allNodes.containsKey(childNode.getId())) {
							DataNode childNodeEarlier = getAuditDataNode(allNodes.get(childNode.getId()));
							DataNode childNodeChanged = getAuditDataNode(childNode);
							childLogs.setEarlierStage(childNodeEarlier);
							childLogs.setChangedStage(childNodeChanged);
							findUpdatedField(childNodeEarlier, childNodeChanged, childLogs);
						}
						if (!childLogs.getChangeStatement().equals("")) {
							childReference.append(getLabel(childNode.getType())).append(" - ")
									.append(childNode.getName()).append(Constants.Parameters.SEPARATOR);
							levelStatement.append(
									childLogs.getChangeStatement().replace(Constants.Parameters.SEPARATOR, "\r\n"))
									.append(Constants.Parameters.SEPARATOR);
						}
					}
				}
				ConfigurationPanel.addDataNode(childNode, childNode.getType());
			}
		}
		if (!oldChildMappings.isEmpty()) {
			for (DataNode child : oldChildMappings) {
				if (!newChildMappings.contains(child.getId())) {
					childReference.append(getLabel(child.getType())).append(" - ").append(child.getName())
							.append(Constants.Parameters.SEPARATOR);
					levelStatement.append("Item deleted").append(Constants.Parameters.SEPARATOR);
				}
			}
		}

		if (levelStatement.length() != 0 && childReference.length() != 0) {
			nodeLogs.setChangeStatement((StringUtils.isNotBlank(nodeLogs.getChangeStatement()))
					? nodeLogs.getChangeStatement() + levelStatement + Constants.Parameters.SEPARATOR
					: levelStatement + Constants.Parameters.SEPARATOR);
			nodeLogs.setReference(StringUtils.isNotBlank(nodeLogs.getReference())
					? nodeLogs.getReference() + childReference + Constants.Parameters.SEPARATOR
					: childReference + Constants.Parameters.SEPARATOR);
		}

		return nodeLogs;
	}

	/**
	 * Method to compare the earlier and new node values and sets the changes in the
	 * log document as a statement separated by a semicolon(::)
	 * 
	 * @param earlierStage
	 *            DataNode
	 * @param changedStage
	 *            DataNode
	 * @return String
	 */
	private void findUpdatedField(DataNode earlierStage, DataNode changedStage, NodeLogs nodeLogs) {
		StringBuilder changeStatement = new StringBuilder();
		StringBuilder reference = new StringBuilder();

		checkChanges(Constants.Parameters.NAME, earlierStage.getName(), changedStage.getName(), changeStatement,
				reference, Boolean.TRUE);
		checkChanges(Constants.Parameters.DESCRIPTION, earlierStage.getDescription(), changedStage.getDescription(),
				changeStatement, reference, Boolean.TRUE);
		checkChanges(Constants.Parameters.SOURCE, earlierStage.getSource(), changedStage.getSource(), changeStatement,
				reference, Boolean.TRUE);

		// Fields corresponding to types
		if (changedStage.getType().equals(Entities.POSITION.name())) {
			String department = configurationPanel.titleCaseConversion(Constants.Parameters.DEPARTMENT);
			Object earlierDeptValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(department)
					: null;
			Object newDeptValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(department)
					: null;
			checkChanges(department, earlierDeptValue, newDeptValue, changeStatement, reference, Boolean.TRUE);
		}
		if (changedStage.getType().equals(Entities.COMPETENCY.name())) {
			Object earlierTypeValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_TYPE)
					: null;
			Object newTypeValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_TYPE)
					: null;
			checkChanges(Constants.Parameters.COMPETENCY_TYPE, earlierTypeValue, newTypeValue, changeStatement,
					reference, Boolean.TRUE);
			Object earlierAreaValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA)
					: null;
			Object newAreaValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCIES_AREA)
					: null;
			checkChanges(Constants.Parameters.COMPETENCIES_AREA, earlierAreaValue, newAreaValue, changeStatement,
					reference, Boolean.TRUE);
			Object earlierCodValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.COD)
					: null;
			Object newCodValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.COD)
					: null;
			checkChanges(Constants.Parameters.COD, earlierCodValue, newCodValue, changeStatement, reference,
					Boolean.TRUE);
			Object earlierCompetencySector = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCYSECTOR)
					: null;
			Object newCompetencySector = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCYSECTOR)
					: null;
			checkChanges(Constants.Parameters.COMPETENCYSECTOR, earlierCompetencySector, newCompetencySector,
					changeStatement, reference, Boolean.TRUE);

			Object earlierSourceValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_SOURCE)
					: null;
			Object newSourceValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.COMPETENCY_SOURCE)
					: null;
			checkChanges(Constants.Parameters.COMPETENCY_SOURCE, earlierSourceValue, newSourceValue, changeStatement,
					reference, Boolean.FALSE);
		}
		if (changedStage.getType().equals(Entities.KNOWLEDGERESOURCE.name())) {
			Object earlierUrlValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.URL)
					: null;
			Object newUrlValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.URL)
					: null;
			checkChanges(Constants.Parameters.URL, earlierUrlValue, newUrlValue, changeStatement, reference,
					Boolean.FALSE);
			Object earlierFileValue = (earlierStage.getAdditionalProperties() != null)
					? earlierStage.getAdditionalProperties().get(Constants.Parameters.FILES)
					: null;
			Object newFileValue = (changedStage.getAdditionalProperties() != null)
					? changedStage.getAdditionalProperties().get(Constants.Parameters.FILES)
					: null;
			checkChanges(Constants.Parameters.FILES, earlierFileValue, newFileValue, changeStatement, reference,
					Boolean.FALSE);
		}
		if (changedStage.getType().equals(Entities.COMPETENCIESLEVEL.name())) {
			checkChanges(Constants.Parameters.LEVEL, earlierStage.getLevel(), changedStage.getLevel(), changeStatement,
					reference, Boolean.TRUE);
		}
		nodeLogs.setChangeStatement(changeStatement.toString());
		nodeLogs.setReference(reference.toString());
	}

	// Checks if earlier and the new value of a field are equal. If equals creates
	// the statement with changed value
	private boolean checkChanges(String field, Object oldValue, Object newValue, StringBuilder statement,
			StringBuilder reference, Boolean trackValue) {
		String space = " ";
		if ((oldValue == null || oldValue == "") && newValue != null) {
			if (trackValue) {
				statement.append(Constants.Parameters.CHANGED).append(space).append(Constants.Parameters.TO)
						.append(space).append("\'").append(newValue).append("\'")
						.append(Constants.Parameters.SEPARATOR);
			} else {
				statement.append(Constants.Parameters.UPDATED).append(Constants.Parameters.SEPARATOR);
			}
			reference.append(getLabel(field)).append(Constants.Parameters.SEPARATOR);
		} else if (oldValue != null && newValue == null) {
			if (trackValue) {
				statement.append("Removed").append(Constants.Parameters.SEPARATOR);
			} else {
				statement.append(Constants.Parameters.UPDATED).append(Constants.Parameters.SEPARATOR);
			}
			reference.append(getLabel(field)).append(Constants.Parameters.SEPARATOR);
		} else if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
			if (trackValue) {
				statement.append(Constants.Parameters.CHANGED).append(space).append(Constants.Parameters.FROM)
						.append(space).append("\'").append(oldValue).append("\'").append(space)
						.append(Constants.Parameters.TO).append(space).append("\'").append(newValue).append("\'")
						.append(Constants.Parameters.SEPARATOR);
			} else {
				statement.append(Constants.Parameters.UPDATED).append(Constants.Parameters.SEPARATOR);
			}
			reference.append(getLabel(field)).append(Constants.Parameters.SEPARATOR);
		}
		return true;
	}

	/**
	 * Returns the label value in title case format
	 * 
	 * @param label
	 *            String
	 * @return String
	 */
	public String getLabel(String label) {
		if (label.equalsIgnoreCase(Entities.KNOWLEDGERESOURCE.name())) {
			return Constants.Parameters.KR;
		} else if (label.equalsIgnoreCase(Entities.COMPETENCIESLEVEL.name())) {
			return Constants.Parameters.COMPETENCY_LEVEL;
		} else if (label.equalsIgnoreCase(Entities.COMPETENCYAREA.name())) {
			return Constants.Parameters.COMPETENCY_AREA;
		} else if (label.equalsIgnoreCase(Constants.Parameters.COMPETENCY_TYPE)) {
			return Constants.Parameters.COMP_TYPE;
		} else if (label.equalsIgnoreCase(Constants.Parameters.COD)) {
			return Constants.Parameters.COD.toUpperCase();
		} else if (label.equalsIgnoreCase(Constants.Parameters.COMPETENCYSECTOR)) {
			return "Competency Sector";
		} else if (label.equalsIgnoreCase(Constants.Parameters.URL)) {
			return Constants.Parameters.URL.toUpperCase();
		} else if (label.equalsIgnoreCase(Constants.Parameters.COMPETENCY_SOURCE)) {
			return "Competency Source";
		}
		return configurationPanel.titleCaseConversion(label);
	}

	public String convertToTitleCase(String s) {
		if (StringUtils.isNotEmpty(s)) {
			char[] delimiters = { ' ', '_' };
			String d = WordUtils.capitalizeFully(s, delimiters);
			return d;
		}
		return " ";
	}

	public String generateUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * Converting the KR's url & files list in additional property from List of
	 * object to list of string to store in es activity index
	 * 
	 * @param dataNode
	 *            DataNode
	 * @return DataNode
	 * @throws Exception
	 */
	private DataNode getAuditDataNode(DataNode dataNode) throws Exception {
		DataNode auditNode = dataNode.clone();
		if (dataNode.getType().equals(Entities.KNOWLEDGERESOURCE.name())
				&& dataNode.getAdditionalProperties() != null) {
			if (dataNode.getAdditionalProperties().get(Constants.Parameters.URL) != null) {
				List<String> url = new ArrayList<>();
				List<Map<String, Object>> urlList = new ObjectMapper().convertValue(
						dataNode.getAdditionalProperties().get(Constants.Parameters.URL),
						new TypeReference<List<Map<String, String>>>() {
						});
				for (Map<String, Object> urlObj : urlList) {
					url.add((String) urlObj.get(Constants.Parameters.VALUE));
				}
				auditNode.getAdditionalProperties().put(Constants.Parameters.URL, url);
			}
			if (dataNode.getAdditionalProperties().get(Constants.Parameters.FILES) != null) {
				List<String> files = new ArrayList<>();
				List<Object> filesList = new ObjectMapper().convertValue(
						dataNode.getAdditionalProperties().get(Constants.Parameters.FILES),
						new TypeReference<List<Object>>() {
						});
				for (Object filesObj : filesList) {
					if (filesObj instanceof Map) {
						Map<String, Object> fileMap = new ObjectMapper().convertValue(filesObj, Map.class);
						files.add((String) fileMap.get(Constants.Parameters.VALUE));
					} else if (filesObj instanceof String) {
						files.add((String) filesObj);
					}
				}
				auditNode.getAdditionalProperties().put(Constants.Parameters.FILES, files);
			}
		}

		return auditNode;
	}

}
