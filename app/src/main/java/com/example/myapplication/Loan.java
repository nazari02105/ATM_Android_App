package com.example.myapplication;

public class Loan {
    private int id;
    private int loanAmount;
    private int repaymentPeriod;
    private int installmentsPaid;
    private String qrValue;

    public Loan(int id, int loanAmount, int repaymentPeriod, int installmentsPaid, String qrValue) {
        this.id = id;
        this.loanAmount = loanAmount;
        this.repaymentPeriod = repaymentPeriod;
        this.installmentsPaid = installmentsPaid;
        this.qrValue = qrValue;
    }

    public int getId() {
        return id;
    }

    public int getLoanAmount() {
        return loanAmount;
    }

    public int getRepaymentPeriod() {
        return repaymentPeriod;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public String getQrValue() {
        return qrValue;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", LoanAmount='" + loanAmount + '\'' +
                ", RepaymentPeriod='" + repaymentPeriod + '\'' +
                ", InstallmentsPaid='" + installmentsPaid + '\'' +
                ", QrValue=" + qrValue +
                '}';
    }
}
