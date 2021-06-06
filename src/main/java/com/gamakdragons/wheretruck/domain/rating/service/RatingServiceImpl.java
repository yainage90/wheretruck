package com.gamakdragons.wheretruck.domain.rating.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.util.EsRequestFactory;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RatingServiceImpl implements RatingService {
    
    @Value("${elasticsearch.index.truck.name}")
    private String TRUCK_INDEX;

    private final ElasticSearchServiceImpl restClient;

    @Autowired
    public RatingServiceImpl (ElasticSearchServiceImpl restClient) {
        this.restClient = restClient;
    }

    @Override
    public UpdateResultDto saveRating(String truckId, Rating rating) {

        rating.setId(UUID.randomUUID().toString());
        String current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        rating.setCreatedDate(current);
        rating.setUpdatedDate(current);

        Map<String, Object> params = new HashMap<>();
        params.put("rating", rating);

        String script = "if(ctx._source.ratings == null) {ctx._source.ratings = new ArrayList();}" + 
                        "ctx._source.ratings.add(params.rating);" + 
                        "ctx._source.numRating=ctx._source.ratings.stream().count();" + 
                        "ctx._source.starAvg=ctx._source.ratings.stream().mapToDouble(r -> r.star).average().getAsDouble();";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, rating.getTruckId(), inline);

        UpdateResponse response;
        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return UpdateResultDto.builder()
                .result(e.getLocalizedMessage())
                .build();

        }

        return UpdateResultDto.builder()
                .result(response.getResult().name())
                .id(rating.getId())
                .build();
    }

    @Override
    public UpdateResultDto updateRating(String truckId, Rating rating) {

        rating.setUpdatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Map<String, Object> params = new HashMap<>();
        params.put("rating", rating);

        String script = "def target = ctx._source.ratings.find(rating -> rating.id == params.rating.id);" +
                        "target.userId= params.rating.userId;" + 
                        "target.truckId = params.rating.truckId;" +
                        "target.star = params.rating.star;" +
                        "target.comment = params.rating.comment" +
                        "ctx._source.numRating=ctx._source.ratings.stream().count();" + 
                        "ctx._source.starAvg=ctx._source.ratings.stream().mapToDouble(r -> r.star).average().getAsDouble();";


        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, rating.getTruckId(), inline);

        UpdateResponse response;
        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return UpdateResultDto.builder()
                .result(e.getLocalizedMessage())
                .build();
        }

        return UpdateResultDto.builder()
                .result(response.getResult().name())
                .id(rating.getId())
                .build();

    }

    @Override
    public UpdateResultDto deleteRating(String truckId, String id) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        String script = "ctx._source.ratings.removeIf(rating -> rating.id == params.id);";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, truckId, inline);
        UpdateResponse response;

        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return UpdateResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        }

        return UpdateResultDto.builder()
                .result(response.getResult().name())
                .build();
    }

    public SearchResultDto<Rating> findByUserId(String userId) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("ratings.userId", userId));

        InnerHitBuilder innerHitBuilder = new InnerHitBuilder();

        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("ratings", boolQueryBuilder, ScoreMode.None);
        nestedQueryBuilder.innerHit(innerHitBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(nestedQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        SearchRequest request = new SearchRequest(TRUCK_INDEX);
        request.source(searchSourceBuilder);
    }
}
