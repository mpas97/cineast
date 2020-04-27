package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.SomTrainQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.som.SOM;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SomTrainQueryMessageHandler extends AbstractSomQueryMessageHandler<SomTrainQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, SomTrainQuery message) {
        final String uuid = qconf.getQueryId().toString();
        try {
            SOM som = SOM
                    .setInstance(message.getSize(), message.getSize(), 1)
                    .trainSOM(".som/shuffled-50k/AverageColor.csv");
            final List<String> results = Arrays.stream(som.nearestEntryOfNode)
                    .mapToObj(elem -> som.getIds().get(elem))
                    .collect(Collectors.toList());
            this.submitOverviewInfo(session, uuid, results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
