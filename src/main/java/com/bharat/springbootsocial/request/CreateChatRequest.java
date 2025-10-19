package com.bharat.springbootsocial.request;

import java.util.UUID;

import lombok.Data;

@Data
public class CreateChatRequest {
    private UUID userId;
}
