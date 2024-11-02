package com.knu.cdp1.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class testController {

    @PostMapping(value = "/testData")
    public Map<Integer, String> testData(@RequestBody List<String> params) {
        Map<Integer, String> data = new HashMap<>();
        data.put(1, "받은 데이터1");
        data.put(2, "받은 데이터2");
        data.put(3, "받은 데이터3");

        int i = 4;
        for (String param : params) {
            data.put(i, param);
            i++;
        }

        return data;
    }
}
