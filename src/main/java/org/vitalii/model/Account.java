package org.vitalii.model;

import org.vitalii.service.Currency;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Double amount;

    private String currency;

    @Transient
    private Currency currencyEnum;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "from")
    private Transaction from;

    @OneToOne(mappedBy = "to")
    private Transaction to;

    public Account() {
    }

    public Account(Double amount, Currency currencyEnum) {
        this.amount = amount;
        this.currencyEnum = currencyEnum;
        this.currency = currencyEnum.name();
    }

    public void addAmount(Double amount){
        if(amount > 0){
            this.amount += amount;
        }
    }

    public void minusAmount(Double amount){
        if(amount > 0){
            this.amount -= amount;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Transaction getFrom() {
        return from;
    }

    public void setFrom(Transaction from) {
        this.from = from;
    }

    public Transaction getTo() {
        return to;
    }

    public void setTo(Transaction to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", amount=" + amount +
                ", currency=" + currency +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(amount, account.amount) && currencyEnum == account.currencyEnum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, currencyEnum);
    }
}
