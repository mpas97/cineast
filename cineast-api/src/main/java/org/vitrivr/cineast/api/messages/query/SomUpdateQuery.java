package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;


public class SomUpdateQuery extends Query {
    String[] positives, negatives;

    @JsonCreator
    public SomUpdateQuery(@JsonProperty(value = "positives", required = true) String[] positives,
                          @JsonProperty(value = "negatives", required = true) String[] negatives,
                          @JsonProperty(value = "config", required = true) QueryConfig config) {
        super(config);
        this.positives = positives;
        this.negatives = negatives;
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
