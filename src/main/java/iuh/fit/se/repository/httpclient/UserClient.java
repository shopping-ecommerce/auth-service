package iuh.fit.se.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import iuh.fit.se.config.AuthenticationRequestInterceptor;
import iuh.fit.se.dto.request.UserClientRequest;
import iuh.fit.se.dto.response.UserClientResponse;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8082",
        configuration = {AuthenticationRequestInterceptor.class})
public interface UserClient {
    @PostMapping(value = "/profiles/create", produces = MediaType.APPLICATION_JSON_VALUE)
    UserClientResponse createUser(@RequestBody UserClientRequest request);
}
