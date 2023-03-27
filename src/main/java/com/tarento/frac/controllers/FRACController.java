package com.tarento.frac.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeFeedback;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.service.DataNodeService;
import com.tarento.frac.service.FRACDictionaryService;
import com.tarento.frac.service.FRACService;
import com.tarento.frac.service.VerificationService;
import com.tarento.frac.utils.AuthUtil;
import com.tarento.frac.utils.CloudStore;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.PathRoutes;
import com.tarento.frac.utils.ResponseGenerator;

/**
 * 
 * @author Darshan Nagesh
 *
 */
@RestController
@RequestMapping(PathRoutes.Endpoints.FRAC_ROOT)
public class FRACController {

	public static final Logger logger = LoggerFactory.getLogger(FRACController.class);

	@Autowired
	private FRACService fracService;

	@Autowired
	private DataNodeService dataNodeService;

	@Autowired
	private FRACDictionaryService dictionaryService;

	@Autowired
	private AuthUtil authUtil;

	@Autowired
	private VerificationService verificationService;

	@PostMapping(value = PathRoutes.Endpoints.ADD_DATANODE)
	public String addDataNode(@RequestBody DataNode dataNode,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		List<String> roles = authUtil.getUserRole(userInfo.getSub(), userInfo.getAuthToken());
		if (!dataNodeService.checkUserAccesstoEdit(dataNode, roles)) {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.UNAUTHORIZED_ACCESS);
		}
		DataNode node = dataNodeService.addDataNode(dataNode, userInfo);
		if (node != null && node.getId() != null) {
			return ResponseGenerator.successResponse(node);
		} else {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.NOT_CREATED_MESSAGE);
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.ADD_DATANODES)
	public String addDataNodes(@RequestBody List<DataNode> dataNodeList,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		List<String> roles = authUtil.getUserRole(userInfo.getSub(), userInfo.getAuthToken());
		userInfo.setRoles(roles);
		List<DataNode> resultNodes = dataNodeService.addDataNodes(dataNodeList, userInfo);
		if (resultNodes != null && !resultNodes.isEmpty()) {
			return ResponseGenerator.successResponse(resultNodes);
		} else {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.NOT_CREATED_MESSAGE);
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.ADD_DATA_NODE_BULK, produces = MediaType.APPLICATION_JSON_VALUE)
	public String addDataNodeBulk(@RequestBody DataNode dataNode,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		List<String> roles = authUtil.getUserRole(userInfo.getSub(), userInfo.getAuthToken());
		if (!dataNodeService.checkUserAccesstoEdit(dataNode, roles)) {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.UNAUTHORIZED_ACCESS);
		}
		dataNode = dataNodeService.addDataNodeBulk(dataNode, userInfo);
		if (dataNode != null) {
			return ResponseGenerator.successResponse(dataNode);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.UPLOAD_DATA_NODE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String uploadDataNode(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		try {
			if (multipartFile != null && multipartFile.getOriginalFilename().endsWith(".xlsx")) {
				File file = new File(multipartFile.getOriginalFilename());
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(multipartFile.getBytes());
				fos.close();
				List<DataNode> uploaded = dataNodeService.uploadDataNode(file, userInfo);
				file.delete();
				if (uploaded != null) {
					return ResponseGenerator.successResponse(uploaded);
				}
			}

		} catch (Exception e) {
			logger.error("Error in uploadDataNode : " + e.getMessage());
		}
		return ResponseGenerator.failureResponse();
	}

	@PostMapping(value = PathRoutes.Endpoints.VERIFY_DATANODE)
	public String verifyDataNode(@RequestBody DataNodeVerification verification,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo,
			@RequestHeader(value = Constants.Parameters.ORIGIN, required = true) String origin)
			throws JsonProcessingException {
		List<String> roles = authUtil.getUserRole(userInfo.getSub(), userInfo.getAuthToken());
		if ((roles != null && (roles.contains(Constants.UserType.REVIEWER_ONE)
				|| roles.contains(Constants.UserType.REVIEWER_TWO) || roles.contains(Constants.UserType.ADMIN)))) {
			Boolean status = verificationService.verifyDataNode(verification, userInfo);
			if (status != null && status) {
				return ResponseGenerator.successResponse(status);
			}
		}
		return ResponseGenerator.failureResponse(Constants.ResponseMessages.UNABLE_TO_VERIFY);
	}

	@PostMapping(value = PathRoutes.Endpoints.VERIFY_ALL_DATANODE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String verifyAllDataNode(@RequestBody DataNodeVerification verification,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		Boolean status = verificationService.verifyAllDataNode(verification, userInfo);
		if (status != null && status) {
			return ResponseGenerator.successResponse(status);
		}
		return ResponseGenerator.failureResponse(Constants.ResponseMessages.UNABLE_TO_VERIFY);
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_ALL_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllDataNodes(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type,
			@RequestParam(value = "isDetail", required = false) Boolean isDetail,
			@RequestParam(value = Constants.Parameters.STATUS, required = false) String status,
			@RequestParam(value = Constants.Parameters.DEPARTMENT, required = false) String department,
			@RequestParam(value = "bookmarks", required = false) Boolean bookmarks,
			@RequestParam(value = "myRequest", required = false) Boolean myRequest,
			@RequestParam(value = Constants.Parameters.USER_TYPE, required = false) String userType,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		DataNode d = new DataNode();
		d.setType(type);
		d.setStatus(status);
		d.setDepartment(department);
		d.setBookmark(bookmarks);
		List<DataNode> dataNodes = fracService.getAllDataNodes(d, isDetail, myRequest, userInfo.getSub(), userType);
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.FILTER_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String filterDataNodes(@RequestBody FilterList filterList,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		List<DataNode> dataNodes = fracService.filterDataNodes(filterList, userInfo.getSub());
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_VERIFICATION_LIST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getVerificationList(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type,
			@RequestParam(value = Constants.Parameters.USER_TYPE, required = false) String userType,
			@RequestParam(value = "isDetail", required = false) Boolean isDetail,
			@RequestParam(value = Constants.Parameters.DEPARTMENT, required = false) String department)
			throws IOException {
		DataNodesVerificationView verification = fracService.getVerificationList(type, department, isDetail, userType);
		if (verification != null) {
			return ResponseGenerator.successResponse(verification);
		}
		return ResponseGenerator.failureResponse();
	}

	@PostMapping(value = PathRoutes.Endpoints.MAP_NODES)
	public String mapNodes(@RequestBody NodeMapping nodeMapping,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		Boolean status = dataNodeService.mapNodes(nodeMapping, userInfo);
		if (status != null && status) {
			return ResponseGenerator.successResponse(status);
		} else {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.NOT_CREATED_MESSAGE);
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.NODE_FEEDBACK)
	public String nodeFeedback(@RequestBody Extensions extension,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		extension.setUserId(userInfo.getSub());
		Boolean status = fracService.nodeFeedback(extension, userInfo.getName());
		if (status) {
			return ResponseGenerator.successResponse(status);
		} else {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.NOT_CREATED_MESSAGE);
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_NODE_FEEDBACK, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getNodeFeedback(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type,
			@RequestParam(value = Constants.Parameters.ID, required = true) String id,
			@RequestParam(value = Constants.Parameters.USER, required = false) Boolean myFeedback,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		String userId = null;
		if (myFeedback != null && myFeedback) {
			userId = userInfo.getSub();
		}
		List<NodeFeedback> nodeFeedback = fracService.getNodeFeedback(type, id, userId);
		if (nodeFeedback != null) {
			return ResponseGenerator.successResponse(nodeFeedback);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_NODE_RATING_AVERAGE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getNodeRatingAverage(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type,
			@RequestParam(value = Constants.Parameters.ID, required = true) String id) throws IOException {
		Extensions extension = fracService.getNodeRatingAverage(type, id);
		if (extension != null) {
			return ResponseGenerator.successResponse(extension);
		} else {
			return ResponseGenerator.successResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_CHILD_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getChildNodes(@RequestParam(value = Constants.Parameters.PARENT_ID, required = true) String parentId,
			@RequestParam(value = Constants.Parameters.TYPE, required = true) String type) throws IOException {
		List<DataNode> dataNodes = fracService.getChildNodes(parentId, type);
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_PARENT_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getParentNodes(@RequestParam(value = "childId", required = true) String childId,
			@RequestParam(value = Constants.Parameters.TYPE, required = true) String type) throws IOException {
		List<DataNode> dataNodes = fracService.getParentNodes(childId, type);
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_NODE_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getNodeById(@RequestParam(value = Constants.Parameters.ID, required = true) String id,
			@RequestParam(value = "isDetail", required = false) Boolean isDetail,
			@RequestParam(value = "bookmarks", required = false) Boolean bookmarks,
			@RequestParam(value = "showSimilar", required = false) Boolean showSimilar,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		DataNode dataNode = fracService.getNodeById(id, showSimilar, isDetail, bookmarks, userInfo.getSub());
		if (dataNode != null) {
			return ResponseGenerator.successResponse(dataNode);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@DeleteMapping(value = PathRoutes.Endpoints.DELETE_NODE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteNode(@RequestParam(value = Constants.Parameters.ID, required = true) String id,
			@RequestParam(value = Constants.Parameters.TYPE, required = true) String type,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		List<String> roles = authUtil.getUserRole(userInfo.getSub(), userInfo.getAuthToken());
		if (roles != null && !roles.contains(Constants.UserType.REVIEWER_TWO)
				&& !roles.contains(Constants.UserType.ADMIN)) {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.UNAUTHORIZED_ACCESS);
		}
		Boolean deleted = dataNodeService.deleteNode(id, type, userInfo);
		if (deleted) {
			return ResponseGenerator.successResponse(deleted);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.SEARCH_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String searchNodes(@RequestBody MultiSearch multiSearch) throws IOException {
		List<DataNode> dataNodes = fracService.searchNodes(multiSearch);
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}

	}

	@PostMapping(value = PathRoutes.Endpoints.APPEND_MAP_NODES)
	public String appendMapNodes(@RequestBody NodeMapping nodeMapping,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		Boolean status = dataNodeService.appendMapNodes(nodeMapping, userInfo);
		if (status != null && status) {
			return ResponseGenerator.successResponse(status);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_COUNT_OF_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCountOfNodes(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type,
			@RequestParam(value = Constants.Parameters.DEPARTMENT, required = false) String department,
			@RequestParam(value = Constants.Parameters.STATUS, required = false) String status,
			@RequestParam(value = Constants.Parameters.USER_TYPE, required = false) String userType)
			throws IOException {
		Long count = fracService.getCountOfNodes(type, department, status, userType);
		if (count != null) {
			return ResponseGenerator.successResponse(count);
		} else {
			return ResponseGenerator.failureResponse();
		}

	}

	@GetMapping(value = PathRoutes.Endpoints.EXPLORE_ALL_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String exploreAllNodes(@RequestParam(value = Constants.Parameters.KEYWORD, required = false) String keyword,
			@RequestParam(value = Constants.Parameters.DEPARTMENT, required = false) String department)
			throws IOException {
		Overview overview = fracService.exploreAllNodes(keyword, department);
		if (overview != null) {
			return ResponseGenerator.successResponse(overview);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getMapping(@RequestParam(value = Constants.Parameters.ID, required = false) String id,
			@RequestParam(value = Constants.Parameters.TYPE, required = false) String type,
			@RequestParam(value = "returnType", required = false) String returnType,
			@RequestParam(value = "isDetail", required = false) Boolean isDetail) throws IOException {
		return ResponseGenerator.successResponse(fracService.getMapping(id, type, returnType, isDetail));
	}

	@GetMapping(value = PathRoutes.Endpoints.EXPLORE_SEARCH, produces = MediaType.APPLICATION_JSON_VALUE)
	public String exploreSearch(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = Constants.Parameters.DEPARTMENT, required = false) String department)
			throws IOException {
		return ResponseGenerator.successResponse(fracService.exploreSearch(keyword, department));
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_COMPETENCY_AREA_LISTING, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCompetencyAreaListing(
			@RequestParam(value = Constants.Parameters.STATUS, required = false) String status,
			@RequestParam(value = Constants.Parameters.DEPARTMENT, required = false) String department)
			throws JsonProcessingException {
		KeyValueList compArea = fracService.getCompetencyAreaListing(department, status);
		return ResponseGenerator.successResponse(compArea);
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_COLLECTION_LOGS, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCollectionLogs(@RequestParam(value = Constants.Parameters.ID, required = true) String id,
			@RequestHeader(value = Constants.Parameters.TYPE, required = false) String type) throws IOException {
		return ResponseGenerator.successResponse(fracService.getCollectionLogs(id, type));
	}

	@PostMapping(value = PathRoutes.Endpoints.BOOKMARK_DATA_NODE)
	public String bookmarkDataNode(@RequestBody DataNode bookmarkNode,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		if (StringUtils.isNotBlank(bookmarkNode.getId()) && StringUtils.isNotBlank(bookmarkNode.getType())) {
			Boolean bookmarked = fracService.bookmarkDataNode(bookmarkNode, userInfo);
			if (bookmarked) {
				return ResponseGenerator.successResponse(bookmarked);
			}
		}
		return ResponseGenerator.failureResponse();
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_BOOKMARK)
	public String getBookmark(@RequestParam(value = Constants.Parameters.TYPE, required = false) String type,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		List<DataNode> dataNode = fracService.getBookmark(userInfo.getSub(), type);
		if (dataNode != null) {
			return ResponseGenerator.successResponse(dataNode);
		}
		return ResponseGenerator.failureResponse();
	}

	@PostMapping(value = PathRoutes.Endpoints.FILTER_MAPPINGS)
	public String filterByMappings(@RequestBody FilterList filterList,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		List<DataNode> dataNodes = fracService.filterByMappings(filterList, userInfo.getSub());
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.REVIEW_MAPPINGS)
	public String reviewMappings(@RequestBody MappingVerification mappingVerification) throws JsonProcessingException {
		Boolean status = fracService.reviewMappings(mappingVerification);
		if (status != null && status) {
			return ResponseGenerator.successResponse(status);
		} else {
			return ResponseGenerator.failureResponse(Constants.ResponseMessages.UNABLE_TO_VERIFY);
		}
	}

	@PostMapping(value = PathRoutes.Endpoints.FILTER_REVIEW_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String filterReviewNodes(@RequestBody FilterList filterList) throws IOException {
		DataNodesVerificationView dataNodes = fracService.filterReviewNodes(filterList);
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		}
		return ResponseGenerator.failureResponse();
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_SOURCE_LIST)
	public String getSourceList(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type)
			throws JsonProcessingException {
		List<String> source = fracService.getSourceList(type);
		if (source != null) {
			return ResponseGenerator.successResponse(source);
		}
		return ResponseGenerator.failureResponse();
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_MY_GRAPHS)
	public String getMyGraphs(
			@RequestParam(value = Constants.Parameters.VISUALIZATION_CODE, required = false) String visualizationCode,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		return ResponseGenerator.successResponse(fracService.getMyGraphs(visualizationCode, userInfo.getSub()));

	}

	@GetMapping(value = PathRoutes.Endpoints.FLUSH_RELOAD_CACHE)
	public String flushReloadCache(@RequestParam(value = Constants.Parameters.TYPE, required = false) String type,
			@RequestParam(value = "flush", required = false) Boolean flush,
			@RequestParam(value = "reload", required = false) Boolean reload) throws JsonProcessingException {
		Boolean updated = fracService.flushReloadCache(type, flush, reload);
		return updated ? ResponseGenerator.successResponse() : ResponseGenerator.failureResponse();
	}

	@PostMapping(value = PathRoutes.Endpoints.CLOUD_STORAGE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String cloudStorage(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
			@RequestParam(value = "folderName", required = false) String folderName) throws JsonProcessingException {
		try {
			File file = new File(multipartFile.getOriginalFilename());
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(multipartFile.getBytes());
			fos.close();
			Map<String, String> uploadedFile = CloudStore.uploadFile(folderName, file);
			file.delete();
			if (uploadedFile != null) {
				uploadedFile.put(Constants.Parameters.FILE_TYPE, multipartFile.getContentType());
				return ResponseGenerator.successResponse(uploadedFile);
			}
		} catch (Exception e) {
			logger.error("Error in cloudStorage : " + e.getMessage());
		}
		return ResponseGenerator.failureResponse();
	}

	@DeleteMapping(value = PathRoutes.Endpoints.DELETE_CLOUD_FILE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteCloudFile(@RequestParam(value = "fileName", required = true) String fileName)
			throws JsonProcessingException {
		Boolean deleted = CloudStore.deleteFile(fileName);
		return deleted ? ResponseGenerator.successResponse() : ResponseGenerator.failureResponse();
	}

	@GetMapping(value = PathRoutes.Endpoints.RELOAD_DICTIONARY)
	public String reloadDictionary() throws JsonProcessingException {
		Boolean loaded = dictionaryService.reloadDictionary();
		return loaded ? ResponseGenerator.successResponse() : ResponseGenerator.failureResponse();
	}

	@PostMapping(value = PathRoutes.Endpoints.TRIGGER_AUDIT_EVENT)
	public String triggerAuditEvent(@RequestBody Map<String, Object> search,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		dataNodeService.getReviewedNode(search, userInfo);
		return ResponseGenerator.successResponse("Telemetry event trigger started");
	}

	@GetMapping(value = PathRoutes.Endpoints.GET_PROPERTY_COUNT_LIST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPropertyCountList(@RequestParam(value = Constants.Parameters.TYPE, required = true) String type)
			throws JsonProcessingException {
		KeyValueList compArea = fracService.getPropertyCountList(type);
		return compArea != null ? ResponseGenerator.successResponse(compArea) : ResponseGenerator.failureResponse();
	}

	@PostMapping(value = PathRoutes.EndpointsVersion.V2
			+ PathRoutes.Endpoints.GET_ALL_NODES, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAllDataNodes(@RequestBody RequestObject reqObject,
			@RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws JsonProcessingException {
		reqObject.setUserId(userInfo.getSub());
		Map<String, Object> dataNodes = fracService.getAllDataNodes(reqObject, PathRoutes.EndpointsVersion.V2);
		if (dataNodes != null) {
			return ResponseGenerator.successResponse(dataNodes);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}

	@PatchMapping(value = PathRoutes.Endpoints.PRIVATE_UPDATE)
	private String privateMethodToUpdateDB(@RequestBody Map<String, Object> request,
										   @RequestAttribute(Constants.Parameters.USER_INFO) UserInfo userInfo) throws IOException {
		List<DataNode> dataNodeList = fracService.privateMethodToUpdateDB(request,userInfo.getSub());
		if (dataNodeList != null) {
			return ResponseGenerator.successResponse(dataNodeList);
		} else {
			return ResponseGenerator.failureResponse();
		}
	}
}