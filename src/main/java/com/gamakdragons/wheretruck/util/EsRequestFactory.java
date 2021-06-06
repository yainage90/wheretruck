package com.gamakdragons.wheretruck.util;

import com.gamakdragons.wheretruck.common.GeoLocation;
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
import org.elasticsearch.script.Script;
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
    
    public static SearchRequest createSearchAllRequest(String indexName, String[] fieldsToInclude, String[] fieldsToExclude) {

        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.fetchSource(fieldsToInclude, fieldsToExclude);

        request.source(searchSourceBuilder);

        return request;
    }


    public static SearchRequest createGeoSearchRequest(String indexName, GeoLocation geoLocation, float distance) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        GeoDistanceQueryBuilder geoDistanceQuery = QueryBuilders.geoDistanceQuery("geoLocation");
        geoDistanceQuery.point(new GeoPoint(geoLocation.getLat(), geoLocation.getLon()));
        geoDistanceQuery.distance(distance + "km");

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchAllQuery());
        queryBuilder.filter(geoDistanceQuery);

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;
    }
    
    public static SearchRequest createGeoSearchRequest(String indexName, GeoLocation geoLocation, float distance, String[] fieldsToInclude, String[] fieldsToExclude) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        GeoDistanceQueryBuilder geoDistanceQuery = QueryBuilders.geoDistanceQuery("geoLocation");
        geoDistanceQuery.point(new GeoPoint(geoLocation.getLat(), geoLocation.getLon()));
        geoDistanceQuery.distance(distance + "km");

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchAllQuery());
        queryBuilder.filter(geoDistanceQuery);

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.fetchSource(fieldsToInclude, fieldsToExclude);

        request.source(searchSourceBuilder);

        return request;
    }

    public static SearchRequest createAddressSearchRequest(String indexName, String city, String town) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
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

    public static SearchRequest createAddressSearchRequest(String indexName, String city, String town, String[] fieldsToInclude, String[] fieldsToExclude) {

        SearchRequest request = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
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
        searchSourceBuilder.fetchSource(fieldsToInclude, fieldsToExclude);

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

    public static SearchRequest createSearchByFieldRequest(String index, String field, String value) {

        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        request.source(searchSourceBuilder);

        return request;
    }

    public static DeleteByQueryRequest createDeleteByFieldRequest(String[] indices, String field, String value) {
        
        DeleteByQueryRequest request = new DeleteByQueryRequest(indices);

        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(field, value);
        request.setQuery(termQueryBuilder);

        request.setMaxRetries(3);

        return request;
    }

    public static UpdateRequest createUpdateWithScriptRequest(String index, String id, Script inline) {

        UpdateRequest request = new UpdateRequest(index, id);
        request.script(inline);

        return request;

    }

}
