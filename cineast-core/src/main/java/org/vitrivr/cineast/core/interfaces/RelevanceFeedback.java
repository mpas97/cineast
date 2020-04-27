package org.vitrivr.cineast.core.interfaces;

import java.util.Set;

public interface RelevanceFeedback<T> {
    T update(Set<String> positiveObjectIds, Set<String> negativeObjectIds);
}
