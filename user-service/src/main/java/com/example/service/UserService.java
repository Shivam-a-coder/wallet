package com.example.service;

import com.example.Dto.UserDto;
import com.example.Dto.UserProfile;
import com.example.UserCreatedPayload;
import com.example.UserNotExistException;
import com.example.entity.User;
import com.example.repo.UserRepo;

import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final String PREFIX ="user:";
    private static final String TOPIC = "USER_CREATED";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RedisTemplate<String,User> redisTemplate;

    @Autowired
    private KafkaTemplate<String, UserCreatedPayload> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    public Long createUser(UserDto userDto) throws ExecutionException, InterruptedException, JsonProcessingException {
        User user = User.builder().name(userDto.getName())
                .email(userDto.getEmail())
                .phone(userDto.getPhone())
                .kycId(userDto.getKycId())
                .address(userDto.getAddress())
                .build();
        userRepo.save(user);

        UserCreatedPayload userCreatedPayload = UserCreatedPayload.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        // LitenableFuture is deprecated so that's why we have to use CompletableFuture
        CompletableFuture<?> completableFuture = kafkaTemplate.send(TOPIC,userCreatedPayload);
        LOGGER.info("Pushed to kafka, kafka response : {}", completableFuture.get());
        return user.getId();
    }

    public UserProfile getuserProfile(Long id) throws UserNotExistException {
        String key = PREFIX+id;
        User user = redisTemplate.opsForValue().get(key);
        if(user == null){
            user = userRepo.findById(id).get();
            if(user != null){
                redisTemplate.opsForValue().set(key,user);
            }
            else {
                throw new UserNotExistException("User does not exist");
            }
        }

        // call wallet to get balance
        String url = "http://localhost:8081/wallet-service/balance";
        HttpHeaders httpHeaders = new HttpHeaders();
        String userIdString = id.toString();
        httpHeaders.set("userId", userIdString);
        httpHeaders.set("requestId", MDC.get("requestId"));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//      ResponseEntity<JsonNode> apiResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JsonNode.class);

        Double balance = null;
        try {
            ResponseEntity<Double> apiResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Double.class);
            balance = apiResponse.getBody();
        }
        catch (Exception ex){
            LOGGER.error("Exception while calling wallet service");
        }

        UserProfile userProfile = UserProfile.builder()
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .balance(balance)
                .build();

        return  userProfile;
    }
}
