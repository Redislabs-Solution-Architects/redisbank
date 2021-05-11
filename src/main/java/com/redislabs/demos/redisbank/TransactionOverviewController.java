package com.redislabs.demos.redisbank;

import com.redislabs.demos.redisbank.Config.StompConfig;
import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.SearchResults;
import com.redislabs.lettusearch.StatefulRediSearchConnection;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin
public class TransactionOverviewController {

    private static final String TRANSACTIONS_INDEX = "transaction_idx";

    private final Config config;
    private final UserSessionRepository userSessionRepository;
    private final BankTransactionRepository btr;
    private final StatefulRediSearchConnection<String, String> srsc;

    public TransactionOverviewController(Config config, UserSessionRepository userSessionRepository,
            BankTransactionRepository btr, StatefulRediSearchConnection<String, String> srsc) {
        this.config = config;
        this.userSessionRepository = userSessionRepository;
        this.btr = btr;
        this.srsc = srsc;
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
    public SearchResults<String, String> listTransactions() {
        RediSearchCommands<String, String> commands = srsc.sync();
        SearchResults<String, String> results = commands.search(TRANSACTIONS_INDEX, "lars");
        return results;
    }

    @GetMapping(value = "/login")
    public void login(@RequestParam String userName) {
        String accountNumber = FakeIbanUtil.generateFakeIbanFrom(userName);
        UserSession userSession = new UserSession(userName, accountNumber);
        userSessionRepository.save(userSession);

    }

}
