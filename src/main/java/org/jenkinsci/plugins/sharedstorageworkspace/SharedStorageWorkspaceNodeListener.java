package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.Extension;
import hudson.model.Node;
import jenkins.model.NodeListener;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * The Node Listener extension allocates, reallocates and deallocates the root workspace
 * for the node according to the creation, updation and deletion of nodes
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
@Extension
public class SharedStorageWorkspaceNodeListener extends NodeListener {
  private static final Logger LOGGER = Logger.getLogger(SharedStorageWorkspaceNodeListener.class.getName());

  @Override
  protected void onDeleted(@Nonnull Node node) {
    String workspace = SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().deallocateRootPath(node);
    LOGGER.info("The node - " + node.getDisplayName() + " is being deleted and the corresponding mapping to workspace " + workspace + "is removed");

    super.onDeleted(node);
  }

  @Override
  protected void onUpdated(@Nonnull Node oldOne, @Nonnull Node newOne) {
    String workspace = SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().reallocateRootPath(oldOne, newOne);
    LOGGER.info("The node " + oldOne.getDisplayName() + " is being updated and hence the workspace mapping is updated to " + workspace);

    super.onUpdated(oldOne, newOne);
  }

  @Override
  protected void onCreated(@Nonnull Node node) {
    String workspace = SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().allocateRootPath(node);
    LOGGER.info("The node - " + node.getDisplayName() + " is mapped to the workspace - " + workspace);

    super.onCreated(node);
  }
}