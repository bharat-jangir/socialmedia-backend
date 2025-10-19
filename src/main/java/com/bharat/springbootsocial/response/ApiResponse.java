package com.bharat.springbootsocial.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String message;
    private Boolean status;
    private Object data;
    
    public ApiResponse(String message, Boolean status) {
        this.message = message;
        this.status = status;
    }
}
