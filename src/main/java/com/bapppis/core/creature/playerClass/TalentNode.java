package com.bapppis.core.creature.playerClass;

import java.util.List;

/**
 * Represents a single node in a talent tree.
 * A node can have 1-3 choices (usually 1).
 * Only one choice from a node can be selected.
 * Nodes can have prerequisites (other node IDs that must be unlocked first).
 */
public class TalentNode {
  private String id;
  private String name;
  private String description;

  // Prerequisites: node IDs that must be unlocked before this node
  private List<String> requires;

  // Choices: 1-3 mutually exclusive options (usually just 1)
  private List<TalentChoice> choices;

  // Display/UI positioning
  private Integer row; // Visual row in the tree
  private Integer column; // Visual column in the tree
  private String path; // Path name (e.g., "Holy", "Protection", "Combat")

  public TalentNode() {
  }

  // Getters and setters

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getRequires() {
    return requires;
  }

  public void setRequires(List<String> requires) {
    this.requires = requires;
  }

  public List<TalentChoice> getChoices() {
    return choices;
  }

  public void setChoices(List<TalentChoice> choices) {
    this.choices = choices;
  }

  public Integer getRow() {
    return row;
  }

  public void setRow(Integer row) {
    this.row = row;
  }

  public Integer getColumn() {
    return column;
  }

  public void setColumn(Integer column) {
    this.column = column;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Checks if this node has prerequisites
   */
  public boolean hasPrerequisites() {
    return requires != null && !requires.isEmpty();
  }

  /**
   * Checks if this node has multiple choices
   */
  public boolean hasMultipleChoices() {
    return choices != null && choices.size() > 1;
  }

  /**
   * Gets the choice by ID
   */
  public TalentChoice getChoiceById(String choiceId) {
    if (choices == null)
      return null;
    return choices.stream()
        .filter(c -> c.getId().equals(choiceId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String toString() {
    return "TalentNode{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", path='" + path + '\'' +
        ", choices=" + (choices != null ? choices.size() : 0) +
        '}';
  }
}
