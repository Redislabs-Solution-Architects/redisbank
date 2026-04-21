package com.redislabs.demos.redisbank.transactions;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import io.lettuce.core.search.SearchReply;
import io.lettuce.core.search.arguments.SearchArgs;
import com.redis.lettucemod.timeseries.Sample;
import com.redis.lettucemod.timeseries.TimeRange;
import com.redislabs.demos.redisbank.Config;
import com.redislabs.demos.redisbank.Config.StompConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Sample> tsValues = srsc.sync().tsRange(BALANCE_TS,
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

        if (range == null || range.isEmpty()) {
            return new BiggestSpenders(0);
        }

        BiggestSpenders biggestSpenders = new BiggestSpenders(range.size());
        int i = 0;
        for (TypedTuple<String> typedTuple : range) {
            final double s = (typedTuple.getScore() == null) ? 0d : typedTuple.getScore(); // no unboxing if null
            biggestSpenders.getSeries()[i] = Math.floor(s * 100) / 100;
            biggestSpenders.getLabels()[i] = typedTuple.getValue();
            i++;
        }
        return biggestSpenders;
    }

    @GetMapping("/search")
    @SuppressWarnings("all")
    public List<Map<String, String>> searchTransactions(@RequestParam("term") String term) {
        RediSearchCommands<String, String> commands = srsc.sync();

        SearchArgs<String, String> options = SearchArgs.<String, String>builder()
                .highlightField("description")
                .highlightField("fromAccountName")
                .highlightField("transactionType")
                .highlightTags("<mark>", "</mark>")
                .build();

        SearchReply<String, String> results = commands.ftSearch(SEARCH_INDEX, term, options);
        return toLegacyDocumentList(results);
    }

    @GetMapping("/transactions")
    public List<Map<String, String>> listTransactions() {
        RediSearchCommands<String, String> commands = srsc.sync();
        return toLegacyDocumentList(commands.ftSearch(ACCOUNT_INDEX, "lars"));
    }

    private List<Map<String, String>> toLegacyDocumentList(SearchReply<String, String> reply) {
        return reply.getResults().stream().map(result -> {
            Map<String, String> row = new LinkedHashMap<>(result.getFields());
            row.put("id", result.getId());
            return row;
        }).collect(Collectors.toList());
    }

}
