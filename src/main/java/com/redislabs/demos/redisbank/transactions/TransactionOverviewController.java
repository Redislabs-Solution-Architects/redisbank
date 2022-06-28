package com.redislabs.demos.redisbank.transactions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.search.SearchOptions;
import com.redis.lettucemod.search.SearchResults;
import com.redis.lettucemod.timeseries.Sample;
import com.redis.lettucemod.timeseries.TimeRange;
import com.redislabs.demos.redisbank.Config;
import com.redislabs.demos.redisbank.Config.StompConfig;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin
public class TransactionOverviewController {

    private static final String ACCOUNT_INDEX = "transaction_account_idx";
    private static final String SEARCH_INDEX = "transaction_description_idx";
    private static final String BALANCE_TS = "balance_ts";
    private static final String SORTED_SET_KEY = "bigspenders";

    private final Config config;
    private final StatefulRedisModulesConnection<String, String> srsc;
    private final StringRedisTemplate redis;

    public TransactionOverviewController(Config config, StatefulRedisModulesConnection<String, String> srsc,
            StringRedisTemplate redis) {
        this.config = config;
        this.srsc = srsc;
        this.redis = redis;
    }

    @GetMapping("/config/stomp")
    public StompConfig stompConfig() {
        return config.getStomp();
    }

    @GetMapping("/balance")
    public Balance[] balance() {
        List<Sample> tsValues = srsc.sync().range(BALANCE_TS,
                TimeRange.from(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7))
                        .to(System.currentTimeMillis()).build());
        Balance[] balanceTs = new Balance[tsValues.size()];
        int i = 0;

        for (Sample entry : tsValues) {
            Object keyString = entry.getTimestamp();
            Object valueString = entry.getValue();
            balanceTs[i] = new Balance(keyString, valueString);
            i++;
        }

        return balanceTs;
    }

    @GetMapping("/biggestspenders")
    public BiggestSpenders biggestSpenders() {
        Set<TypedTuple<String>> range = redis.opsForZSet().rangeByScoreWithScores(SORTED_SET_KEY, 0, Double.MAX_VALUE);
        if (range.size() > 0) {
            BiggestSpenders biggestSpenders = new BiggestSpenders(range.size());
            int i = 0;
            for (TypedTuple<String> typedTuple : range) {
                biggestSpenders.getSeries()[i] = Math.floor(typedTuple.getScore() * 100) / 100;
                biggestSpenders.getLabels()[i] = typedTuple.getValue();
                i++;
            }
            return biggestSpenders;
        } else {
            return new BiggestSpenders(0);
        }

    }

    @GetMapping("/search")
    @SuppressWarnings("all")
    public SearchResults<String, String> searchTransactions(@RequestParam("term") String term) {
        RediSearchCommands<String, String> commands = srsc.sync();

        SearchOptions options = SearchOptions
                .builder().highlight(SearchOptions.Highlight.builder().field("description").field("fromAccountName")
                        .field("transactionType").tags("<mark>","</mark>").build()).build();

        SearchResults<String, String> results = commands.search(SEARCH_INDEX, term, options);
        return results;
    }

    @GetMapping("/transactions")
    public SearchResults<String, String> listTransactions() {
        RediSearchCommands<String, String> commands = srsc.sync();
        SearchResults<String, String> results = commands.search(ACCOUNT_INDEX, "lars");
        return results;
    }

}
