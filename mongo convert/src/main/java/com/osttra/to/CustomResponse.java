
package com.osttra.to;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

 

import lombok.Getter;
import lombok.Setter;

 

@Getter
@Setter
public class CustomResponse<T> {
    private T data;
    private String message;
    private int statusCode;
    private String path;

 

    public CustomResponse(T data, String message, int statusCode, String path) {
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
        this.path = path;
    }
}