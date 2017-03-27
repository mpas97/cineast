package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorLayoutDescriptor;
import org.vitrivr.cineast.core.util.ColorReductionUtil;

public class CLDReduced15 extends AbstractFeatureModule {

	public CLDReduced15(){
		super("features_CLDReduced15", 1960f / 4f);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			FloatVector fv = ColorLayoutDescriptor.calculateCLD(ColorReductionUtil.quantize15(shot.getMostRepresentativeFrame().getImage()));
			persist(shot.getId(), fv);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(ColorReductionUtil.quantize15(sc.getMostRepresentativeFrame().getImage()));
		return getSimilar(query.toArray(null), qc);
	}


}
