package com.example.prac2.api;

/**
 * Simple wrapper used to keep API responses consistent.
 */
public class ApiResponse<T> {

    private final String status;
    private final T data;
    private final String error;

    private ApiResponse(String status, T data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }

    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
