package com.example;

import com.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/wallet-service")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/balance")
    ResponseEntity<Double> getBalance(@RequestHeader Long userId){
        return ResponseEntity.ok(walletService.getBalanceByUserId(userId));
    }
}
