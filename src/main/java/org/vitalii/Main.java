package org.vitalii;

import org.vitalii.model.Account;
import org.vitalii.model.ExchangeRate;
import org.vitalii.model.Transaction;
import org.vitalii.model.User;
import org.vitalii.service.Currency;
import org.vitalii.service.Service;

import javax.persistence.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static EntityManager em;

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Bank");
        em = emf.createEntityManager();
        startSession();
        em.close();
        emf.close();
    }

    private static void startSession(){
        initExchangeRate();
        while (true) {
            Service.printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewClients();
                    break;
                case "2":
                    registerClient();
                    break;
                case "3":
                    createAccount();
                    break;
                case "4":
                    addMoney();
                    break;
                case "5":
                    transferMoney();
                    break;
                case "6":
                    convertCurrency();
                    break;
                case "7":
                    System.out.println("All your money in UAH - " + getAmountInUAH());
                    break;
                default:
                    return;
            }
        }
    }

    public static void initExchangeRate() {
        ExchangeRate uahToUsd = new ExchangeRate("UAH", "USD", 0.037);
        ExchangeRate uahToEur = new ExchangeRate("UAH", "EUR", 0.032);
        ExchangeRate usdToUah = new ExchangeRate("USD", "UAH", 26.82);
        ExchangeRate usdToEur = new ExchangeRate("USD", "EUR", 0.85);
        ExchangeRate eurToUsd = new ExchangeRate("EUR", "USD", 1.18);
        ExchangeRate eurToUah = new ExchangeRate("EUR", "UAH", 31.55);
        performTransaction(() -> {
            em.persist(uahToUsd);
            em.persist(uahToEur);
            em.persist(usdToUah);
            em.persist(usdToEur);
            em.persist(eurToUsd);
            em.persist(eurToUah);
            return null;
        });
    }

    private static <T> T performTransaction(Callable<T> action) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        try {
            T result = action.call();

            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        }
    }

    private static <T> T find(Class<T> cls){
        String input = scanner.nextLine();

        try{
            Long id = Long.parseLong(input);

            return em.find(cls, id);
        }catch (NumberFormatException e){
            return null;
        }
    }

    private static void registerClient(){
        System.out.println("Enter first name:");
        String firstName = scanner.nextLine();

        System.out.println("Enter last name:");
        String lastName = scanner.nextLine();

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.firstName = '" + firstName + "' AND u.lastName = '" + lastName + '\'', User.class);
        try {

            query.getSingleResult();
            System.out.println("User already registered!");

        }catch (NoResultException e){
            User user = new User(firstName, lastName);

            user.addAccount(new Account(0.0, getCurrency()));

            performTransaction(() -> {
                em.persist(user);
                return user;
            });
        }
    }

    private static void createAccount(){
        System.out.println("Enter user id:");
        User user = find(User.class);
        Currency currency = getCurrency();
        newAccount(user, currency);
    }

    private static Account newAccount(User user, Currency currency){
        if(user == null){
            System.out.println("Wrong id!");
            return null;
        }

        if(user.getAccount(currency) == null) {
            Account account = new Account(0.0, currency);
            user.addAccount(account);
            return account;
        }else{
            System.out.println("Account already created!");
        }

        performTransaction(() -> {
            em.merge(user);
            return user;
        });

        return null;
    }

    private static void transferMoney(){
        System.out.println("Enter your account id:");
        Account sender = find(Account.class);
        System.out.println("Enter receiver account id:");
        Account receiver = find(Account.class);
        Double amount = getAmount();
        transfer(sender, receiver, amount);
    }

    private static void viewClients(){
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        List<User> users = query.getResultList();

        for(User user : users){
            System.out.println(user);
        }
    }

    private static void addMoney(){
        System.out.println("Enter account id:");

        Account account = find(Account.class);

        if(account == null){
            System.out.println("Incorrect id");
            return;
        }

        Currency currency = getCurrency();

        if(currency != account.getCurrencyEnum()){
            Double multiplier = getMultiplier(currency, account.getCurrencyEnum());
            account.addAmount(getAmount() * multiplier);
        }else{
            account.addAmount(getAmount());
        }

        performTransaction(() -> {
            em.merge(account);
            return account;
        });
    }

    private static void transfer(Account sender, Account receiver, Double amount){
        if(sender == null || receiver == null){
            System.out.println("Incorrect id");
            return;
        }

        if(amount > sender.getAmount()){
            System.out.println("Not enough money!");
            return;
        }

        Transaction transaction = new Transaction(sender, receiver, amount, sender.getCurrencyEnum());

        transaction.init(getMultiplier(sender.getCurrencyEnum(), receiver.getCurrencyEnum()));

        performTransaction(() -> {
            em.persist(transaction);
            return null;
        });
    }

    private static void convertCurrency(){
        System.out.println("Enter user id:");
        User user = find(User.class);

        if(user == null){
            System.out.println("Wrong id!");
            return;
        }

        Service.printAccounts(user);

        int choice = chooseAccount(user.getAccounts().size());

        Account sender = user.getAccounts().get(choice);

        Currency currency = getCurrency();

        Account receiver = newAccount(user, currency);

        transfer(sender, receiver, sender.getAmount());
    }

    private static Double getAmountInUAH(){
        System.out.println("Enter user id:");
        User user = find(User.class);

        if(user == null){
            System.out.println("Wrong id!");
            return null;
        }

        double sum = 0.0;

        List<Account> accounts = user.getAccounts();

        for(int i=0; i<accounts.size(); i++){
            Account account = accounts.get(i);
            sum += account.getAmount() * getMultiplier(account.getCurrencyEnum(), Currency.UAH);
        }

        return sum;
    }

    private static int chooseAccount(int max){
        System.out.println("Choose account to convert:");

        String input = scanner.nextLine();
        try {
            int choice = Integer.parseInt(input);

            if (choice < 0 || choice > max) {
                chooseAccount(max);
            }

            return choice-1;
        }catch (NumberFormatException e){
            System.out.println("WRONG");
            chooseAccount(max);
        }
        return -1;
    }

    private static Currency getCurrency(){
        Service.printCurrency();

        String choice = scanner.nextLine();

        Currency currency = Currency.UAH;

        switch (choice){
            case "1":
                break;
            case "2":
                currency = Currency.USD;
                break;
            case "3":
                currency = Currency.EUR;
                break;
            default:
                getCurrency();
                break;
        }

        return currency;
    }

    private static double getAmount(){
        System.out.println("Enter amount:");
        String input = scanner.nextLine();

        try {
            return Double.parseDouble(input);
        }catch (NumberFormatException e){
            getAmount();
        }
        return -1;
    }

    private static double getMultiplier(Currency from, Currency to){
        if(from == to){
            return 1.0;
        }
        Query query = em.createQuery("SELECT ex.rate FROM ExchangeRate ex WHERE ex.fromCurrency = '" + from.name() + "' AND ex.toCurrency = '" + to.name() + "'");
        double multiplier = (double) query.getSingleResult();

        return multiplier;
    }
}
