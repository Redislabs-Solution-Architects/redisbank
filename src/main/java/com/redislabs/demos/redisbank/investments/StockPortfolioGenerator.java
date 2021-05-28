package com.redislabs.demos.redisbank.investments;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.redislabs.demos.redisbank.SerializationUtil;
import com.redislabs.demos.redisbank.Utilities;

public class StockPortfolioGenerator {

    private List<Stock> stocks = new ArrayList<>();
    private final StockPortfolio spf;
    private final SecureRandom random;
    private final DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
    private final NumberFormat nf = NumberFormat.getCurrencyInstance();
    private final TradeRepository tr;
    private final StockPortfolioRepository spr;

    public StockPortfolioGenerator(TradeRepository tr, StockPortfolioRepository spr)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.random = SecureRandom.getInstance("SHA1PRNG");
        this.random.setSeed("lars".getBytes("UTF-8")); // Prime the RNG so it always generates the same pseudorandom set
        this.stocks = SerializationUtil.loadObjectList(Stock.class, "/stocks.csv");
        this.spf = generatePortfolioFor("lars");
        this.tr = tr;
        this.spr = spr;
        generateTrades(this.spf);
    }

    private StockPortfolio generatePortfolioFor(String userName) {

        StockPortfolio spf = new StockPortfolio();

        for (Stock stock : stocks) {
            spf.getStocks().put(stock, 100.0);
        }

        spf.setPortfolioHolderName("userName");
        spf.setPortfolioId(Utilities.generatePortfolioIdFrom(userName));
        spr.save(spf);
        return spf;
    }

    private void generateTrades(StockPortfolio spf) {
        for (Stock stock : spf.getStocks().keySet()) {
            Double amountLeft = spf.getStocks().get(stock).doubleValue();
            int amountToSell = this.random.nextInt(10);
            Trade trade = new Trade();
            trade.setAmount(amountToSell);
            trade.setCreatedDate(df.format(new Date()));
            trade.setFromPortfolio(Utilities.generatePortfolioIdFrom(spf.getPortfolioHolderName()));
            trade.setFromPortfolioName(spf.getPortfolioHolderName());
            trade.setStock(stock);
            tr.save(trade);
            amountLeft = amountLeft - amountToSell;
            spf.getStocks().put(stock, amountLeft);
        }
        spr.save(spf);
    }
}
