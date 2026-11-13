package com.stayease.util;
import com.stayease.exception.ApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public final class BookingRules {
    private BookingRules() {}
    public static int nights(LocalDate checkIn,LocalDate checkOut){if(checkIn==null||checkOut==null)throw ApiException.badRequest("Check-in and check-out are required");if(checkIn.isBefore(LocalDate.now()))throw ApiException.badRequest("Check-in cannot be in the past");if(!checkOut.isAfter(checkIn))throw ApiException.badRequest("Check-out must be after check-in");return Math.toIntExact(ChronoUnit.DAYS.between(checkIn,checkOut));}
    public static BigDecimal total(BigDecimal nightlyPrice,int nights){return nightlyPrice.multiply(BigDecimal.valueOf(nights)).setScale(2);}
}
