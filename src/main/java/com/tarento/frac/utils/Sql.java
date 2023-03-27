package com.tarento.frac.utils;

public class Sql {

	public static class DataNode {

		public static final String ADD_DATA_NODE = "INSERT INTO data_node (id, type, name, description, status, source, level, created_date, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		public static final String ADD_NODE_PROPERTY = "INSERT INTO additional_properties (node_id, prop_key,prop_name ,prop_value) VALUES (?, ?, ?, ?)";
		public static final String ADD_MAPPINGS = "INSERT INTO node_mapping (parent, parent_id, child, child_id, status) VALUES (?, ?, ?, ?, ?)";

		public static final String ADD_PARENT_MAPPINGS = "INSERT INTO node_mapping_parent (parent, parent_id, child, status) VALUES (?, ?, ?, ?)";
		public static final String ADD_CHILD_MAPPINGS = "INSERT INTO node_mapping_child (id, child_id) VALUES (?,?)";
		public static final String DELETE_CHILD_MAPPINGS = "DELETE FROM node_mapping_child where id = ?";

		public static final String UPDATE_DATA_NODE = "UPDATE data_node SET name = ?, description = ?, status = ?, secondary_status = ?, source = ?, level = ?, updated_date = ?, updated_by= ? WHERE id = ?";
		public static final String UPDATE_NODE_ACTIVE_STATUS = "UPDATE data_node SET is_active = ? WHERE id=?";
		public static final String UPDATE_NODE_STATUS = "UPDATE data_node SET status = ? WHERE id = ?";
		public static final String UPDATE_NODE_STATUS_AND_COMMENTS = "UPDATE data_node SET status = ?, secondary_status = ?, review_comments = ?, secondary_review_comments = ? WHERE id = ?";

		public static final String DELETE_DATA_NODE = "DELETE FROM data_node WHERE id= ?";
		public static final String DELETE_NODE_PROPERTY = "DELETE FROM additional_properties WHERE node_id = ?";
		public static final String DELETE_MAPPINGS = "DELETE FROM node_mapping WHERE parent_id = ? AND child = ?";

		public static final String GET_NODE = "SELECT id, type, name, description, status, secondary_status as secondaryStatus, review_comments AS reviewComments, secondary_review_comments AS secondaryReviewComments, source, level, is_active AS active FROM data_node";
		public static final String GET_NODE_AND_PROPERTY = "SELECT data_node.id, type, name, description, status, secondary_status as secondaryStatus, created_by as createdBy, created_date as createdDate, source, level, review_comments AS reviewComments, secondary_review_comments AS secondaryReviewComments, prop_key, prop_name, prop_value"
				+ " FROM  data_node LEFT JOIN additional_properties  ON data_node.id = additional_properties.node_id";
		public static final String GET_ALL_NODE_AND_PROPERTY = "SELECT data_node.id, type, name, description, status, secondary_status AS secondaryStatus, source, level, review_comments AS reviewComments,"
				+ "secondary_review_comments AS secondaryReviewComments, created_date AS createdDate, created_by AS createdBy, "
				+ "updated_date AS updatedDate, updated_by AS updatedBy, reviewed_date AS reviewedDate, reviewed_by AS reviewedBy, is_active as active,"
				+ "prop_key, prop_name, prop_value FROM  data_node LEFT JOIN additional_properties  ON data_node.id = additional_properties.node_id";
		public static final String FILTER_BY_USERS_DRAFT = "((status = \"DRAFT\" and created_by = ?) OR status NOT LIKE \"DRAFT\" OR status IS NULL)";
		public static final String GET_DEPARTMENT = "SELECT prop_value as department FROM additional_properties where prop_key='department' and node_id = \'";
		public static final String TYPE = "and type=?";

		public static final String GET_NODE_ID_STATUS_FROM_DEPARTMENT = "and id in (SELECT node_id FROM additional_properties where prop_key='department' and prop_value = ?);";

		public static final String ID_CONDITION = " id = ? ";
		public static final String DATA_NODE_ID_CONDITION = " data_node.id = ? ";
		public static final String ACTIVE_CONDITION = " is_active = true ";
		public static final String TYPE_CONDITION = " type = ? ";
		public static final String DATA_NODE_TYPE_CONDITION = " data_node.type = ? ";
		public static final String STATUS_CONDITION = " status = ? ";
		public static final String CREATED_BY_CONDITION = " created_by = ? ";
		public static final String DEPARTMENT_CONDITION = " data_node.id in (SELECT node_id FROM additional_properties where prop_key='department' and prop_value like ?) ";
		public static final String ID_IN = "  id in (?) ";

