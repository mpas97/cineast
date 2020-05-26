package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

public class SomClusterQuery extends Query {
    String[] requestedClusterIds;
    int offset;

    @JsonCreator
    public SomClusterQuery(@JsonProperty(value = "cids", required = true) String[] requestedClusterIds,
                           @JsonProperty(value = "offset", required = true) int offset,
                           @JsonProperty(value = "config", required = true) QueryConfig config) {
        super(config);
        this.requestedClusterIds = requestedClusterIds;
        this.offset = offset;
    }

    public String[] getRequestedClusterIds() {
        return requestedClusterIds;
    }

    public int getOffset() {
        return offset;
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