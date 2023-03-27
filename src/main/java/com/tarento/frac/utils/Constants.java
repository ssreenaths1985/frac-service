package com.tarento.frac.utils;

public final class Constants {

	public class ServiceRepositories {
		private ServiceRepositories() {
		}

		public static final String COMMON_SERVICE = "commonService";
		public static final String FRAC_SERVICE = "fracService";
		public static final String FRAC_DAO = "fracDao";
		public static final String COMMON_DAO = "CommonDao";
		public static final String AUTH_UTIL = "authUtil";
		public static final String DICTIONARY_SERVICE = "dictionaryService";
		public static final String NOTIFICATION_UTIL = "notificationUtil";
	}

	public class Instances {
		private Instances() {
		}

		public static final String STAGE = "stage";
		public static final String PRODUCTION = "production";
	}

	public class RequestMethods {
		private RequestMethods() {
		}

		public static final String GET = "GET";
		public static final String POST = "POST";
		public static final String OPTIONS = "OPTIONS";
		public static final String DELETE = "DELETE";
		public static final String PUT = "PUT";
	}

	public class ResponseCodes {
		private ResponseCodes() {
		}

		public static final int UNAUTHORIZED_ID = 401;
		public static final int SUCCESS_ID = 200;
		public static final int FAILURE_ID = 320;
		public static final String UNAUTHORIZED = "Invalid credentials. Please try again.";
		public static final String PROCESS_FAIL = "Process failed, Please try again.";
		public static final String SUCCESS = "success";
	}

	public class Parameters {
		private Parameters() {
		}

		public static final String STAGE = "stage";
		public static final String PRODUCTION = "production";
		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String LEVEL = "level";
		public static final String LEVEL_ID = "levelId";
		public static final String WORKFLOW_ID = "workflowId";
		public static final String KEYWORD = "keyword";
		public static final String TYPE = "type";
		public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
		public static final String ROLES = "roles";
		public static final String POSITIONS = "positions";
		public static final String ACTIVITIES = "activities";
		public static final String KNOWLEDGE_RESOURCES = "knowledgeResources";
		public static final String COMPETENCIES = "competencies";
		public static final String PUBLISHED = "isPublished";
		public static final String SAVED = "isSaved";
		public static final String ACTIVE = "isActive";
		public static final String DETAIL = "isDetail";
		public static final String DELETED = "isDeleted";
		public static final String ID = "id";
		public static final String UNDERSCORE_ID = "_id";
		public static final String ID_KEYWORD = "id.keyword";
		public static final String PARENT = "parent";
		public static final String PARENT_ID = "parentId";
		public static final String CHILD = "child";
		public static final String CHILD_IDS = "childIds";
		public static final String PROFILE_NAME = "profileName";
		public static final String DEFAULT_PROFILE = "default";
		public static final String GETALL = "getAll";
		public static final String POSITION = "position";
		public static final String POSITION_PREFIX = "positionPrefix";
		public static final String ROLE = "role";
		public static final String ROLE_PREFIX = "rolePrefix";
		public static final String ACTIVITY = "activity";
		public static final String ACTIVITY_PREFIX = "activityPrefix";
		public static final String COMPETENCE = "competence";
		public static final String COMPETENCE_PREFIX = "competencePrefix";
		public static final String KNOWLEDGE_RESOURCE = "knowledgeResource";
		public static final String COMPETENCIES_LEVEL = "competenciesLevel";
		public static final String COMPETENCIES_LEVEL_PREFIX = "competenciesLevelPrefix";
		public static final String COMPETENCIES_AREA = "competencyArea";
		public static final String COMPETENCIES_AREA_PREFIX = "competencyAreaPrefix";
		public static final String KNOWLEDGE_PREFIX = "knowledgePrefix";
		public static final String RATING = "rating";
		public static final String USER = "user";
		public static final String COMMENTS = "comments";
		public static final String FEEDBACKS = "feedbacks";
		public static final String STATUS = "status";
		public static final String SOURCE = "source";
		public static final String AUTHORIZATION = "Authorization";
		public static final String DEPARTMENT = "department";
		public static final String REVIEW_COMMENTS = "reviewComments";
		public static final String ADDITIONAL_PROPERTIES_DEPT = "additionalProperties.Department.keyword";
		public static final String ADDITIONAL_PROPERTIES_TYPE = "additionalProperties.competencyType.keyword";
		public static final String ADDITIONAL_PROPERTIES_AREA = "additionalProperties.competencyArea.keyword";
		public static final String COMPETENCY_AREA_LISTING = "CompetencyAreaListing";
		public static final String COMPETENCY_TYPE = "competencyType";
		public static final String COD = "cod";
		public static final String COMPETENCYSECTOR = "competencySector";
		public static final String URL = "URL";
		public static final String FILES = "files";
		public static final String COMPETENCY_SOURCE = "competencySource";
		public static final String CONCAT_KEYWORD = ".keyword";
		public static final String CREATED_DATE = "createdDate";
		public static final String USERINFO_SUB_KEY = "userInfo.sub" + CONCAT_KEYWORD;
		public static final String UPDATED_BY = "updatedBy";
		public static final String UPDATED_BY_EMAIL = "updatedByEmail";
		public static final String UPDATED_DATE = "updatedDate";
		public static final String REVIEWED_BY = "reviewedBy";
		public static final String CHANGE_STATEMENT = "changeStatement";
		public static final String REFERENCE = "reference";
		public static final String CHANGED = "Changed";
		public static final String FROM = "from";
		public static final String TO = "to";
		public static final String SEPARATOR = "::";
		public static final String ITEM_CREATED = "Item created";
		public static final String UPDATED = "Updated";
		public static final String KR = "Knowledge Resource";
		public static final String COMPETENCY_LEVEL = "Competency Level";
		public static final String COMPETENCY_AREA = "Competency Area";
		public static final String COMP_TYPE = "Competency Type";
		public static final String INDEX = "index";
		public static final String ACTION = "action";
		public static final String DATA_NODE = "datanode";
		public static final String OPERATION = "operation";
		public static final String VISUALIZATION_CODE = "visualizationCode";
		public static final String USER_TYPE = "userType";
		public static final String PD = "password";
		public static final String CLIENT_ID = "client_id";
		public static final String USERNAME = "username";
		public static final String FILE_TYPE = "fileType";
		public static final String VALUE = "value";
		public static final String USER_INFO = "userInfo";
		public static final int DEFAULT_LIMIT = 100;
		public static final String COUNT = "count";

