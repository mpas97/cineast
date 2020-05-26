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
            if (message.getOffset() == 0) {
                this.submitClusterInfo(session, uuid, SOM.getResult(uuid).get(message.getRequestedClusterIds()[0]));
            } else {
                String[] keys = SOM.getResult(uuid).keySet().toArray(new String[0]);
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i].equals(message.getRequestedClusterIds()[0])) {
                        int desiredIndex = (i+message.getOffset())%keys.length;
                        if (desiredIndex < 0) desiredIndex = keys.length + desiredIndex;
                        this.submitClusterInfo(session, uuid, SOM.getResult(uuid).get(keys[desiredIndex]));
                        break;
                    }
                }
            }
        } else {
            List<String> items = new ArrayList<>();
            for (String cluster : message.getRequestedClusterIds()) {
                items.addAll(SOM.getResult(uuid).get(cluster));
            }
            this.submitClusterInfo(session, uuid, items);
        }
    }
}
