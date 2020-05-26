package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.SomTrainQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.som.SOM;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.*;
import java.util.stream.Collectors;

public class SomTrainQueryMessageHandler extends AbstractSomQueryMessageHandler<SomTrainQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, SomTrainQuery message) {
        final String uuid = qconf.getQueryId().toString();
        Optional<Retriever> optional = Config.sharedConfig().getRetriever().getRetrieverByName(message.getRetriever());
        if (optional.isPresent() && optional.get() instanceof AbstractFeatureModule) {
            AbstractFeatureModule retriever = (AbstractFeatureModule) optional.get();
            retriever.init(Config.sharedConfig().getDatabase().getSelectorSupplier());
            SOM som = new SOM(message.getSize(), message.getSize(), 2);
            if (message.getDeepness() == -1) {
                List<Map<String, PrimitiveTypeProvider>> sampleRows = retriever.getSampleRows(50000, "*");
                SOM.setResult(uuid, som.trainFromArrayOnly(
                        sampleRows.stream().map(e -> e.get("id").getString()).collect(Collectors.toCollection(ArrayList::new)),
                        sampleRows.stream().map(e -> e.get("feature").getFloatArray()).collect(Collectors.toCollection(ArrayList::new))
                ));
            } else {
                qconf = new QueryConfig(qconf);
                qconf.setResultsPerModule(message.getDeepness() * 1000);
                qconf.setMaxResults(message.getDeepness() * 1000);
                List<Map<String, PrimitiveTypeProvider>> positives = retriever.getBatchedNearestNeighbourRows(message.getPositives(), qconf);
                System.out.println("deepness: " + message.getDeepness() + " size positive knn: " + positives.size());

                qconf.setResultsPerModule(1000 * message.getNegatives().size());
                qconf.setMaxResults(1000 * message.getNegatives().size());
                HashSet<String> neq_lookup = retriever.getBatchedNearestNeighbourRows(message.getNegatives(), qconf, "id")
                        .stream().map(e -> e.get("id").toString()).collect(Collectors.toCollection(HashSet::new));
                neq_lookup.addAll(message.getNegatives());
                System.out.println("size negative knn: " + neq_lookup.size());

                // split up query result items by id and feature
                // avoid duplicate (batched knn can return same neighbors arbitrary often) or negative segments by using hashsets
                ArrayList<String> ids = new ArrayList<>(positives.size());
                ArrayList<float[]> vectors = new ArrayList<>(positives.size());
                HashSet<String> ids_lookup = new HashSet<>(positives.size());
                positives.forEach(e -> {
                    String id = e.get("id").getString();
                    if (ids_lookup.add(id) && !neq_lookup.contains(id)) {
                        ids.add(id);
                        vectors.add(e.get("feature").getFloatArray());
                    }
                });
                System.out.println("knn final size: " + ids.size());
                SOM.setResult(uuid, som.trainFromArrayOnly(ids, vectors));
            }
        }
        this.submitOverviewInfo(session, uuid, new ArrayList<>(SOM.getResult(uuid).keySet()));
    }
}
