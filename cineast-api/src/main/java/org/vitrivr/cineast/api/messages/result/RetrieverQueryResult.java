package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import java.util.List;


public class RetrieverQueryResult extends AbstractQueryResultMessage<String> {

    @JsonCreator
    public RetrieverQueryResult(String queryId, List<String> content) {
        super(queryId, String.class, content);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.QR_RETRIEVER;
    }
}