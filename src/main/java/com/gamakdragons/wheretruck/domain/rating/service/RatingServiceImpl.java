package com.gamakdragons.wheretruck.domain.rating.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    
    @Value("${elasticsearch.index.truck.name}")
    private String TRUCK_INDEX;

    private final RestHighLevelClient esClient;

    @Override
    public UpdateResultDto saveRating(String truckId, Rating rating) {


        String current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        rating.setUpdatedDate(current);

        String script;

        if(rating.getId() == null) {
            rating.setId(UUID.randomUUID().toString());
            rating.setCreatedDate(current);

            script = "if(ctx._source.ratings == null) {ctx._source.ratings = new ArrayList();}" + 
                        "ctx._source.ratings.add(params.rating);" + 
                        "ctx._source.numRating=ctx._source.ratings.stream().count();" + 
                        "ctx._source.starAvg=ctx._source.ratings.stream().mapToDouble(r -> r.star).average().getAsDouble();";
        } else {
            script = "def target = ctx._source.ratings.find(rating -> rating.id == params.rating.id);" +
                        "target.star = params.rating.star;" +
                        "target.comment = params.rating.comment;" +
                        "target.updatedDate = params.rating.updatedDate;" +
                        "ctx._source.numRating=ctx._source.ratings.stream().count();" + 
                        "ctx._source.starAvg=ctx._source.ratings.stream().mapToDouble(r -> r.star).average().getAsDouble();";
        }

        Map<String, Object> params = new HashMap<>();
        params.put("rating", rating.toMap());

        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, truckId, inline);

        UpdateResponse response;
        try {
            response = esClient.update(request, RequestOptions.DEFAULT);
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

        String script = "ctx._source.ratings.removeIf(rating -> rating.id == params.id);" +
                        "ctx._source.numRating=ctx._source.ratings.stream().count();" + 
                        "ctx._source.starAvg=ctx._source.ratings.stream().mapToDouble(r -> r.star).average().getAsDouble();";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, truckId, inline);
        UpdateResponse response;

        try {
            response = esClient.update(request, RequestOptions.DEFAULT);
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

    public SearchResultDto<MyRatingDto> findByUserId(String userId) {
        SearchRequest request = EsRequestFactory.createNestedSearchRequest(TRUCK_INDEX, "ratings", "ratings.userId", userId);

        SearchResponse response;
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error(e.getMessage());
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        final List<MyRatingDto> myRatings = new ArrayList<>();
        Arrays.stream(response.getHits().getHits()).forEach(truckHit -> {
            Arrays.stream(truckHit.getInnerHits().get("ratings").getHits())
                    .map(ratingHit -> new Gson().fromJson(ratingHit.getSourceAsString(), Rating.class))
                    .map(rating -> MyRatingDto.builder()
                                        .id(rating.getId())
                                        .star(rating.getStar())
                                        .userId(rating.getUserId())
                                        .comment(rating.getComment())
                                        .createdDate(rating.getCreatedDate())
                                        .updatedDate(rating.getUpdatedDate())
                                        .truckId(truckHit.getSourceAsMap().get("id").toString())
                                        .truckName(truckHit.getSourceAsMap().get("name").toString())
                                        .build()
                    )
                    .forEach(myRatings::add);
        });

        List<MyRatingDto> result = myRatings.stream()
                .sorted((r1, r2) -> {
                    return r2.getCreatedDate().compareTo(r1.getCreatedDate());
                })
                .collect(Collectors.toList());

        return SearchResultDto.<MyRatingDto> builder()
                .status(response.status().name())
                .numFound(result.size())
                .docs(result)
                .build();
    }

    private SearchResultDto<MyRatingDto> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<MyRatingDto> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }
}