		public static final String GRANT_TYPE = "grant_type";
		public static final String ACCESSTOKENSPEC_JSON = "/accesstokenspec.json";
		public static final String X_USER_TOKEN = "x-authenticated-user-token";
		public static final String ORIGIN = "Origin";
		public static final String BEARER = "bearer ";
		public static final String UNAUTHORIZED = "Unauthorized";

		// Keycloak validation
		public static final String ANONYMOUS = "Anonymous";
		public static final String DOT_SEPARATOR = ".";
		public static final String SHA_256_WITH_RSA = "SHA256withRSA";
		public static final String SUB = "sub";
		public static final String ISS = "iss";
		public static final String EXP = "exp";
		public static final String KID = "kid";
		public static final String UPDATES = "updates";
		public static final String NODE_ID = "nodeId";
		public static final String FIELDS_TO_BE_UPDATED = "fieldsToBeUpdated";
		public static final String KEY = "key";
	}

	public class WorkflowParameters {
		private WorkflowParameters() {
		}

		public static final String CONDITION = "condition";
		public static final String ACCESS = "access";
		public static final String USERS = "users";
		public static final String WORKFLOW = "workflow";
	}

	public class ResponseMessages {
		private ResponseMessages() {
		}

		public static final String ERROR_MESSAGE = "Unable to fetch the details. Please try again later!";
		public static final String NOT_CREATED_MESSAGE = "Unable to create. Please try again later!";
		public static final String UNABLE_TO_VERIFY = "Verification was not successful. Please try again later !";
		public static final String UNABLE_TO_AUTHENTICATE = "Unable to Authenticate. Please check with your admin!";
		public static final String UNAUTHORIZED_ACCESS = "Unauthorized access, Please check your permissions";
		public static final String AUTH_TOKEN_MISSING = "Auth token is missing";
	}

	public class Markers {
		private Markers() {
		}

		public static final String DAO_IMPL = "DAOIMPL";
	}

	public class Actions {
		private Actions() {
		}

		public static final String CREATE = "Creation";
		public static final String PUBLISH = "Publish";
		public static final String UPLOAD = "Upload";
		public static final String UPDATE = "Update";
		public static final String TECHNICAL_REVIEW = "Technical review";
		public static final String REVIEW_BOARD = "Review board";
		public static final String REVIEW = "Review";
		public static final String DELETE = "Delete";
	}

	public class UserType {
		private UserType() {
		}

		public static final String REVIEWER_ONE = "FRAC_REVIEWER_L1";
		public static final String REVIEWER_TWO = "FRAC_REVIEWER_L2";
		public static final String ADMIN = "FRAC_ADMIN";
		public static final String COMPETENCY_REVIEWER = "FRAC_COMPETENCY_REVIEWER";
	}

	public class SqlParams {
		private SqlParams() {
		}

		public static final String SECONDARY_STATUS = "secondary_status";
		public static final String PROP_KEY = "prop_key";
		public static final String PROP_VALUE = "prop_value";
		public static final String SUB_ID = "sub_id";
		public static final String SUB_NAME = "sub_name";
		public static final String SUB_DESCRIPTION = "sub_description";
		public static final String PROP_NAME = "prop_name";
	}

}