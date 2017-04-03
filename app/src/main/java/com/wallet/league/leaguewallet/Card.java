package com.wallet.league.leaguewallet;

/**
 * Created by irteza.arif on 2017-04-03.
 */

public class Card {
    String type;
    int amount;
    String currency;
    String startDate;
    String endDate;

    Card(String type, int amount, String currency, String startDate, String endDate) {
        this.type = type;
        this.amount = amount;
        this. currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
