package Entities;

import Orm.annotations.Entity;

import java.time.LocalDate;

@Entity(name = "accounts")
public class Account {

    @ID
    @Column(name = "id")
    private int id;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "created_on")
    private LocalDate created_on;

    @Column(name = "age")
    private int age;

    public Account(String accountName, LocalDate created_on, int age,double money) {
        setAccountName(accountName);
        this.created_on = created_on;
        setAge(age);
    }

    public int getId() {
        return id;
    }


    public String getAccountName() {
        return accountName;
    }

    private void setAccountName(String accountName) {
        if (accountName == null || accountName.trim().isEmpty()) { throw new IllegalArgumentException("Invalid name !"); }
        this.accountName = accountName;
    }

    public LocalDate getCreated_on() {
        return created_on;
    }

    public int getAge() {
        return age;
    }

    private void setAge(int age) {
        if (age < 18) { throw new IllegalArgumentException("Non-adults are forbidden !"); }
        this.age = age;
    }
}
