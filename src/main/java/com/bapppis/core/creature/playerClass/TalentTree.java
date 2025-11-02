package com.bapppis.core.creature.playerClass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a complete talent tree for a player class.
 * Talent trees are JSON-driven and define progression paths within a class.
 */
public class TalentTree {
  private int id;
  private String name;
  private String description;
  private Integer classId; // PlayerClass ID this tree belongs to

  // All nodes in the tree
  private List<TalentNode> nodes;

  // UI/display
  private String tooltip;

  public TalentTree() {
  }

  // Getters and setters

  public int getId() {
    return id;
  }

  public void setId(int id) {
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

  public Integer getClassId() {
    return classId;
  }

  public void setClassId(Integer classId) {
    this.classId = classId;
  }

  public List<TalentNode> getNodes() {
    return nodes;
  }

  public void setNodes(List<TalentNode> nodes) {
    this.nodes = nodes;
  }

  public String getTooltip() {
    return tooltip;
  }

  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  /**
   * Gets a node by its ID
   */
  public TalentNode getNodeById(String nodeId) {
    if (nodes == null)
      return null;
    return nodes.stream()
        .filter(n -> n.getId().equals(nodeId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets all nodes on a specific path
   */
  public List<TalentNode> getNodesByPath(String path) {
    if (nodes == null)
      return null;
    return nodes.stream()
        .filter(n -> path.equals(n.getPath()))
        .collect(Collectors.toList());
  }

  /**
   * Gets all unique paths in this tree
   */
  public Set<String> getAllPaths() {
    if (nodes == null)
      return null;
    return nodes.stream()
        .map(TalentNode::getPath)
        .filter(p -> p != null)
        .collect(Collectors.toSet());
  }

  /**
   * Checks if all prerequisites for a node are met
   */
  public boolean arePrerequisitesMet(TalentNode node, Set<String> unlockedNodeIds) {
    if (!node.hasPrerequisites())
      return true;

    for (String requiredNodeId : node.getRequires()) {
      if (!unlockedNodeIds.contains(requiredNodeId)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets all nodes that can currently be unlocked based on unlocked nodes
   */
  public List<TalentNode> getAvailableNodes(Set<String> unlockedNodeIds) {
    if (nodes == null)
      return null;

    return nodes.stream()
        .filter(n -> !unlockedNodeIds.contains(n.getId())) // Not already unlocked
        .filter(n -> arePrerequisitesMet(n, unlockedNodeIds)) // Prerequisites met
        .collect(Collectors.toList());
  }

  /**
   * Gets total number of nodes in the tree
   */
  public int getTotalNodeCount() {
    return nodes != null ? nodes.size() : 0;
  }

  @Override
  public String toString() {
    return "TalentTree{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", classId=" + classId +
        ", nodes=" + (nodes != null ? nodes.size() : 0) +
        '}';
  }
}
