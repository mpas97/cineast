package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;


public class SomTrainQuery extends Query {
    String retriever;
    int size;

    @JsonCreator
    public SomTrainQuery(@JsonProperty(value = "retriever", required = true) String retriever,
                         @JsonProperty(value = "size", required = true) int size) {
        super(null);
        this.retriever = retriever;
        this.size = size;
    }

    public String getRetriever() {
        return retriever;
    }

    public int getSize() {
        return size;
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