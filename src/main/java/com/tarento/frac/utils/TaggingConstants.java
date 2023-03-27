package com.tarento.frac.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Entities;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("all")
public class TaggingConstants {
	public static Map<String, Boolean> getTaggingMap() {
		return TAGGING_MAP;
	}

	private static final Map<String, Boolean> TAGGING_MAP = ImmutableMap.<String, Boolean>builder()
			.put("POSITION", Boolean.TRUE).put("ROLE", Boolean.TRUE).put("COMPETENCY", Boolean.TRUE)
			.put("ACTIVITY", Boolean.TRUE).put("KNOWLEDGERESOURCE", Boolean.FALSE)
			.put("COMPETENCIESLEVEL", Boolean.FALSE).put("COMPETENCYAREA", Boolean.FALSE)
			.put("SECTOR", Boolean.FALSE).build();

	public static Map<String, Boolean> getDraftNodeMap() {
		return DRAFT_NODE;
	}

	private static final Map<String, Boolean> DRAFT_NODE = ImmutableMap.<String, Boolean>builder()
			.put("POSITION", Boolean.TRUE).put("ROLE", Boolean.TRUE).put("COMPETENCY", Boolean.TRUE)
			.put("ACTIVITY", Boolean.TRUE).put("KNOWLEDGERESOURCE", Boolean.TRUE)
			.put("COMPETENCIESLEVEL", Boolean.FALSE).put("COMPETENCYAREA", Boolean.FALSE)
			.put("SECTOR", Boolean.FALSE).build();

	public static Map<String, Boolean> getDictionaryNodeMap() {
		return DICTIONARY_NODE;
	}

	private static final Map<String, Boolean> DICTIONARY_NODE = ImmutableMap.<String, Boolean>builder()
			.put("POSITION", Boolean.TRUE).put("ROLE", Boolean.TRUE).put("COMPETENCY", Boolean.TRUE)
			.put("ACTIVITY", Boolean.TRUE).put("KNOWLEDGERESOURCE", Boolean.FALSE)
			.put("COMPETENCIESLEVEL", Boolean.FALSE).put("COMPETENCYAREA", Boolean.FALSE)
			.put("SECTOR", Boolean.FALSE).build();

	public static Map<String, List<String>> getChildTaggingMap() {
		return CHILD_NODE;
	}

	private static final Map<String, List<String>> CHILD_NODE = ImmutableMap.<String, List<String>>builder()
			.put("POSITION", new ArrayList<>(Arrays.asList(Entities.ROLE.name())))
			.put("ROLE", new ArrayList<>(Arrays.asList(Entities.ACTIVITY.name(), Entities.COMPETENCY.name())))
			.put("COMPETENCY", new ArrayList<>(Arrays.asList(Entities.COMPETENCIESLEVEL.name())))
			.put("ACTIVITY", new ArrayList<>(Arrays.asList(Entities.KNOWLEDGERESOURCE.name())))
			.put("KNOWLEDGERESOURCE", new ArrayList<>()).put("COMPETENCIESLEVEL", new ArrayList<>())
			.put("COMPETENCYAREA", new ArrayList<>()).put("SECTOR", new ArrayList<>()).build();

	public static List<DataNode> getDefaultCompetencyLevels() {
		return COMPETENCY_LEVELS;
	}

	private static final List<DataNode> COMPETENCY_LEVELS = Arrays.asList(new DataNode("1", "Basic",
			"Possesses basic knowledge and skills related to some elements of the competency and is able to apply them with moderate supervision.",
			"Level 1"),
			new DataNode("2", "Proficient",
					"Able to demonstrate knowledge and skills related to most of the elements of the competency and apply them without need for constant supervision.",
					"Level 2"),
			new DataNode("3", "Advanced",
					"Possesses strong knowledge and skills required for the competency and demonstrates an understanding of the interlinkages between competencies. Acts as an advisor on the topic, often producing manuals/notes to support colleagues.",
					"Level 3"),
			new DataNode("4", "Expert",
					"Demonstrates excellence in all capabilities related to the competency compared to best industry benchmarks within the country. Is a person of authority on practices and/or systems related to the competency and is widely consulted on the same.",
					"Level 4"),
			new DataNode("5", "Ustad",
					"Demonstrates complete mastery of the competency and use of it in unprecedented ways. Has a fundamental, outsized impact on their field of knowledge with few other people having similar capabilities.",
					"Level 5"));
}
