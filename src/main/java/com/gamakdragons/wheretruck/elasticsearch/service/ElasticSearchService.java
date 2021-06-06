package com.gamakdragons.wheretruck.elasticsearch.service;

import java.io.IOException;

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
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

public interface ElasticSearchService {

    GetResponse get(GetRequest request, RequestOptions options) throws IOException;

    SearchResponse search(SearchRequest searchRequest, RequestOptions options) throws IOException;

    IndexResponse index(IndexRequest request, RequestOptions options) throws IOException; 
    UpdateResponse update(UpdateRequest request, RequestOptions options) throws IOException;
    DeleteResponse delete(DeleteRequest request, RequestOptions options) throws IOException;
    BulkByScrollResponse deleteByQuery(DeleteByQueryRequest request, RequestOptions options) throws IOException;
}
