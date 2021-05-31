package com.gamakdragons.wheretruck.util;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.google.gson.Gson;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class EsRequestFactory {
    
    public static SearchRequest createSearchAllRequest(String indexName) {
        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        request.source(searchSourceBuilder);

        return request;
    }

    public static SearchRequest createGeoSearchRequest(String indexName, GeoLocation geoLocation, float distance) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        GeoDistanceQueryBuilder geoDistanceQuery = new GeoDistanceQueryBuilder("geoLocation");
        geoDistanceQuery.point(new GeoPoint(geoLocation.getLat(), geoLocation.getLon()));
        geoDistanceQuery.distance(distance + "km");

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.matchAllQuery());
        queryBuilder.filter(geoDistanceQuery);

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;
    }

    public static SearchRequest createAddressSearchRequest(String indexName, String city, String town) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        
        if(city != null && city.trim().length() > 0) {
            TermQueryBuilder cityQuery = QueryBuilders.termQuery("city", city);
            queryBuilder.must(cityQuery);

        }

        if(town != null && town.trim().length() > 0) {
            TermQueryBuilder townQuery = QueryBuilders.termQuery("town", town); 
            queryBuilder.filter(townQuery);
        }

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;
    }

    public static GetRequest createGetRequest(String indexName, String id) {
        return new GetRequest(indexName, id);
    }

    public static IndexRequest createIndexRequest(String indexName, String id, Object object) {
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id(id);
        indexRequest.source(new Gson().toJson(object), XContentType.JSON);

        return indexRequest;
    }

    public static UpdateRequest createUpdateRequest(String index, String id, Object object) {
        UpdateRequest request = new UpdateRequest(index, id);
        request.doc(new Gson().toJson(object), XContentType.JSON);

        return request;
    }

    public static DeleteRequest createDeleteByIdRequest(String index, String id) {
        DeleteRequest request = new DeleteRequest(index, id);
        return request;
    }

    public static SearchRequest createSearchByTruckIdRequest(String index, String truckId) {
        
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("truckId", truckId);

        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;
    }

    public static DeleteByQueryRequest createDeleteByTruckIdRequest(String[] indices, String truckId) {
        
        DeleteByQueryRequest request = new DeleteByQueryRequest(indices);

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("truckId", truckId);
        request.setQuery(termQueryBuilder);

        request.setMaxRetries(3);

        return request;
    }

    public static SearchRequest createSearchByUserIdRequest(String index, String userId) {

        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("userId", userId);

        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;

    }
    
}
