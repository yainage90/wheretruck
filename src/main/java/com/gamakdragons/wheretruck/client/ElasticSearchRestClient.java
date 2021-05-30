package com.gamakdragons.wheretruck.client;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchRestClient {

    private RestHighLevelClient esClient;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    @PostConstruct
    private void createRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

        this.esClient = new RestHighLevelClient(builder);
    }

    public SearchResponse search(SearchRequest searchRequest, RequestOptions options) throws IOException {
        return esClient.search(searchRequest, options);
    }

    public GetResponse get(GetRequest request, RequestOptions options) throws IOException {
        return esClient.get(request, options);
    }

    public IndexResponse index(IndexRequest request, RequestOptions options) throws IOException {
        return esClient.index(request, options);
    }

    public UpdateResponse update(UpdateRequest request, RequestOptions options) throws IOException {
        return esClient.update(request, options);
    }

    public DeleteResponse delete(DeleteRequest request, RequestOptions options) throws IOException {
        return esClient.delete(request, options);
    }

    public BulkByScrollResponse deleteByQuery(DeleteByQueryRequest request, RequestOptions options) throws IOException {
        return esClient.deleteByQuery(request, options);
    }
}
