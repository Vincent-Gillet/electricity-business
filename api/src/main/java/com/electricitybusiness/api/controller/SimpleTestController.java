package com.electricitybusiness.api.controller; // Adaptez le package

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class SimpleTestController {

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from SimpleTestController!");
    }

    @GetMapping("/null-test")
    public ResponseEntity<String> testNullResponse() {
        // Juste pour tester un retour null, si votre application le gère différemment
        return ResponseEntity.ok(null);
    }
}
