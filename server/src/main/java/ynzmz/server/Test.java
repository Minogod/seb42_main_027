package ynzmz.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Test {
    @Value("${test.test}")
    private String username;

    @GetMapping
    public String test(){
        return "서버 작동 확인" + username;
    }
}
