package com.redislabs.demos.redisbank;

import com.redislabs.demos.redisbank.Config.StompConfig;
import com.redislabs.lettusearch.SearchResults;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin
public class TransactionOverviewController {

    private final Config config;
    private final UserSessionRepository userSessionRepository;

    public TransactionOverviewController(Config config, UserSessionRepository userSessionRepository) {
        this.config = config;
        this.userSessionRepository = userSessionRepository;
    }

    @GetMapping("/config/stomp")
    public StompConfig stompConfig() {
        return config.getStomp();
    }

    @GetMapping("/search")
    public SearchResults<String, String> findTransactionsBy(String criteria) {
        return null;
    }

    @GetMapping("/list")
    public SearchResults<String, String> listTransactionsByDate() {
        return null;
    }

    @GetMapping(value = "/login")
    public void login(@RequestParam String userName) {
        String accountNumber = FakeIbanUtil.generateFakeIbanFrom(userName);
        UserSession userSession = new UserSession(userName, accountNumber);
        userSessionRepository.save(userSession);
        
    }

}
