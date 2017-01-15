package reciter.database.mongo.repository.impl;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.repository.ESearchResultRepositoryCustom;

public class ESearchResultRepositoryImpl implements ESearchResultRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public boolean pushESearchResult(ESearchResult eSearchResult) {
		return false;
		//		String uid = eSearchResult.getCwid();
		//		ESearchPmid eSearchPmid = eSearchResult.getEsearchResult();
		//		// Insert each ESearchPmid object into the collection.
		//		BasicDBObject basicDbObject = new BasicDBObject("$addToSet", new BasicDBObject("eSearchPmid", new BasicDBObject("$each", eSearchPmid)));
		//		WriteResult writeResult = mongoTemplate.getCollection("esearchresult").update(new BasicDBObject("uid", uid), basicDbObject, true, false);
		//		return writeResult.wasAcknowledged();
	}

	@Override
	public WriteResult update(String uid, ESearchPmid eSearchPmid) {
		
		// use java.util.Date until MongoDB driver supports LocalDateTime codec
		Instant instant = eSearchPmid.getRetrievalDate().atZone(ZoneOffset.UTC).toInstant();
		Date date = Date.from(instant);
		
		BasicDBObject query = new BasicDBObject("uid", uid).append("eSearchPmid.retrievalStrategyName", eSearchPmid.getRetrievalStrategyName());
		BasicDBObject update = new BasicDBObject("$set", 
				new BasicDBObject("eSearchPmid.pmids", eSearchPmid.getPmids())
				.append("eSearchPmid.retrievalDate", date));
		
		WriteResult writeResult = mongoTemplate.getCollection("esearchresult").update(query, update, true, false);
		return writeResult;
	}
	
	@Override
	public boolean existByUidAndRetrievalStrategyName(String uid, String retrievalStrategyName) {
		BasicDBObject query = new BasicDBObject("uid", uid).append("eSearchPmid.retrievalStrategyName", retrievalStrategyName);
		return mongoTemplate.getCollection("esearchresult").findOne(query) != null;
	}
}
