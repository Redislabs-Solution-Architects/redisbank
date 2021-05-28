package com.redislabs.demos.redisbank.investments;

import org.springframework.data.repository.CrudRepository;

public interface TradeRepository extends CrudRepository<Trade, String>{
    
}
