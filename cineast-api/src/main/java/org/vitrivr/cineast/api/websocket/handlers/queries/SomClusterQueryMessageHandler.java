package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.SomClusterQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.som.SOM;

import java.util.*;
import java.util.stream.Collectors;

public class SomClusterQueryMessageHandler extends AbstractSomQueryMessageHandler<SomClusterQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, SomClusterQuery message) {
        SOM som = SOM.getInstance();
        ArrayList<String> cluster = new ArrayList<>();
        List<Integer> ids = Arrays.stream(message.getRequestedClusterIds())
                .map(cid -> som.getNodes()[som.getIds().indexOf(cid)])
                .collect(Collectors.toList());
        for (int i = 0; i < som.getNodes().length; i++) {
            if (ids.contains(som.getNodes()[i])) {
                cluster.add(som.getIds().get(i));
            }
        }
        this.submitClusterInfo(session, qconf.getQueryId().toString(), cluster);
    }
}
