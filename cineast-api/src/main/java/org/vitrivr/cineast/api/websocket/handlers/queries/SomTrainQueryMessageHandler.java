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

    public static final int DEFAULT_RANGE = 1000;

    @Override
    public void execute(Session session, QueryConfig qconf, SomTrainQuery message) {
        final String uuid = qconf.getQueryId().toString();
        Optional<Retriever> optSom = Config.sharedConfig().getRetriever().getRetrieverByName(message.getRetrieverSom());
        if (optSom.isPresent() && optSom.get() instanceof AbstractFeatureModule) {
            AbstractFeatureModule retrieverSom = (AbstractFeatureModule) optSom.get();
            retrieverSom.init(Config.sharedConfig().getDatabase().getSelectorSupplier());
            SOM som = new SOM(message.getSize(), message.getSize(), 2);
            if (message.getDeepness() == -1) {
                List<Map<String, PrimitiveTypeProvider>> sampleRows = retrieverSom.getSampleRows(50000, "*");
                System.out.println("sample size: "+sampleRows.size());
                SOM.setResult(uuid, som.trainFromArrayOnly(
                        sampleRows.stream().map(e -> e.get("id").getString()).collect(Collectors.toCollection(ArrayList::new)),
                        sampleRows.stream().map(e -> e.get("feature").getFloatArray()).collect(Collectors.toCollection(ArrayList::new))
                ));
            } else {
                Optional<Retriever> optKnn = Config.sharedConfig().getRetriever().getRetrieverByName(message.getRetrieverKnn());
                if (optKnn.isPresent() && optKnn.get() instanceof AbstractFeatureModule) {
                    AbstractFeatureModule retrieverKnn = (AbstractFeatureModule) optKnn.get();
                    retrieverKnn.init(Config.sharedConfig().getDatabase().getSelectorSupplier());

                    // recreate qconf to omit config constraints
                    qconf = new QueryConfig(qconf);

                    qconf.setResultsPerModule(message.getDeepness() * DEFAULT_RANGE / message.getPositives().size());
                    qconf.setMaxResults(message.getDeepness() * DEFAULT_RANGE / message.getPositives().size());

                    List<Map<String, PrimitiveTypeProvider>> positives = retrieverKnn.getBatchedNearestNeighbourRows(message.getPositives(), qconf, "id", "feature");
                    System.out.println("fetching knn rows with retriever: "+retrieverKnn.getClass().getSimpleName());
                    if (!message.getRetrieverKnn().equals(message.getRetrieverSom())) {
                        positives = retrieverSom.getRows(positives.stream().map(e -> e.get("id").getString()).collect(Collectors.toList()));
                        System.out.println("fetching som rows with retriever: "+retrieverSom.getClass().getSimpleName());
                    }
                    System.out.println("knn positive size: " + positives.size());

                    qconf.setResultsPerModule(DEFAULT_RANGE);
                    qconf.setMaxResults(DEFAULT_RANGE);
                    HashSet<String> neq_lookup = retrieverKnn.getBatchedNearestNeighbourRows(message.getNegatives(), qconf, "id")
                            .stream().map(e -> e.get("id").toString()).collect(Collectors.toCollection(HashSet::new));
                    neq_lookup.addAll(message.getNegatives());
                    System.out.println("knn negative size: " + neq_lookup.size());

                    // split up query result items by id and feature
                    // avoid negative segments by using hashsets
                    ArrayList<String> ids = new ArrayList<>(positives.size());
                    ArrayList<float[]> vectors = new ArrayList<>(positives.size());
                    positives.forEach(e -> {
                        String id = e.get("id").getString();
                        if (!neq_lookup.contains(id)) {
                            ids.add(id);
                            vectors.add(e.get("feature").getFloatArray());
                        }
                    });
                    System.out.println("knn final size: " + ids.size());
                    SOM.setResult(uuid, som.trainFromArrayOnly(ids, vectors));
                }
            }
        }
        this.submitOverviewInfo(session, uuid, new ArrayList<>(SOM.getResult(uuid).keySet()));
    }
}
