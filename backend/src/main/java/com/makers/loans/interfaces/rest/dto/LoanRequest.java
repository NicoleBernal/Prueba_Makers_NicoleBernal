package com.makers.loans.interfaces.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Cuerpo compartido por la solicitud (POST /api/loans) y la edición (PUT /api/admin/loans/{id}). */
public class LoanRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "100.00", message = "El monto mínimo es $100.00")
    @DecimalMax(value = "1000000.00", message = "El monto máximo es $1,000,000.00")
    @Digits(integer = 13, fraction = 2, message = "El monto debe tener máximo 2 decimales")
    private BigDecimal amount;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 3, message = "El plazo mínimo es de 3 meses")
    @Max(value = 60, message = "El plazo máximo es de 60 meses")
    private Integer termMonths;

    public LoanRequest() {
    }

    public LoanRequest(BigDecimal amount, Integer termMonths) {
        this.amount = amount;
        this.termMonths = termMonths;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
}
