package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.BulkChange;
import hudson.FilePath;
import hudson.XmlFile;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SharedStorageWorkspaceManager manages the mapping between node and remote root and also the
 * mapping between project and workspace
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
class SharedStorageWorkspaceManager implements Saveable {
  private static final Logger LOGGER = Logger.getLogger(SharedStorageWorkspaceManager.class.getName());

  private static SharedStorageWorkspaceManager SharedStorageWorkspaceManager = new SharedStorageWorkspaceManager();

  private SharedStorageWorkspaceManager() {
    load();
  }

  static SharedStorageWorkspaceManager getSharedStorageWorkspaceManager() {
    return SharedStorageWorkspaceManager;
  }

  private transient final BidiMap<Node, String> nodeWorkspaceMap = new DualHashBidiMap<Node, String>();
  private final Map<String, String> projectWorkspaceMap = new HashMap<String, String>();
  private final Map<String, Date> nodeWorkspaceLastUsedMap = new HashMap<String, Date>();

  synchronized void cleanupNodeWorkspace() {
    long currentTime = (new Date()).getTime();
    Set<String> workspaceSet = nodeWorkspaceLastUsedMap.keySet();
    for (String workspace : workspaceSet) {
      if (currentTime - nodeWorkspaceLastUsedMap.get(workspace).getTime() > DAYS_30_IN_MILLIS) {
        nodeWorkspaceLastUsedMap.remove(workspace);
        //Probably there's a stale node-workspace mapping
        nodeWorkspaceMap.removeValue(workspace);

        try {
          (new FilePath(Jenkins.getInstance().getChannel(), workspace)).deleteRecursive();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      this.save();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  synchronized String deallocateRootPath(@Nonnull Node node) {
    if (nodeWorkspaceMap.containsKey(node)) {
      String workspace = nodeWorkspaceMap.get(node);

      LOGGER.info("Deallocating the root workspace - " + workspace + " from the node - " + node.getDisplayName());

      nodeWorkspaceMap.remove(node);
      nodeWorkspaceLastUsedMap.put(workspace, new Date());

      try {
        this.save();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return workspace;
    }

    return null;
  }

  synchronized String reallocateRootPath(@Nonnull Node oldOne, @Nonnull Node newOne, String ws) {
    if (nodeWorkspaceMap.containsKey(oldOne)) {
      String workspace = nodeWorkspaceMap.get(oldOne);

      nodeWorkspaceMap.remove(oldOne);
      nodeWorkspaceMap.put(newOne, workspace);
      nodeWorkspaceLastUsedMap.put(workspace, new Date());

      LOGGER.info("Reallocating the workspace - " + workspace + " from node - " + oldOne.getDisplayName() +
          " to node - " + newOne.getDisplayName());

      try {
        this.save();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return workspace;
    }

    return allocateRootPath(newOne, ws);
  }

  synchronized String allocateRootPath(@Nonnull Node node, String base) {
    //Limiting the upper limit to 500 - assuming there won't be more than 500 nodes being used for builds at a time
    for (int i = 1; i < 500; i++) {
      String candidate = i == 1 ? base : (base + COMBINATOR + i);
      if (!nodeWorkspaceMap.containsValue(candidate)) {
        nodeWorkspaceMap.put(node, candidate);
        nodeWorkspaceLastUsedMap.put(candidate, new Date());

        LOGGER.info("Allocating the workspace - " + candidate + " to the node - " + node.getDisplayName());

        try {
          this.save();
        } catch (IOException e) {
          e.printStackTrace();
        }

        return candidate;
      }
    }

    return base;
  }

  synchronized String getRootPath(@Nonnull Node node) {
    return nodeWorkspaceMap.get(node);
  }

  synchronized void removeProjectWorkspace(@Nonnull Item item) {
    if (projectWorkspaceMap.containsKey(item.getDisplayName())) {
      LOGGER.info("Unlinking the workspace - " + projectWorkspaceMap.get(item.getDisplayName())
          + " from " + item.getDisplayName());

      projectWorkspaceMap.remove(item.getDisplayName());
    }
  }

  synchronized void updateProjectWorkspace(@Nonnull Item item, @Nonnull String workspace) {
    LOGGER.info("Updating the project - " + item.getDisplayName() + " to " + workspace);

    projectWorkspaceMap.put(item.getDisplayName(), workspace);
  }

  synchronized String getProjectWorkspace(@Nonnull Item item) {
    return projectWorkspaceMap.get(item.getDisplayName());
  }

  private XmlFile getConfigFile() {
    return new XmlFile(Jenkins.XSTREAM2, new File(Jenkins.getInstance().getRootDir(), SharedStorageWorkspaceManager.class.getName() + ".xml"));
  }

  private void load() {
    XmlFile file = getConfigFile();

    if (file.exists()) {
      LOGGER.info("The Shared Storage Workspace Manager being loaded from - " + file.getFile().getName());

      try {
        file.unmarshal(this);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to load " + file, e);
      }
    }
  }

  @Override
  public void save() throws IOException {
    if (BulkChange.contains(this)) {
      return;
    }

    XmlFile file = getConfigFile();
    LOGGER.info("Persisting the Shared Storage Manager state to - " + file.getFile().getName());

    file.write(this);
    SaveableListener.fireOnChange(this, getConfigFile());
  }

  /**
   * The token that combines the project name and unique number to create unique workspace directory.
   */
  private static final String COMBINATOR = SystemProperties.getString(SharedStorageWorkspaceManager.class.getName(), "@");
//  private static final long DAYS_30_IN_MILLIS = 30 * 24 * 60 * 60 * 1000L;
  private static final long DAYS_30_IN_MILLIS = 60 * 60 * 1000L;
}
