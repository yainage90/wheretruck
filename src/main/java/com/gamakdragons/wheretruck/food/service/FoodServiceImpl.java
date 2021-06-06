package com.gamakdragons.wheretruck.food.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gamakdragons.wheretruck.aws.S3Service;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.food.dto.FoodUpdateRequestDto;
import com.gamakdragons.wheretruck.food.entity.Food;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FoodServiceImpl implements FoodService {

    @Value("${es.index.truck.name}")
    private String TRUCK_INDEX;

    private final ElasticSearchServiceImpl restClient;
    private final S3Service s3Service;

    @Autowired
    public FoodServiceImpl(ElasticSearchServiceImpl restClient, S3Service s3Service) {
        this.restClient = restClient;
        this.s3Service = s3Service;
    }

    @Override
    public UpdateResultDto saveFood(String truckId, FoodSaveRequestDto foodSaveRequestDto) {

        Food food = foodSaveRequestDto.toEntity();
        String foodImageUrl = s3Service.uploadFoodImage(truckId, food.getId(), foodSaveRequestDto.getImage());
        food.setImageUrl(foodImageUrl);
        log.info("food: " + food);

        Map<String, Object> params = new HashMap<>();
        params.put("food", food.toMap());

        String script = "if(ctx._source.foods == null) {ctx._source.foods = new ArrayList();}" + 
                        "ctx._source.foods.add(params.food);";
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
                .id(food.getId())
                .build();
    }

    @Override
    public UpdateResultDto updateFood(String truckId, FoodUpdateRequestDto foodUpdateRequestDto) {

        Food food = foodUpdateRequestDto.toEntity();
        String foodImageUrl = s3Service.uploadFoodImage(truckId, food.getId(), foodUpdateRequestDto.getImage());
        log.info("image uploaded to s3 bucket. url=" + foodImageUrl);
        food.setImageUrl(foodImageUrl);

        Map<String, Object> params = new HashMap<>();
        params.put("food", food.toString());

        String script = "def target = ctx._source.foods.find(food -> food.id == params.food.id);" +
                           "target.name = params.food.name;" + 
                           "target.cost = params.food.cost;" +
                           "target.description = params.food.description;" +
                           "target.imageUrl = params.food.imageUrl;";
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
                .id(food.getId())
                .build();
    }
    
    @Override
    public UpdateResultDto deleteFood(String truckId, String id) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        String script = "def target = ctx._source.foods.removeIf(food -> food.id == params.id);";
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

}
