package iuh.fit.se.repository.httpclient;

import feign.Retryer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import iuh.fit.se.config.AuthenticationRequestInterceptor;
import iuh.fit.se.dto.request.UserClientRequest;
import iuh.fit.se.dto.response.UserClientResponse;

@FeignClient(
        name = "user-service",
        configuration = {UserClient.FeignRetryConfig.class
        })
public interface UserClient {
    @PostMapping(value = "/info/profiles/create", produces = MediaType.APPLICATION_JSON_VALUE)
    UserClientResponse createUser(@RequestBody UserClientRequest request);

    @Configuration
    public class FeignRetryConfig{
        @Bean
        public Retryer feignRetryConfig(){
            return new Retryer.Default(100,1000,3);
        }
    }
}
