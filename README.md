Shared Storage Workspace Jenkins Plugin
======
This plugin provides the mechanism to use "Shared Storage" as workspace for builds.

This plugin ensures the builds don't run into each other even when there are parallel builds being run for the same job.

This plugin ensures the project workspace is available for the users to browse even when the slave nodes are disconnected.

Assumptions
============
+ The "Shared Storage" is mounted at all the slaves and the master at the same corresponding mount points; and the workspace root is appropriately configured.

Compilation
============
Like any standard jenkins plugin, call maven goals "clean install". This will create shared-storage-workspace.hpi in target directory

Installation
============
To install this plugin :
+ Simply drop the created .hpi file into any running Jenkins.
+ Restart Jenkins
