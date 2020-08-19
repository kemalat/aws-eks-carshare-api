package com.rideshare.controller;

import com.rideshare.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {
    @Autowired
    InstanceService service;

    @RequestMapping("/dummy")
    public String dummy()  {
        return "Hello , " + service.getHostName();
    }

    @GetMapping({"/dummy/{name}", "/dummy-service/{name}"})
    public String dummy2(@PathVariable String name)  {
        return "Hello , " + name;
    }
}
