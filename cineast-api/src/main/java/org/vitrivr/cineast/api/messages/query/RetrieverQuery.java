package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class RetrieverQuery extends Query {

    @JsonCreator
    public RetrieverQuery() {
        super(null);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.Q_RETRIEVER;
    }
}
