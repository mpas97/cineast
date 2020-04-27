package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

import java.util.List;

/**
 * @author rgasser
 * @created 12.01.17
 */
public class MediaSegmentQueryResult extends AbstractQueryResultMessage<MediaSegmentDescriptor> {

  public enum SEGMENT_TYPE { DEFAULT, SOM_OVERVIEW, SOM_CLUSTER }

  @JsonProperty
  private final SEGMENT_TYPE type;

  /**
   * @param content
   */
  @JsonCreator
  public MediaSegmentQueryResult(String queryId, List<MediaSegmentDescriptor> content) {
    super(queryId, MediaSegmentDescriptor.class, content);
    type = SEGMENT_TYPE.DEFAULT;
  }

  @JsonCreator
  public MediaSegmentQueryResult(String queryId, List<MediaSegmentDescriptor> content, SEGMENT_TYPE type) {
    super(queryId, MediaSegmentDescriptor.class, content);
    this.type = type;
  }
  
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_SEGMENT;
  }
  
  
}
