/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.base.ReindexAIPPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.roda.core.util.RESTClientUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationPlugin.class);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "AIP replication plugin";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Replicates AIPs on a different RODA of a different machine.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      List<PreservationMetadata> pms = new ArrayList<PreservationMetadata>();
      PluginState state = PluginState.SUCCESS;
      String commandResult = null;
      List<String> aipIds = new ArrayList<String>();

      try {
        commandResult = ReplicationPluginUtils.executeRsyncAIP(list);
      } catch (UnsupportedOperationException | CommandException | IOException e) {
        LOGGER.error("Could not run rsync command on AIPs: ", e);
        state = PluginState.FAILURE;
      }

      try {
        for (AIP aip : list) {
          pms.add(PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, commandResult, true));
          aipIds.add(aip.getId());
        }
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event", e);
        state = PluginState.FAILURE;
      }

      if (state.equals(PluginState.SUCCESS)) {
        try {
          ReplicationPluginUtils.executeRsyncEvents(pms);
        } catch (UnsupportedOperationException | CommandException | IOException e) {
          LOGGER.error("Could not run rsync command on AIP events: ", e);
        }
      }

      if (state.equals(PluginState.SUCCESS)) {
        sendReindexRequest(aipIds);
      }

      jobPluginInfo.incrementObjectsProcessed(state);
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return PluginHelper.initPluginReport(this);
  }

  private void sendReindexRequest(List<String> aipIds) {
    String targetApi = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_api");
    String targetResource = RodaCoreFactory.getRodaConfigurationAsString("core", "aip_rsync", "target_job_resource");
    String username = RodaCoreFactory.getRodaConfigurationAsString("ldap", "adminUsername");
    String password = RodaCoreFactory.getRodaConfigurationAsString("ldap", "adminPassword");

    Job job = new Job();
    job.setName(getClass().getSimpleName() + " " + job.getStartDate());
    job.setSourceObjects(new SelectedItemsList<>(aipIds, AIP.class.getCanonicalName()));
    job.setPlugin(ReindexAIPPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.MISC);

    try {
      RESTClientUtility.sendPostRequest(job, Job.class, targetApi, targetResource, username, password);
    } catch (RODAException e) {
      LOGGER.error("Error send post request to reindex AIPs");
    }
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ReplicationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.REPLICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REPLICATION);
  }

}
