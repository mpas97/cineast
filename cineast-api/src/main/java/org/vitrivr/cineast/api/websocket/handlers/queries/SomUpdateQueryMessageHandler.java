package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.SomUpdateQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.som.SOM;
import org.vitrivr.cineast.standalone.config.Config;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SomUpdateQueryMessageHandler extends AbstractSomQueryMessageHandler<SomUpdateQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, SomUpdateQuery message) {
        Optional<Retriever> optional = Config.sharedConfig().getRetriever().getRetrieverByName("AverageColor");
        if (optional.isPresent() && optional.get() instanceof AbstractFeatureModule) {
            AbstractFeatureModule retriever = (AbstractFeatureModule) optional.get();
            retriever.init(Config.sharedConfig().getDatabase().getSelectorSupplier());

            final String uuid = qconf.getQueryId().toString();

            List<String> posIds = new ArrayList<>();
            for (String id : message.getPositives()) {
                posIds.addAll(retriever.getSimilar(id, qconf).stream()
                        .map(ScoreElement::getId).collect(Collectors.toList()));
            }
            List<String> negIds = new ArrayList<>();
            for (String id : message.getNegatives()) {
                negIds.addAll(retriever.getSimilar(id, qconf).stream()
                        .map(ScoreElement::getId).collect(Collectors.toList()));;
            }
            posIds = posIds.stream().distinct().collect(Collectors.toList());
            negIds = negIds.stream().distinct().collect(Collectors.toList());
            posIds.removeIf(negIds::contains);

            List<Map<String, PrimitiveTypeProvider>> vectors = retriever.getRows(posIds);
            try {
                SOM som = SOM.getInstance().trainSOM(".som/shuffled-50k/AverageColor.csv",
                        vectors.stream().map(
                                v -> v.get("id").getString()
                        ).collect(Collectors.toCollection(ArrayList::new)),
                        vectors.stream().map(
                                v -> v.get("feature").getFloatArray()
                        ).collect(Collectors.toCollection(ArrayList::new)));
                final List<String> results = Arrays.stream(som.nearestEntryOfNode)
                        .filter(i -> i!=-1)
                        .mapToObj(elem -> som.getIds().get(elem))
                        .collect(Collectors.toList());
                this.submitOverviewInfo(session, uuid, results);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
