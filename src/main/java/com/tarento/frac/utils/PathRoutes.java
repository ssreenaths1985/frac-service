package com.tarento.frac.utils;

public class PathRoutes {

	public class Endpoints {
		private Endpoints() {
		}

		public static final String FRAC_ROOT = "/frac";
		public static final String ADD_DATANODE = "/addDataNode";
		public static final String ADD_DATANODES = "/addDataNodes";
		public static final String ADD_POSITION = "/addPosition";
		public static final String GET_ALL_NODES = "/getAllNodes";
		public static final String MAP_NODES = "/mapNodes";
		public static final String APPEND_MAP_NODES = "/appendMapNodes";
		public static final String GET_CHILD_NODES = "/getChildNodes";
		public static final String GET_PARENT_NODES = "/getParentNodes";
		public static final String GET_NODE_BY_ID = "/getNodeById";
		public static final String DELETE_NODE = "/deleteNode";
		public static final String EXPLORE_ALL_NODES = "/exploreAllNodes";
		public static final String GET_MAPPING = "/getMapping";
		public static final String GET_COUNT_OF_NODES = "/getCountOfNodes";
		public static final String SEARCH_NODES = "/searchNodes";
		public static final String GET_ALL_POSITIONS = "/getAllPositions";
		public static final String ADD_ROLE = "/addRole";
		public static final String ADD_ACTIVITY = "/addActivity";
		public static final String ADD_KNOWLEDGE_RESOURCE = "/addKnowledgeResource";
		public static final String GET_CONTENT_SEARCH = "/getContentSearch";
		public static final String EXPLORE_SEARCH = "/exploreSearch";

		public static final String NODE_FEEDBACK = "/nodeFeedback";
		public static final String GET_NODE_FEEDBACK = "/getNodeFeedback";
		public static final String GET_NODE_RATING_AVERAGE = "/getNodeRatingAverage";

		public static final String VERIFY_DATANODE = "/verifyDataNode";
		public static final String VERIFY_ALL_DATANODE = "/verifyAllDataNode";
		public static final String GET_VERIFICATION_LIST = "/getVerificationList";
		public static final String FILTER_NODES = "/filterNodes";
		public static final String GET_COMPETENCY_AREA_LISTING = "/getCompetencyAreaListing";
		public static final String GET_COLLECTION_LOGS = "/getCollectionLogs";
		public static final String ADD_DATA_NODE_BULK = "/addDataNodeBulk";
		public static final String UPLOAD_DATA_NODE = "/uploadDataNode";

		public static final String BOOKMARK_DATA_NODE = "/bookmarkDataNode";
		public static final String GET_BOOKMARK = "/getBookmark";
		public static final String FILTER_MAPPINGS = "/filterByMappings";
		public static final String REVIEW_MAPPINGS = "/reviewMappings";

		public static final String FILTER_REVIEW_NODES = "/filterReviewNodes";
		public static final String GET_SOURCE_LIST = "getSourceList";
		public static final String GET_MY_GRAPHS = "/getMyGraphs";
		public static final String FLUSH_RELOAD_CACHE = "/flushReloadCache";
		public static final String RELOAD_DICTIONARY = "reloadDictionary";
		public static final String TRIGGER_AUDIT_EVENT = "/triggerAuditEvent";
		public static final String GET_PROPERTY_COUNT_LIST = "/getPropertyCountList";

		public static final String CLOUD_STORAGE = "/cloudStorage";
		public static final String DELETE_CLOUD_FILE = "/deleteCloudFile";
		public static final String PRIVATE_UPDATE = "/privateUpdate";

	}

	public class EndpointsVersion {
		public static final String V2 = "/v2";
	}
}