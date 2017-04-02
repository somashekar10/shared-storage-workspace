package org.jenkinsci.plugins.sharedstorageworkspace;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * The Run Listener extension updates the project workspace on completion of build
 * <p>
 * Created by Soma Shekar on 3/28/17.
 *
 * @author somashekar10
 */
@Extension
public class SharedStorageWorkspaceRunListener extends RunListener<Run<?, ?>> {
  final private Logger LOGGER = Logger.getLogger(getClass().getName());

  public SharedStorageWorkspaceRunListener() {
  }

  public SharedStorageWorkspaceRunListener(Class<Run<?, ?>> targetType) {
    super(targetType);
  }

  @Override
  public void onCompleted(Run<?, ?> run, @Nonnull TaskListener listener) {
    if (run instanceof AbstractBuild) {
      FilePath workspace = ((AbstractBuild) run).getWorkspace();

      LOGGER.info("Updating the project workspace for - " + ((AbstractBuild) run).getProject()
          + " to " + workspace.getRemote() + " on build completion");

      SharedStorageWorkspaceManager.getSharedStorageWorkspaceManager().updateProjectWorkspace(((AbstractBuild) run).getProject(), workspace.getRemote());
    }
  }
}
