package in.btm.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.btm.dto.RegisterRequest;

@FeignClient(
        name = "AUTHENTICATIONSERVICE"
)
public interface AuthClient {

    @PostMapping("/auth/register")
    void register(@RequestBody RegisterRequest request);
}