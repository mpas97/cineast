package ch.unibas.cs.dbis.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;

import ch.unibas.cs.dbis.cineast.core.data.Shot;
import ch.unibas.cs.dbis.cineast.core.setup.EntityCreator;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.QueryResultMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage;
import ch.unibas.dmi.dbis.adam.http.Grpc.BooleanQueryMessage.WhereMessage;

public class ShotLookup {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private ADAMproWrapper adampro = new ADAMproWrapper();
	
	public void close(){
		this.adampro.close();
	}
	
	public ShotDescriptor lookUpShot(String shotId){
		LOGGER.entry();
		long start = System.currentTimeMillis();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("id").setValue(shotId).build();
		//TODO check type as well
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(EntityCreator.CINEAST_SEGMENT)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = adampro.booleanQuery(qbqm);
		QueryResponseInfoMessage responce;

		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ShotDescriptor("", "", 0, 0);
		}

		List<QueryResultMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return new ShotDescriptor("", "", 0, 0);
		}
		
		QueryResultMessage result = results.get(0);
		
		Map<String, String> map = result.getMetadata();
		
		ShotDescriptor _return = new ShotDescriptor(map.get("multimediaobject"), map.get("id"), Integer.parseInt(map.get("segmentstart")), Integer.parseInt(map.get("segmentend")));
		
		LOGGER.debug("lookUpShot done in {}ms", System.currentTimeMillis() - start);
		return LOGGER.exit(_return);
		
	}
	
	public Map<String, ShotDescriptor> lookUpShots(String...ids){
		LOGGER.entry();
		
		if(ids == null || ids.length == 0){
			return new HashMap<>();
		}
		
		long start = System.currentTimeMillis();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		StringBuilder builder = new StringBuilder("IN(");
		for(int i = 0; i < ids.length - 1; ++i){
			builder.append("'");
			builder.append(ids[i]);
			builder.append("', ");
		}
		builder.append("'");
		builder.append(ids[ids.length - 1]);
		builder.append("')");
		WhereMessage where = WhereMessage.newBuilder().setField("id").setValue(builder.toString()).build();
		//TODO check type as well
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(EntityCreator.CINEAST_SEGMENT)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = adampro.booleanQuery(qbqm);
		QueryResponseInfoMessage responce;
		
		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<>();
		}

		List<QueryResultMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return new HashMap<>();
		}
		
		HashMap<String, ShotDescriptor> _return = new HashMap<>();
		for(QueryResultMessage result : results){
			Map<String, String> map = result.getMetadata();
			_return.put(map.get("id"), new ShotDescriptor(map.get("multimediaobject"), map.get("id"), Integer.parseInt(map.get("segmentstart")), Integer.parseInt(map.get("segmentend"))));
		}
		LOGGER.debug("lookUpShot done in {}ms", System.currentTimeMillis() - start);
		return LOGGER.exit(_return);
	}
	
	
	public String lookUpVideoid(String name){ //TODO move to VideoLookup
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("name").setValue(name).build();
		//TODO check type as well
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(EntityCreator.CINEAST_MULTIMEDIAOBJECT)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = adampro.booleanQuery(qbqm);
		QueryResponseInfoMessage responce;
		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

		List<QueryResultMessage> results = responce.getResultsList();
		
		if(results.isEmpty()){//no such video
			return "";
		}
		
		QueryResultMessage result = results.get(0);
		
		String id = result.getMetadata().get("id");
		
		return id;

	}
	
	public List<ShotDescriptor> lookUpVideo(String videoId){
		LinkedList<ShotDescriptor> _return = new LinkedList<ShotLookup.ShotDescriptor>();
		ArrayList<WhereMessage> tmp = new ArrayList<>(1);
		WhereMessage where = WhereMessage.newBuilder().setField("multimediaobject").setValue(videoId).build();
		//TODO check type as well
		tmp.add(where);
		SimpleBooleanQueryMessage qbqm = SimpleBooleanQueryMessage.newBuilder().setEntity(EntityCreator.CINEAST_SEGMENT)
				.setBq(BooleanQueryMessage.newBuilder().addAllWhere(tmp)).build();
		ListenableFuture<QueryResponseInfoMessage> f = adampro.booleanQuery(qbqm);
		QueryResponseInfoMessage responce;
		try {
			responce = f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return _return;
		}
		
		List<QueryResultMessage> results = responce.getResultsList();
		
		for(QueryResultMessage result : results){
			Map<String, String> metadata = result.getMetadata();
			_return.add(new ShotDescriptor(
					videoId, 
					metadata.get("id"), 
					Integer.parseInt(metadata.get("segmentstart")), 
					Integer.parseInt(metadata.get("segmentend"))));
		}
		

		
		return _return;
	}
	
//	@Override
//	protected void finalize() throws Throwable {
//		
//		super.finalize();
//	}

	public static class ShotDescriptor{
		
		private final String shotId, videoId;
		private final int startFrame, endFrame;
		
		
		public ShotDescriptor(String videoId, int shotNumber, int startFrame, int endFrame) {
			this(videoId, Shot.generateShotID(videoId, shotNumber), startFrame, endFrame);
		}
		
		ShotDescriptor(String videoId, String shotId,  int startFrame, int endFrame){
			this.videoId = videoId;
			this.shotId = shotId;
			this.startFrame = startFrame;
			this.endFrame = endFrame;
		}

		public String getShotId() {
			return shotId;
		}

		public String getVideoId() {
			return videoId;
		}



		public int getFramecount() {
			return endFrame - startFrame + 1;
		}

		public int getStartFrame() {
			return startFrame;
		}

		public int getEndFrame() {
			return endFrame;
		}

		@Override
		public String toString() {
			return "ShotDescriptor(" + shotId + ")";
		}

	}
	
}
