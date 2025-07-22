package com.example.threaddump_analyzer.dto;

import lombok.Data;

@Data
public class RemoteDumpRequest {
    private String host;
    private String port;
    private String pid;
    private String username;
    private String password;
}