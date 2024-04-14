package globalquake.archive;

import globalquake.client.GlobalQuakeClient;
import globalquake.core.GlobalQuake;
import globalquake.core.events.GlobalQuakeEventListener;
import globalquake.core.events.specific.*;
import globalquake.db.entities.ArchivedEarthquake;

public class EarthquakeArchiveService {

    public EarthquakeArchiveService() {
        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener() {
            @Override
            public void onQuakeArchive(QuakeArchiveEvent event) {
                GlobalQuakeClient.instance.getDatabaseService().insertEarthquake(new ArchivedEarthquake(event.archivedQuake()));
            }
        });
    }
}
