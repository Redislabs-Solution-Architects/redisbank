package com.redislabs.demos.redisbank.investments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class TradeController {

    private final DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
    private final TradeRepository tr;

    public TradeController(TradeRepository tr) {
        this.tr = tr;
    }

    public void settleTrade(String tradeId)    {
        Optional<Trade> tradeMaybe = tr.findById(tradeId);
        
        if (tradeMaybe.isPresent())  {
            Trade trade = tradeMaybe.get();
            trade.setSettledBy("Trading desk");
            trade.setSettledDate(df.format(new Date()));
            tr.save(trade);
        }else{
            // Trade not found
        }

    }

}
