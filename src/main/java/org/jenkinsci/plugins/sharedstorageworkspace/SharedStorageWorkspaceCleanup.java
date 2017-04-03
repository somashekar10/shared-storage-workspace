package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * SharedStorageWorkspaceCleanup cleans up the workspace if it's being used for more than 30 days
 * The periodic job runs every hour and cleans up the workspace
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
public class SharedStorageWorkspaceCleanup extends AsyncPeriodicWork {
  private static final Logger LOGGER = Logger.getLogger(SharedStorageWorkspaceCleanup.class.getName());

  protected SharedStorageWorkspaceCleanup() {
    super("Periodic cleanup of shared workspace");
  }

  @Override
  protected void execute(TaskListener taskListener) throws IOException, InterruptedException {
    taskListener.getLogger().println("The Shared Storage Workspace clean up triggered");
    LOGGER.info("The Shared Storage Workspace clean up triggered");

    SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().cleanupNodeWorkspace();
  }

  @Override
  public long getRecurrencePeriod() {
    //The periodic cleanup runs every hour
    return 60 * 60 * 1000L;
  }
}
