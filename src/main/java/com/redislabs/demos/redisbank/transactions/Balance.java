package com.redislabs.demos.redisbank.transactions;

import com.redislabs.redistimeseries.Value;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Balance {

    private long x;
    private double y;

    public Balance(Value v) {
        this.x = v.getTime();
        this.y = v.getValue();
    }

}
