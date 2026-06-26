package com.store.product.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.store.product.dto.EmbeddingRequest;
import com.store.product.dto.EmbeddingResponse;

@FeignClient(name = "ai-service", url = "http://localhost:2050")
public interface AiServiceClient {

        @PostMapping("/ai/embeddings")
        public ResponseEntity<EmbeddingResponse> createEmbedding(@RequestBody EmbeddingRequest request) ;
       
       
    

}
