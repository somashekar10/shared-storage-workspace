package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TopLevelItem;
import hudson.remoting.VirtualChannel;
import jenkins.slaves.WorkspaceLocator;

import java.util.logging.Logger;

/**
 * The Workspace Locator uses the workspace of the node and returns the corresponding
 * workspace for the project.
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
@Extension
public class SharedStorageWorkspaceLocator extends WorkspaceLocator {
  private static final Logger LOGGER = Logger.getLogger(SharedStorageWorkspaceLocator.class.getName());

  @Override
  public FilePath locate(TopLevelItem topLevelItem, Node node) {
    String rootPath = SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().getRootPath(node);

    if (rootPath == null) {
      rootPath = node.getRootPath().getRemote();
    }

    VirtualChannel channel = node.getChannel();
    FilePath workspaceRoot = new FilePath(channel, rootPath);

    FilePath projectWorkspace = workspaceRoot.child(topLevelItem.getFullName());

    LOGGER.info("The workspace for the project " + topLevelItem.getDisplayName() + " is - "
        + projectWorkspace.getRemote());

    return projectWorkspace;
  }
}