package com.gamakdragons.wheretruck.util;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
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

    public static IndexRequest createIndexRequest(String indexName, Object object) {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source(object);
        indexRequest.

        return indexRequest;
    }

    public static UpdateRequest createUpdateRequest(String index, String id, Object object) {
        UpdateRequest request = new UpdateRequest(index, id);
        request.doc(object);

        return request;
    }

    public static DeleteRequest creatDeleteRequest(String index, String id) {
        DeleteRequest request = new DeleteRequest(index, id);
        return request;
    }
}
