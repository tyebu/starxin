package com.star.starxin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Star
 * @createdDate
 * @description
 */
@RestController
public class HelloController {
    @RequestMapping("/hello")
    public Map<String, Object> Hello() {

        Map<String, Object> map =new HashMap<>();
        map.put("msg","2333");
        return map;
    }
}
