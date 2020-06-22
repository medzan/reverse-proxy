package com.kapelse.ktmp.helpers;

import org.springframework.http.HttpHeaders;

import java.time.Duration;

import static java.lang.String.join;

public class Utils {


    public static HttpHeaders copyHeaders(HttpHeaders headers) {
        HttpHeaders copy = new HttpHeaders();
        copy.putAll(headers);
        return copy;
    }

    private Utils() {
    }
}
