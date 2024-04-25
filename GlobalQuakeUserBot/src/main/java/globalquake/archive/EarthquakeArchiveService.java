package globalquake.archive;

import globalquake.client.GlobalQuakeClient;
import globalquake.core.GlobalQuake;
import globalquake.core.events.GlobalQuakeEventListener;
import globalquake.core.events.specific.*;
import globalquake.db.entities.ArchivedEarthquake;
import globalquake.utils.NamedThreadFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EarthquakeArchiveService {
    private final ScheduledExecutorService loadOldService;

    public EarthquakeArchiveService() {
        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener() {
            @Override
            public void onQuakeArchive(QuakeArchiveEvent event) {
                GlobalQuakeClient.instance.getDatabaseService().insertEarthquake(new ArchivedEarthquake(event.archivedQuake()));
            }
        });

        loadOldService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Load old ArchivedEvents"));
        loadOldService.scheduleAtFixedRate(this::initOld, 15000, 1000, TimeUnit.MILLISECONDS);
    }

    private void initOld() {
        if (GlobalQuake.instance != null && GlobalQuake.instance.getArchive() != null && CollectionUtils.isNotEmpty(GlobalQuake.instance.getArchive().getArchivedQuakes())) {
            GlobalQuake.instance.getArchive().getArchivedQuakes().forEach(archivedQuake -> {
                ArchivedEarthquake archivedEarthquake = GlobalQuakeClient.instance.getDatabaseService().findArchivedEarthquakeById(archivedQuake.getUuid());
                if (archivedEarthquake == null) {
                    GlobalQuakeClient.instance.getDatabaseService().insertEarthquake(new ArchivedEarthquake(archivedQuake));
                }
            });
            loadOldService.shutdown();
        }
    }

    public void destroy() {
        GlobalQuake.instance.stopService(loadOldService);
    }
}
