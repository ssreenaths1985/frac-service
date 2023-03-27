package com.tarento.frac.service.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.FRACDao;
import com.tarento.frac.models.CompetencyType;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeFeedback;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.models.analytics.AggregateDto;
import com.tarento.frac.service.FRACService;
import com.tarento.frac.utils.Constants;
import org.springframework.util.ObjectUtils;

@Service(Constants.ServiceRepositories.FRAC_SERVICE)
public class FRACServiceImpl implements FRACService {

	public static final Logger LOGGER = LoggerFactory.getLogger(FRACServiceImpl.class);

	@Autowired
	private FRACDao fracDao;

	@Autowired
	private ConfigurationPanel configurationPanel;

	@Override
	public List<DataNode> getChildNodes(String parentId, String type) {
		return fracDao.getChildNodes(parentId, type);
	}

	@Override
	public List<DataNode> getParentNodes(String childId, String type) {
		return fracDao.getParentNodes(childId, type);
	}

	@Override
	public List<DataNode> filterDataNodes(FilterList filterList, String userId) {
		return fracDao.filterDataNodes(filterList, userId);
	}

	@Override
	public Long getCountOfNodes(String type, String department, String status, String userType) {
		return fracDao.getCountOfNodes(type, department, status, userType);
	}

	@Override
	public Overview exploreAllNodes(String keyword, String department) {
		return fracDao.exploreAllNodes(keyword, department);
	}

	@Override
	public DataNodesVerificationView getVerificationList(String type, String department, Boolean isDetail,
			String userType) {
		return fracDao.getVerificationList(type, department, isDetail, userType);
	}

	@Override
	public boolean bookmarkDataNode(DataNode bookmarkNode, UserInfo userInfo) {
		return fracDao.bookmarkDataNode(bookmarkNode, userInfo.getSub());
	}

	@Override
	public DataNode getNodeById(String id, Boolean showSimilar, Boolean isDetail, Boolean bookmarks, String userId) {
		return fracDao.getNodeById(id, showSimilar, isDetail, bookmarks, userId);
	}

	@Override
	public List<DataNode> searchNodes(MultiSearch multiSearch) {
		LOGGER.info("Inside searchNodes Service layer");
		return fracDao.searchNodes(multiSearch);
	}

	@Override
	public List<DataNode> getBookmark(String userId, String type) {
		return fracDao.getBookmark(userId, type);
	}

	@Override
	public DataNodesVerificationView filterReviewNodes(FilterList filterList) {
		return fracDao.filterReviewNodes(filterList);
	}

	@Override
	public List<String> getSourceList(String type) {
		return fracDao.getSourceList(type);
	}

	@Override
	public Boolean flushReloadCache(String type, Boolean flush, Boolean reload) {
		if (flush != null && flush) {
			configurationPanel.flushCache(type);
		}
		if (reload != null && reload) {
			configurationPanel.reloadCache(type);
		}
		return Boolean.TRUE;
	}

	@Override
	public List<DataNode> getAllDataNodes(DataNode node, Boolean isDetail, Boolean myRequest, String userId,
			String userType) {
		if (StringUtils.isNotBlank(node.getType())) {
			if (node.getType().equals(Entities.COMPETENCYAREA.name())) {
				return fracDao.fetchCompetencyAreas();
			} else if (node.getType().equals(Entities.SECTOR.name())) {
				return fracDao.getPropertyNode(node.getType());
			} else if (node.getType().equals(Entities.COMPETENCYTYPE.name())) {
				List<DataNode> dataNodes = new ArrayList<>();
				int i = 0;
				for (CompetencyType compType : CompetencyType.values()) {
					i = i + 1;
					dataNodes.add(new DataNode(Integer.toString(i), compType.value));
				}
				return dataNodes;
			}
		}

		RequestObject requestObject = new RequestObject(node.getType(), isDetail, node.getStatus(),
				node.getDepartment(), node.getBookmark(), myRequest, userType, userId, null, null);

		Map<String, Object> response = fracDao.getAllDataNodes(requestObject, null);
		return (List<DataNode>) response.get(Constants.Parameters.DATA_NODE);
	}

