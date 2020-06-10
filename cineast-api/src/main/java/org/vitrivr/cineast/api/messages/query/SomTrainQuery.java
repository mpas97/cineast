package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

import java.util.List;


public class SomTrainQuery extends Query {
    int size, deepness;
    String retrieverKnn, retrieverSom;
    List<String> positives, negatives, blacklist;

    @JsonCreator
    public SomTrainQuery(@JsonProperty(value = "size", required = true) int size,
                         @JsonProperty(value = "retriever_knn", required = true) String retriever_knn,
                         @JsonProperty(value = "retriever_som", required = true) String retriever_som,
                         @JsonProperty(value = "deepness", required = true) int deepness,
                         @JsonProperty(value = "positives", required = true) List<String> positives,
                         @JsonProperty(value = "negatives", required = true) List<String> negatives,
                         @JsonProperty(value = "blacklist", required = true) List<String> blacklist,
                         @JsonProperty(value = "config", required = true) QueryConfig config) {
        super(config);
        this.size = size;
        this.retrieverKnn = retriever_knn;
        this.retrieverSom = retriever_som;
        this.deepness = deepness;
        this.positives = positives;
        this.negatives = negatives;
        this.blacklist = blacklist;
    }

    public int getSize() {
        return size;
    }

    public String getRetrieverKnn() {
        return retrieverKnn;
    }

    public String getRetrieverSom() {
        return retrieverSom;
    }

    public int getDeepness() {
        return deepness;
    }

    public List<String> getPositives() {
        return positives;
    }

    public List<String> getNegatives() {
        return negatives;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.Q_SOM_TRAIN;
    }
}
