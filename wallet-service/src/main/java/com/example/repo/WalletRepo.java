package com.example.repo;

import com.example.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepo extends JpaRepository<Wallet,Long> {
    // we have defined this and jpa will automatically generate the method
    Wallet findByUserId(Long userId);
}