	@Override
	public Overview exploreSearch(String keyword, String department) {
		return fracDao.exploreSearch(keyword, department);
	}

	@Override
	public KeyValueList getCompetencyAreaListing(String department, String status) {
		return fracDao.getCompetencyAreaListing(department, status);
	}

	@Override
	public List<DataNode> filterByMappings(FilterList filterList, String userId) {
		return fracDao.filterByMappings(filterList, userId);
	}

	@Override
	public List<DataNode> getMapping(String id, String type, String returnType, Boolean isDetail) {
		return fracDao.getMapping(id, type, returnType, isDetail);
	}

	@Override
	public Boolean reviewMappings(MappingVerification mappingVerification) {
		return fracDao.reviewMappings(mappingVerification);
	}

	@Override
	public AggregateDto getMyGraphs(String visualizationCode, String sub) {
		return fracDao.getMyGraphs(visualizationCode, sub);
	}

	@Override
	public Boolean nodeFeedback(Extensions extension, String name) {
		return fracDao.nodeFeedback(extension, name);
	}

	@Override
	public List<NodeFeedback> getNodeFeedback(String type, String id, String userId) {
		return fracDao.getNodeFeedback(type, id, userId);
	}

	@Override
	public Extensions getNodeRatingAverage(String type, String id) {
		return fracDao.getNodeRatingAverage(type, id);
	}

	@Override
	public Object getCollectionLogs(String id, String type) {
		return fracDao.getCollectionLogs(id, type);
	}

	@Override
	public KeyValueList getPropertyCountList(String type) {
		return fracDao.getPropertyCountList(type);
	}

	@Override
	public Map<String, Object> getAllDataNodes(RequestObject reqObject, String version) {
		if (StringUtils.isNotBlank(reqObject.getType())) {
			Map<String, Object> response = new HashMap<String, Object>();
			if (reqObject.getType().equals(Entities.COMPETENCYAREA.name())
					|| reqObject.getType().equals(Entities.SECTOR.name())) {
				response.put(Constants.Parameters.DATA_NODE, fracDao.getPropertyNode(reqObject.getType()));
				return response;
			} else if (reqObject.getType().equals(Entities.COMPETENCYTYPE.name())) {
				List<DataNode> dataNodes = new ArrayList<>();
				int i = 0;
				for (CompetencyType compType : CompetencyType.values()) {
					i = i + 1;
					dataNodes.add(new DataNode(Integer.toString(i), compType.value));
				}
				response.put(Constants.Parameters.DATA_NODE, dataNodes);
				return response;
			}
		}

		return fracDao.getAllDataNodes(reqObject, version);
	}

