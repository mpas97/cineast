package org.vitrivr.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import org.vitrivr.cineast.core.segmenter.SubdividedFuzzyColorHistogram;

public class SubDivMedianFuzzyColor extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SubDivMedianFuzzyColor(){
		super("features_SubDivMedianFuzzyColor", 2f / 4f);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.traceEntry();
		if (!phandler.idExists(shot.getId())) {
			SubdividedFuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(shot.getMedianImg().getBufferedImage(), 2);
			persist(shot.getId(), fch);
		}
		LOGGER.traceExit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		SubdividedFuzzyColorHistogram query = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(sc.getMedianImg().getBufferedImage(), 2);
		return getSimilar(query.toArray(null), qc);
	}

  @Override
  protected QueryConfig setQueryConfig(ReadableQueryConfig qc) {
    return QueryConfig.clone(qc).setDistanceIfEmpty(Distance.chisquared);
  }

}
