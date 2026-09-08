package com.qs.Backend.modules.notification.delivery.controller;

import com.qs.Backend.modules.notification.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deliverys")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
}
