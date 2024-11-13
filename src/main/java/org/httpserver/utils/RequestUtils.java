package org.httpserver.utils;

import java.util.Map;

public class RequestUtils {

    public static String toQueryParams(Map<String, String> params) {
        StringBuilder queryParams = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!queryParams.isEmpty()) {
                queryParams.append("&");
            }
            queryParams.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        return queryParams.toString();
    }

}
