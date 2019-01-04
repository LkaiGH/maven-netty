package com.open.coinnews.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api")
public class ApiController {

    @GetMapping(value = "hello")
    public String hello(){

        return "hello";

    }
}
