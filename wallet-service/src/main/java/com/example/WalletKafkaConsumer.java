package com.example;

import com.example.entity.Wallet;
import com.example.repo.WalletRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@EnableKafka
@Configuration
public class WalletKafkaConsumer {
    private static Logger LOGGER = LoggerFactory.getLogger(WalletKafkaConsumer.class);

    private static String WALLET_TOPIC = "WALLET_UPDATE";

    private static String TXN_TOPIC = "TXN_COMP";

    @Autowired
    private KafkaTemplate<String, TransactionPayload> txnKafkaTemplate;

    @Autowired
    private  WalletRepo walletRepo;
    @Autowired
    public KafkaTemplate<String, WalletUpdatePayload> kafkaTemplate;


    @KafkaListener(topics = "USER_CREATED",groupId = "wallet-service", containerFactory ="userKafkaListenerContainerFactory")
    public void consumerUserCreated(UserCreatedPayload payload) throws JsonProcessingException,ExecutionException, InterruptedException {
        LOGGER.info("Consumed from kafka : {}", payload);
        Wallet wallet =Wallet.builder()
                .balance(100.00)
                .userId(payload.getUserId())
                .email(payload.getEmail())
                .build();
        walletRepo.save(wallet);

        WalletUpdatePayload walletUpdatePayload = WalletUpdatePayload.builder()
                .email(payload.getEmail())
                .balance(wallet.getBalance())
                .build();

        CompletableFuture<?> completableFuture = kafkaTemplate.send(WALLET_TOPIC,walletUpdatePayload);

        LOGGER.info("Pushed to {}, kafka response : {}", WALLET_TOPIC, completableFuture.get());
    }

    @KafkaListener(topics = "TXN_INIT",groupId = "wallet-service", containerFactory = "txnInitKafkaListenerContainerFactory")
    public void consumeFromTxnInit(TransactionPayload payload) throws ExecutionException, InterruptedException {
        Wallet fromWallet = walletRepo.findByUserId(payload.getFromUserId());
        if(fromWallet.getBalance() >= payload.getAmount()){
            Wallet toWallet = walletRepo.findByUserId(payload.getToUserId());
            fromWallet.setBalance(fromWallet.getBalance() - payload.getAmount());
            toWallet.setBalance(toWallet.getBalance() + payload.getAmount());

            walletRepo.save(fromWallet);
            walletRepo.save(toWallet);

            WalletUpdatePayload fromWalletUpdatePayload = WalletUpdatePayload.builder()
                    .email(fromWallet.getEmail())
                    .balance(fromWallet.getBalance())
                    .build();
            CompletableFuture<?> completableFuture  = kafkaTemplate.send(WALLET_TOPIC,fromWalletUpdatePayload);
            LOGGER.info("Pushed to {}, kafka response : {}", WALLET_TOPIC, completableFuture.get());

            WalletUpdatePayload toWalletUpdatePayload = WalletUpdatePayload.builder()
                    .email(toWallet.getEmail())
                    .balance(toWallet.getBalance())
                    .build();
            CompletableFuture<?> completableFuture2  = kafkaTemplate.send(WALLET_TOPIC,toWalletUpdatePayload);
            LOGGER.info("Pushed to {}, kafka response : {}", WALLET_TOPIC, completableFuture2.get());

            payload.setStatus("SUCCESS");
            CompletableFuture<?> completableFuture3  = txnKafkaTemplate.send(TXN_TOPIC,payload);
            LOGGER.info("Pushed to {}, kafka response : {}", TXN_TOPIC, completableFuture3.get());
            return;
        }
        payload.setStatus("FAILED");
        CompletableFuture<?> completableFuture3  = txnKafkaTemplate.send(TXN_TOPIC,payload);
        LOGGER.info("Pushed to {}, kafka response : {}", TXN_TOPIC, completableFuture3.get());
    }

}
