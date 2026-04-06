package com.example.demo.entity;

public enum TransactionCategory {
    // Crédito — de onde veio o dinheiro
    SALARY,
    GIFT,
    INVESTMENT_RETURN,
    OTHER_INCOME,

    // Débito — onde foi o dinheiro
    FOOD,
    CLOTHING,
    TRANSPORT,
    HEALTH,
    ENTERTAINMENT,
    EDUCATION,
    BILLS,
    OTHER_EXPENSE,

    // Transferência entre contas próprias
    TRANSFER
}
