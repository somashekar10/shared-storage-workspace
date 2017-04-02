package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Job;
import hudson.model.WorkspaceBrowser;

import java.io.File;
import java.util.logging.Logger;

/**
 * WorkspaceBrowser provides mechanism for the project workspace to be browsable
 * by user even when the node goes offline
 * <p>
 * Allows access to the Jenkins slave's workspace when the Jenkins slave is offline.
 * The assumption is the Jenkins slave's root path (configurable via "Remote FS Root"
 * option) points to a shared storage location (e.g., NFS mount) accessible to the Jenkins master.
 * Note that when the Jenkins slave is online, the workspace is directly provided
 * by the slave and the control doesn't reach here.
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
@Extension
public class SharedStorageWorkspaceBrowser extends WorkspaceBrowser {

  private static final Logger LOGGER = Logger
      .getLogger(SharedStorageWorkspaceBrowser.class.getName());

  @Override
  public FilePath getWorkspace(Job job) {
    String workspace = SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().getProjectWorkspace(job);
    if (workspace == null) {
      return null;
    }

    LOGGER.info("The returned workspace for the job - " + job.getDisplayName() + " is " + workspace);

    return new FilePath(new File(workspace));
  }
}
