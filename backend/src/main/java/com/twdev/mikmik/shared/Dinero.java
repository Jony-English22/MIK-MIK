package com.twdev.mikmik.shared;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Embeddable
public record Dinero(BigDecimal monto, Currency moneda) {

    public static final Currency MXN = Currency.getInstance("MXN");

    public Dinero {
        Objects.requireNonNull(monto, "monto");
        Objects.requireNonNull(moneda, "moneda");

        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo: " + monto);
        }
        monto = monto.setScale(moneda.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }

    public static Dinero pesos(String monto) {
        return new Dinero(new BigDecimal(monto), MXN);
    }

    public static Dinero pesos(BigDecimal monto) {
        return new Dinero(monto, MXN);
    }

    public static Dinero cero(Currency moneda) {
        return new Dinero(BigDecimal.ZERO, moneda);
    }

    public Dinero sumar(Dinero dinero) {
        validaMoneda(dinero);
        return new Dinero(monto.add(dinero.monto), moneda);
    }

    public Dinero restar(Dinero dinero) {
        validaMoneda(dinero);
        return new Dinero(monto.subtract(dinero.monto), moneda);
    }

    public Dinero multiplicar(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor");
        return new Dinero(monto.multiply(factor), moneda);
    }

    public boolean esMayorQue(Dinero dinero) {
        validaMoneda(dinero);
        return monto.compareTo(dinero.monto) > 0;
    }

    public boolean esCero() {
        return monto.signum() == 0;
    }

    private void validaMoneda(Dinero dinero) {
        Objects.requireNonNull(dinero, "dinero");
        if (!moneda.equals(dinero.moneda)) {
            throw new IllegalArgumentException(
                "Monedas distintas: " + moneda.getCurrencyCode() + " vs " + dinero.moneda.getCurrencyCode());
        }
    }

    @Override
    public String toString() {
        return monto.toPlainString() + " " + moneda.getCurrencyCode();
    }
}
