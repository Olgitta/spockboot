package com.olg.qweb.api.test;

import com.olg.core.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class TestController {
    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test")
    public String test() {
        return "Hello, " + SecurityUtils.getUsername();
    }
}

