package com.sports.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success = false;
    private int status;
    private String errorCode;
    private String message;
    private List<FieldErrorDetail> validationErrors;
    private String requestId;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.requestId = UUID.randomUUID().toString().substring(0, 8);
    }

    public ErrorResponse(int status, String errorCode, String message, List<FieldErrorDetail> validationErrors) {
        this.success = false;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.validationErrors = validationErrors;
        this.requestId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(int status, String errorCode, String message) {
        return new ErrorResponse(status, errorCode, message, null);
    }

    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return new ErrorResponse(status, errorCode, message, null);
    }

    public static class FieldErrorDetail {
        private String field;
        private Object rejectedValue;
        private String message;

        public FieldErrorDetail() {}

        public FieldErrorDetail(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public boolean isSuccess() { return success; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<FieldErrorDetail> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<FieldErrorDetail> validationErrors) { this.validationErrors = validationErrors; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
