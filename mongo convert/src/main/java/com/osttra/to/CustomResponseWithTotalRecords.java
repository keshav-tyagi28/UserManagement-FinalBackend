package com.osttra.to;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class CustomResponseWithTotalRecords<T> extends CustomResponse<T> {
    private long totalRecords;

    public CustomResponseWithTotalRecords(T data, String message, int statusCode, String path, long totalRecords) {
        super(data, message, statusCode, path);
        this.totalRecords = totalRecords;
    }

    public long getTotalRecords() {
        return totalRecords;
    }
}