	@Override
	public List<DataNode> privateMethodToUpdateDB(Map<String, Object> request, String userId) {
		try {
			if (!request.isEmpty()) {
				String errMsg = "";
				errMsg = validateUpdateRequest(request, errMsg);
				if (errMsg.isEmpty()) {
					List<Map<String, Object>> updatedObjectList = (List<Map<String, Object>>) request.get(Constants.Parameters.UPDATES);
					List<DataNode> finalResponse = new ArrayList<>();
					DataNode dataNodeObj = new DataNode();
					for (Map<String, Object> updatedObjectElement : updatedObjectList) {
						if (null != updatedObjectElement.get(Constants.Parameters.NODE_ID)) {
							String nodeId = (String) updatedObjectElement.get(Constants.Parameters.NODE_ID);
							dataNodeObj = getNodeById(nodeId, true, true, true, userId);
							final ObjectMapper mapper = new ObjectMapper();
							if (!ObjectUtils.isEmpty(dataNodeObj)) {
								Map<String, Object> dataNodeMap = mapper.convertValue(dataNodeObj, Map.class);
								if (null != updatedObjectElement.get(Constants.Parameters.FIELDS_TO_BE_UPDATED)) {
									List<Map<String, Object>> updatedFields = (List<Map<String, Object>>) updatedObjectElement.get(Constants.Parameters.FIELDS_TO_BE_UPDATED);
									for (Map<String, Object> updatedFieldElement : updatedFields) {
										if (null != updatedFieldElement.get(Constants.Parameters.VALUE) && null != updatedFieldElement.get(Constants.Parameters.KEY)) {
											if (dataNodeMap.containsKey(updatedFieldElement.get(Constants.Parameters.KEY))) {
												dataNodeMap.put((String) updatedFieldElement.get(Constants.Parameters.KEY), updatedFieldElement.get(Constants.Parameters.VALUE));
												Gson gson = new Gson();
												JsonElement jsonElement = gson.toJsonTree(dataNodeMap);
												dataNodeObj = gson.fromJson(jsonElement, DataNode.class);
											} else {
												Map<String, Object> additionalProperties = (Map<String, Object>) dataNodeMap.get(Constants.Parameters.ADDITIONAL_PROPERTIES);
												additionalProperties.put((String) updatedFieldElement.get(Constants.Parameters.KEY), updatedFieldElement.get(Constants.Parameters.VALUE));
												dataNodeObj.setAdditionalProperties(additionalProperties);
											}
										} else {
											throw new Exception("Request field values is/are null.");
										}
									}
									fracDao.updateDataNode(dataNodeObj);
									finalResponse.add(dataNodeObj);
								} else {
									throw new Exception("Request doesn't contains the fields to be updated.");
								}
							} else {
								throw new Exception("No entity exists against ID : " + nodeId);
							}
						} else {
							throw new Exception("Request doesn't contains ID of the entity.");
						}
					}
					return finalResponse;
				} else {
					LOGGER.error(errMsg);
					return null;
				}
			} else {
				LOGGER.error("Request is empty!");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(String.format("Exception occurred while trying to update : %s ", e.getMessage()));
			return null;
		}
	}

	public String validateUpdateRequest(Map<String, Object> request, String errMsg) {
		if (request.containsKey(Constants.Parameters.UPDATES)) {
			List<Map<String, Object>> updatedObjectList = (List<Map<String, Object>>) request.get(Constants.Parameters.UPDATES);
			if (updatedObjectList instanceof List) {
				if (!ObjectUtils.isEmpty(updatedObjectList)) {
					for (Map<String, Object> updatedObjectElement : updatedObjectList) {
						if (updatedObjectElement.containsKey(Constants.Parameters.NODE_ID) && !(StringUtils.isEmpty((CharSequence) updatedObjectElement.get(Constants.Parameters.NODE_ID)))) {
							if (updatedObjectElement.containsKey(Constants.Parameters.FIELDS_TO_BE_UPDATED) && !ObjectUtils.isEmpty(updatedObjectElement.get(Constants.Parameters.FIELDS_TO_BE_UPDATED)) && updatedObjectElement.get(Constants.Parameters.FIELDS_TO_BE_UPDATED) instanceof List) {
								List<Map<String, Object>> updatedFields = (List<Map<String, Object>>) updatedObjectElement.get(Constants.Parameters.FIELDS_TO_BE_UPDATED);
								for (Map<String, Object> updatedFieldElement : updatedFields) {
									if (updatedFieldElement.containsKey(Constants.Parameters.KEY) && updatedFieldElement.containsKey(Constants.Parameters.VALUE)) {

									} else {
										errMsg = " fields to be updated list does not contain KEY or VALUE or both! ";
									}
								}
							} else {
								errMsg = "fields to be updated is empty/unexpected type!";
							}
						} else {
							errMsg = "NodeId is empty!";
						}
					}
					if (updatedObjectList.get(0) instanceof Map) {

					} else {
						errMsg = "Unexpected Type, updates List is not a map of updated fields!";
					}
				} else {
					errMsg = "Updates List is empty!";
				}
			} else {
				errMsg = "Unexpected Type, updates is not a list!";
			}
		} else {
			errMsg = "Failed to get the updates Map!";
		}

		return errMsg;
	}

}