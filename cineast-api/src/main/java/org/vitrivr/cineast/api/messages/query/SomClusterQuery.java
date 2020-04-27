package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

public class SomClusterQuery extends Query {
    String[] requestedClusterIds;

    @JsonCreator
    public SomClusterQuery(@JsonProperty(value = "cids", required = true) String[] requestedClusterIds,
                           @JsonProperty(value = "config", required = true) QueryConfig config) {
        super(config);
        this.requestedClusterIds = requestedClusterIds;
    }

    public String[] getRequestedClusterIds() {
        return requestedClusterIds;
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.Q_SOM_CLUSTER;
    }
}