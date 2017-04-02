package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.model.Item;
import hudson.model.Node;
import jenkins.util.SystemProperties;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SharedStorageWorkspaceManager manages the mapping between node and remote root and also the
 * mapping between project and workspace
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
class SharedStorageWorkspaceManager {
  private static final Logger LOGGER = Logger.getLogger(SharedStorageWorkspaceManager.class.getName());

  private static SharedStorageWorkspaceManager SharedStorageWorkspaceManager = new SharedStorageWorkspaceManager();

  private SharedStorageWorkspaceManager() {
  }

  static SharedStorageWorkspaceManager getSharedStorageWorkspaceManager() {
    return SharedStorageWorkspaceManager;
  }

  private static final BidiMap<Node, String> nodeWorkspaceMap = new DualHashBidiMap<Node, String>();
  private static final Map<Item, String> projectWorkspaceMap = new HashMap<Item, String>();

  synchronized String deallocateRootPath(@Nonnull Node node) {
    if (nodeWorkspaceMap.containsKey(node)) {
      String workspace = nodeWorkspaceMap.get(node);

      LOGGER.info("Deallocating the root workspace - " + workspace + " from the node - " + node.getDisplayName());

      nodeWorkspaceMap.remove(node);

      return workspace;
    }

    return null;
  }

  synchronized String reallocateRootPath(@Nonnull Node oldOne, @Nonnull Node newOne) {
    if (nodeWorkspaceMap.containsKey(oldOne)) {
      String workspace = nodeWorkspaceMap.get(oldOne);

      nodeWorkspaceMap.remove(oldOne);
      nodeWorkspaceMap.put(newOne, workspace);

      LOGGER.info("Reallocating the workspace - " + workspace + " from node - " + oldOne.getDisplayName() +
          " to node - " + newOne.getDisplayName());

      return workspace;
    }

    return allocateRootPath(newOne);
  }

  synchronized String allocateRootPath(@Nonnull Node node) {
    String base = node.getRootPath().getRemote();
    for (int i = 1; ; i++) {
      String candidate = i == 1 ? base : (base + COMBINATOR + i);
      if (!nodeWorkspaceMap.containsValue(candidate)) {
        nodeWorkspaceMap.put(node, candidate);

        LOGGER.info("Allocating the workspace - " + candidate + " to the node - " + node.getDisplayName());

        return candidate;
      }
    }
  }

  synchronized String getRootPath(@Nonnull Node node) {
    return nodeWorkspaceMap.get(node);
  }

  synchronized void removeProjectWorkspace(@Nonnull Item item) {
    if (projectWorkspaceMap.containsKey(item)) {
      LOGGER.info("Unlinking the workspace - " + projectWorkspaceMap.get(item) + " from " + item.getDisplayName());

      projectWorkspaceMap.remove(item);
    }
  }

  synchronized void updateProjectWorkspace(@Nonnull Item item, @Nonnull String workspace) {
    LOGGER.info("Updating the project - " + item.getDisplayName() + " to " + workspace);

    projectWorkspaceMap.put(item, workspace);
  }

  synchronized String getProjectWorkspace(@Nonnull Item item) {
    return projectWorkspaceMap.get(item);
  }

  /**
   * The token that combines the project name and unique number to create unique workspace directory.
   */
  private static final String COMBINATOR = SystemProperties.getString(SharedStorageWorkspaceManager.class.getName(), "@");
}
