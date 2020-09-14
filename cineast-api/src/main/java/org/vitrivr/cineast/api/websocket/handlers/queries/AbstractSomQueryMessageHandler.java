package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.Query;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult.SEGMENT_TYPE;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResult;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mpas97
 * @version 1.0
 * @created 26.04.20
 */
public abstract class AbstractSomQueryMessageHandler<T extends Query> extends AbstractQueryMessageHandler<T> {

    protected void submitOverviewInfo(Session session, String queryId, List<String> segmentIds) {
        /* Load segment & object information. */
        final List<MediaSegmentDescriptor> segments = this.loadSegments(segmentIds);
        final List<String> objectIds = segments.stream()
                .map(MediaSegmentDescriptor::getObjectId)
                .collect(Collectors.toList());
        final List<MediaObjectDescriptor> objects = this.loadObjects(objectIds);

        List<StringDoublePair> results = segmentIds.stream()
                .map(sig -> new StringDoublePair(sig, 1d))
                .collect(Collectors.toList());

        /* Write segments, objects and som data to stream. */
        this.write(session, new MediaObjectQueryResult(queryId, objects));
        this.write(session, new MediaSegmentQueryResult(queryId, segments, SEGMENT_TYPE.SOM_OVERVIEW));
        this.write(session, new SimilarityQueryResult(queryId, "som", 0, results));

        /* Load and transmit segment & object metadata. */
        this.loadAndWriteSegmentMetadata(session, queryId, segmentIds, new HashSet<>());
        this.loadAndWriteObjectMetadata(session, queryId, objectIds, new HashSet<>());
    }

    protected void submitClusterInfo(Session session, String queryId, List<String> raw) {
        final int stride = 1000;
        for (int i = 0; i < Math.floorDiv(raw.size(), stride) + 1; i++) {
            final List<String> segmentIds = raw.subList(i * stride, Math.min((i + 1) * stride, raw.size()));

            /* Load segment & object information. */
            final List<MediaSegmentDescriptor> segments = this.loadSegments(segmentIds);
            final List<String> objectIds = segments.stream()
                    .map(MediaSegmentDescriptor::getObjectId)
                    .collect(Collectors.toList());
            final List<MediaObjectDescriptor> objects = this.loadObjects(objectIds);
            if (segments.isEmpty() || objects.isEmpty()) {
                continue;
            }

            /* Write segments, objects and cluster data to stream. */
            this.write(session, new MediaObjectQueryResult(queryId, objects));
            this.write(session, new MediaSegmentQueryResult(queryId, segments, SEGMENT_TYPE.SOM_CLUSTER));

            /* Load and transmit segment & object metadata. */
            this.loadAndWriteSegmentMetadata(session, queryId, segmentIds, new HashSet<>());
            this.loadAndWriteObjectMetadata(session, queryId, objectIds, new HashSet<>());
        }
    }
}
