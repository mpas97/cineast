package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.SomTrainQuery;
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

public class SomTrainQueryMessageHandler extends AbstractSomQueryMessageHandler<SomTrainQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, SomTrainQuery message) {
        final String uuid = qconf.getQueryId().toString();
        SOM som = new SOM(message.getSize(), message.getSize(), 1);
        if (message.getDeepness() == -1) {
            SOM.setResult(uuid, som.trainSOM(".som/shuffled-50k/"+message.getRetriever()+".csv"));
        } else {
            Optional<Retriever> optional = Config.sharedConfig().getRetriever().getRetrieverByName(message.getRetriever());
            if (optional.isPresent() && optional.get() instanceof AbstractFeatureModule) {
                AbstractFeatureModule retriever = (AbstractFeatureModule) optional.get();
                retriever.init(Config.sharedConfig().getDatabase().getSelectorSupplier());
                System.out.println("update using retriever: " + retriever.getClass().getSimpleName());

                qconf.setResultsPerModule(message.getDeepness() * 1000);
                qconf.setMaxResults(message.getDeepness() * 1000);
                List<String> posIds = retriever.getSimilar(Arrays.asList(message.getPositives()), qconf).stream()
                        .map(ScoreElement::getId).collect(Collectors.toList());
                System.out.println("deepness: " + message.getDeepness() + " size batched positive knn: " + posIds.size());

                qconf.setResultsPerModule(1000 * message.getNegatives().length);
                List<String> negIds = retriever.getSimilar(Arrays.asList(message.getNegatives()), qconf).stream()
                        .map(ScoreElement::getId).collect(Collectors.toList());
                System.out.println("size batched negative knn: " + negIds.size());

                posIds.removeAll(negIds);

                System.out.println("knn final size: " + posIds.size());

                List<Map<String, PrimitiveTypeProvider>> vectors = retriever.getRows(posIds);
                SOM.setResult(uuid, som.trainFromArrayOnly(
                    vectors.stream().map(
                            v -> v.get("id").getString()
                    ).collect(Collectors.toCollection(ArrayList::new)),
                    vectors.stream().map(
                            v -> v.get("feature").getFloatArray()
                    ).collect(Collectors.toCollection(ArrayList::new))
                ));
            }
        }
        this.submitOverviewInfo(session, uuid, new ArrayList<>(SOM.getResult(uuid).keySet()));
    }
}
