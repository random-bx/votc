package com.voiceofthecity.server.dto;
import lombok.Data;
@Data
public class QueryRequest {
    private String query;
    private String language;
}