package com.gustavo.billingjob.config;

// Representa um registro (ou uma linha) do arquivo de entrada
public record BillingData (
        int dataYear,
        int dataMonth,
        int accountId,
        String phoneNumber,
        float dataUsage,
        int callDuration,
        int smsCount) {
}
