package com.gamakdragons.wheretruck.cloud.elasticsearch.service;

import java.io.IOException;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private final RestHighLevelClient esClient;

    @Override
    public SearchResponse search(SearchRequest searchRequest, RequestOptions options) throws IOException {
        return esClient.search(searchRequest, options);
    }

    @Override
    public GetResponse get(GetRequest request, RequestOptions options) throws IOException {
        return esClient.get(request, options);
    }

    @Override
    public MultiGetResponse multiGet(MultiGetRequest request, RequestOptions options) throws IOException {
        return esClient.mget(request, options);
    }

    @Override
    public IndexResponse index(IndexRequest request, RequestOptions options) throws IOException {
        return esClient.index(request, options);
    }

    @Override
    public UpdateResponse update(UpdateRequest request, RequestOptions options) throws IOException {
        return esClient.update(request, options);
    }

    @Override
    public DeleteResponse delete(DeleteRequest request, RequestOptions options) throws IOException {
        return esClient.delete(request, options);
    }

}
