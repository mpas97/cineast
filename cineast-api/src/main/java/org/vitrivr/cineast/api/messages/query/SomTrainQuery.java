package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

import java.util.List;


public class SomTrainQuery extends Query {
    int size, deepness;
    String retriever;
    List<String> positives, negatives;

    @JsonCreator
    public SomTrainQuery(@JsonProperty(value = "size", required = true) int size,
                         @JsonProperty(value = "retriever", required = true) String retriever,
                         @JsonProperty(value = "deepness", required = true) int deepness,
                         @JsonProperty(value = "positives", required = true) List<String> positives,
                         @JsonProperty(value = "negatives", required = true) List<String> negatives,
                         @JsonProperty(value = "config", required = true) QueryConfig config) {
        super(config);
        this.size = size;
        this.retriever = retriever;
        this.deepness = deepness;
        this.positives = positives;
        this.negatives = negatives;
    }

    public int getSize() {
        return size;
    }

    public String getRetriever() {
        return retriever;
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
