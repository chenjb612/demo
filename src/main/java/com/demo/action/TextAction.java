package com.demo.action;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TextAction {
        @RequestMapping("/hello")
        public String index() {
            return "Hello World";
        }
}
