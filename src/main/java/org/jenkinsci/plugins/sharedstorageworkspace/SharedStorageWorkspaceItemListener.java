package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

import java.util.logging.Logger;

/**
 * The Item Listener removes the corresponding project-workspace mapping
 * when the project is deleted
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
@Extension
public class SharedStorageWorkspaceItemListener extends ItemListener {

  private static Logger LOGGER = Logger.getLogger(SharedStorageWorkspaceItemListener.class.getName());


  public SharedStorageWorkspaceItemListener() {
  }

  @Override
  public void onDeleted(final Item item) {
    SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().removeProjectWorkspace(item);
    LOGGER.info("Removing the project workspace mapping from " + item.getDisplayName());

    super.onDeleted(item);
  }
}
