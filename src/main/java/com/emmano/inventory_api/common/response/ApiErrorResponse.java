package com.emmano.inventory_api.common.response;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(Instant timeInstant, int status, String error, String message, String path,
        Map<String, String> validationErrors) {

}
