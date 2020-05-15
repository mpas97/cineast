package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;


public class SomUpdateQuery extends Query {
    int size;
    String retriever;
    String[] positives, negatives;

    @JsonCreator
    public SomUpdateQuery(@JsonProperty(value = "size", required = true) int size,
                          @JsonProperty(value = "retriever", required = true) String retriever,
                          @JsonProperty(value = "positives", required = true) String[] positives,
                          @JsonProperty(value = "negatives", required = true) String[] negatives,
                          @JsonProperty(value = "config", required = true) QueryConfig config) {
        super(config);
        this.size = size;
        this.retriever = retriever;
        this.positives = positives;
        this.negatives = negatives;
    }

    public int getSize() {
        return size;
    }

    public String getRetriever() {
        return retriever;
    }

    public String[] getPositives() {
        return positives;
    }

    public String[] getNegatives() {
        return negatives;
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.Q_SOM_UPDATE;
    }
}
