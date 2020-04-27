package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;


public class SomTrainQuery extends Query {
    int size;

    @JsonCreator
    public SomTrainQuery(@JsonProperty(value = "size", required = true) int size) {
        super(null);
        this.size = size;
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