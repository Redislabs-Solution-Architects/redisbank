package com.redislabs.demos.redisbank;

import java.util.ArrayList;
import java.util.List;

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
    private final BankTransactionRepository btr;

    public TransactionOverviewController(Config config, UserSessionRepository userSessionRepository, BankTransactionRepository btr) {
        this.config = config;
        this.userSessionRepository = userSessionRepository;
        this.btr = btr;
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

    @GetMapping("/transactions")
    public List<BankTransaction> listTransactions() {
        List<BankTransaction> results = new ArrayList<BankTransaction>();
        btr.findByToAccount(FakeIbanUtil.generateFakeIbanFrom("lars")).forEach(results::add);
        return results;
    }

    @GetMapping(value = "/login")
    public void login(@RequestParam String userName) {
        String accountNumber = FakeIbanUtil.generateFakeIbanFrom(userName);
        UserSession userSession = new UserSession(userName, accountNumber);
        userSessionRepository.save(userSession);
        
    }

}
