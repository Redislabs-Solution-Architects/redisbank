package com.redislabs.demos.redisbank.investments;

import org.springframework.data.repository.CrudRepository;

public interface StockPortfolioRepository extends CrudRepository<StockPortfolio, String> {

}
