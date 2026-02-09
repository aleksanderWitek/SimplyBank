package main.java.com.alex.dto;

public class SaveBankAccountRequest {

    private final Long clientId;
    private final String bankAccountType;
    private final String bankAccountCurrency;

    public SaveBankAccountRequest(Long clientId, String bankAccountType, String bankAccountCurrency) {
        this.clientId = clientId;
        this.bankAccountType = bankAccountType;
        this.bankAccountCurrency = bankAccountCurrency;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public String getBankAccountCurrency() {
        return bankAccountCurrency;
    }
}
