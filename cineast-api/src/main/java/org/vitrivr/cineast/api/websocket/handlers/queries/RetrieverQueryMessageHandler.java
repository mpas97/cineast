package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.RetrieverQuery;
import org.vitrivr.cineast.api.messages.result.RetrieverQueryResult;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.standalone.config.Config;

public class RetrieverQueryMessageHandler extends AbstractQueryMessageHandler<RetrieverQuery> {

    @Override
    public void execute(Session session, QueryConfig qconf, RetrieverQuery message) {
        final String uuid = qconf.getQueryId().toString();
        this.write(session, new RetrieverQueryResult(uuid,
                Config.sharedConfig().getRetriever().getRetrieverNamesByAbstractFeatureModule()
        ));
    }
}