		public static final String GET_ALL_NODE_MAPPINGS = "SELECT parent.id, parent, parent_id AS parentId, child, child_id AS childId, status FROM node_mapping_parent AS parent LEFT JOIN node_mapping_child AS child ON parent.id = child.id";
		public static final String GET_ALL_CHILD_NODE_BY_TYPE = "SELECT node.id, type, name, description, status, source, level from node_mapping_child as mapping left join data_node as node on mapping.child_id = node.id where mapping.id  = (select id from node_mapping_parent where parent_id = ? and child = ?) and node.is_active is true";
		public static final String GET_MAPPINGS_BY_COLLECTION_TYPE = "SELECT parent, parent_id AS parentId, child_id AS childId, child, status FROM node_mapping_parent, node_mapping_child WHERE node_mapping_parent.id=node_mapping_child.id and child in (\"POSITION\", \"ROLE\", \"COMPETENCY\", \"KNOWLEDGERESOURCE\")";

		public static final String GET_ALL_PARENT_NODE_BY_TYPE = "SELECT node.id, type, name, description, mapping.status, source FROM node_mapping_parent mapping LEFT JOIN data_node AS node ON node.id = mapping.parent_id WHERE mapping.id IN (SELECT id FROM node_mapping_child WHERE child_id = ?) AND type = ?";
		public static final String GET_NODE_PROPERTY = "SELECT node_id AS id, prop_key , prop_name, prop_value FROM additional_properties";
		public static final String DELET_NODE_PROPERTY = "DELETE FROM additional_properties WHERE node_id = ?";

		public static final String DELETE_CHILD_MAPPING_OF_PARENT = "DELETE FROM node_mapping_child WHERE id IN (SELECT id FROM node_mapping_parent WHERE parent_id = ?)";
		public static final String DELETE_NODE_PARENT_MAPPING = "DELETE FROM node_mapping_parent WHERE parent_id = ?";
		public static final String DELETE_NODE_CHILD_MAPPING = "DELETE FROM node_mapping_child WHERE child_id = ?";

		// Node verification
		public static final String LEVEL_ONE_REVIEW = "UPDATE data_node SET status =?, review_comments = ?, reviewed_by = ?, reviewed_date = ? WHERE id= ?";
		public static final String LEVEL_ONE_REVIEW_WITH_SECONDARY = "UPDATE data_node SET status =?, review_comments = ?, secondary_status = ?,reviewed_by = ?, reviewed_date = ? WHERE id= ?";
		public static final String LEVEL_TWO_REVIEW = "UPDATE data_node SET secondary_status =?, secondary_review_comments = ?, reviewed_by = ?, reviewed_date = ? WHERE id= ?";
		public static final String LEVEL_TWO_REVIEW_WITH_PRIMARY = "UPDATE data_node SET status= ?, secondary_status =?, secondary_review_comments = ?, reviewed_by = ?, reviewed_date = ? WHERE id= ?";
		public static final String GET_ALL_UNIQUE_SOURCE = "SELECT distinct(source) FROM data_node";

		public static final String GET_STORAGE_SERVICE = "SELECT * FROM storage_service WHERE provider = ?";

		public static final String GET_PROPERTY_NODE = "SELECT id, prop_value AS name, TRUE AS active FROM additional_properties WHERE prop_key = ? UNION SELECT id, name, FALSE AS active FROM data_node WHERE type = ?";

		public static final String GET_NODE_KEYS = "SELECT id, type, prefix, count FROM node_keys";
		public static final String UPDATE_NODE_KEY_COUNT = "UPDATE node_keys SET count= ? WHERE id= ?";
		public static final String DATA_NODE_COUNT = "SELECT count(*) FROM  data_node ";

		private DataNode() {
		}
	}

	public static class Common {
		public static final String WHERE_CLAUSE = " where ";
		public static final String WHERE_NOT_DRAFT_CLAUSE = " where (status != 'DRAFT' OR status IS NULL)";
		public static final String AND_CLAUSE = " AND ";
		public static final String OR_CLAUSE = " OR ";

		public static final String APPEND_VALUE = " = ? ";
		public static final String OPEN_BRACE = "(";
		public static final String CLOSE_BRACE = ")";

		public static final String NOT_NULL_CHECK = " IS NOT NULL ";
		public static final String LIMIT = " limit ? ";
		public static final String OFFSET = " offset ? ";

		private Common() {
		}
	}

	public static class Bookmark {

		private Bookmark() {
		}

		public static final String ADD_BOOKMARK = "INSERT INTO bookmarks (node_id, type, user_id) VALUES (?, ?, ?)";
		public static final String DELETE_BOOKMARK = "DELETE FROM bookmarks WHERE node_id= ? and user_id = ?";
		public static final String DELETE_BOOKMARK_BY_NODE_ID = "DELETE FROM bookmarks WHERE node_id= ?";
		public static final String GET_BOOKMARK = "SELECT node_id as id , bookmarks.type, name, description, status, source FROM bookmarks LEFT JOIN data_node ON data_node.id = bookmarks.node_id WHERE is_active IS TRUE AND user_id = ?";
	}

}
