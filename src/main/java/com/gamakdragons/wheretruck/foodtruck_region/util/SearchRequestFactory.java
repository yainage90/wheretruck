package com.gamakdragons.wheretruck.foodtruck_region.util;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SearchRequestFactory {
    
    public static SearchRequest createMatchAllQuery(String indexName) {
        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        request.source(searchSourceBuilder);

        return request;
    }

    public static SearchRequest createGeoSearchQuery(String indexName, GeoLocation geoLocation, float distance) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

        queryBuilder.must(QueryBuilders.matchAllQuery());

        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder("geoLocation");
        geoDistanceQueryBuilder.point(new GeoPoint(geoLocation.getLat(), geoLocation.getLon()));
        geoDistanceQueryBuilder.distance(distance + "km");
        queryBuilder.filter(geoDistanceQueryBuilder);

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;

    }
}
