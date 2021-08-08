package org.vitalii.service;

import org.vitalii.model.Account;
import org.vitalii.model.User;

import java.util.List;

public class Service {

    public static void printMenu() {
        System.out.println("\tSelect option:");
        System.out.println("\t1 -> view clients");
        System.out.println("\t2 -> register client");
        System.out.println("\t3 -> create account for client");
        System.out.println("\t4 -> add money");
        System.out.println("\t5 -> transfer money");
        System.out.println("\t6 -> convert currency");
        System.out.println("\t7 -> get amount in UAH from all accounts");
    }

    public static void printCurrency() {
        System.out.println("Select currency type:");
        System.out.println("1 -> uah");
        System.out.println("2 -> usd");
        System.out.println("3 -> eur");
    }

    public static void printAccounts(User user){
        System.out.println("Select account which you want to convert:");

        List<Account> accounts = user.getAccounts();

        for(int i=0; i<accounts.size(); i++){
            System.out.println(i+1 + " -> account with " + accounts.get(i).getAmount() + " " + accounts.get(i).getCurrency());
        }
    }
}
