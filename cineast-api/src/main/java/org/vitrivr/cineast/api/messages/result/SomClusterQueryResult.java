package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class SomClusterQueryResult implements Message {

    public String queryId;

    @JsonCreator
    public SomClusterQueryResult(String queryId) {
        this.queryId = queryId;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.QR_SOM_CLUSTER;
    }
}
