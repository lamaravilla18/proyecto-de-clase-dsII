package com.example.security.controller;

import com.example.security.servicios.NetworkTrafficService;
import org.pcap4j.core.PcapNativeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TemporalController {

    @Autowired
    private NetworkTrafficService service;

    @GetMapping("/admin/interfaces")
    public void printInterfaces() throws PcapNativeException {
        service.printInterfaces();
    }
}
