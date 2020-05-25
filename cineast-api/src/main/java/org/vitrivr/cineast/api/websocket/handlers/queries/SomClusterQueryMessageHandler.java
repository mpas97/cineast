package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.SomClusterQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.som.SOM;

import java.util.*;

public class SomClusterQueryMessageHandler extends AbstractSomQueryMessageHandler<SomClusterQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, SomClusterQuery message) {
        final String uuid = qconf.getQueryId().toString();
        if (message.getRequestedClusterIds().length == 1) {
            this.submitClusterInfo(session, uuid, SOM.getResult(uuid).get(message.getRequestedClusterIds()[0]));
        } else {
            List<String> items = new ArrayList<>();
            for (String cluster : message.getRequestedClusterIds()) {
                items.addAll(SOM.getResult(uuid).get(cluster));
            }
            this.submitClusterInfo(session, uuid, items);
        }
    }
}
