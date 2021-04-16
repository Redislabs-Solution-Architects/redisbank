package com.redislabs.demos.redisbank;

import com.redislabs.demos.redisbank.Config.StompConfig;
import com.redislabs.lettusearch.SearchResults;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(path = "/api")
@CrossOrigin
public class TransactionOverviewController {

    private final Config config;

    public TransactionOverviewController(Config config) {
        this.config = config;
    }

	@GetMapping("/config/stomp")
	public StompConfig stompConfig() {
		return config.getStomp();
	}

    @GetMapping("/search")
    public SearchResults<String, String> findTransactionsBy(String criteria)   {
        return null;
    }

    @GetMapping("/list")
    public SearchResults<String, String> listTransactionsByDate()   {
        return null;
    }

    @GetMapping(value="/login")
    public void login(@RequestParam String userName) {
        
    }
    
    
}
