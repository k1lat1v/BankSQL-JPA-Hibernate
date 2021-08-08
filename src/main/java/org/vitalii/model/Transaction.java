package org.vitalii.model;

import org.vitalii.service.Currency;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_id")
    private Account from;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_id")
    private Account to;

    private Double amount;

    private String currency;

    private Date date;

    @Transient
    private Currency currencyEnum;

    public Transaction() {
    }

    public Transaction(Account from, Account to, Double amount, Currency currencyEnum) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.currency = currencyEnum.name();
        this.date = new Date();
    }

    public void init(Double multiplier){
        from.minusAmount(this.amount);
        to.addAmount(this.amount * multiplier);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getFrom() {
        return from;
    }

    public void setFrom(Account from) {
        this.from = from;
    }

    public Account getTo() {
        return to;
    }

    public void setTo(Account to) {
        this.to = to;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Currency getCurrencyEnum() {
        return currencyEnum;
    }

    public void setCurrencyEnum(Currency currencyEnum) {
        this.currencyEnum = currencyEnum;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}
