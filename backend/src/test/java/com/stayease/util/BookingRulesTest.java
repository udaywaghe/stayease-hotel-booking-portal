package com.stayease.util;
import com.stayease.exception.ApiException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class BookingRulesTest {
    @Test void countsNightsUsingCheckoutExclusive(){LocalDate in=LocalDate.now().plusDays(4);assertEquals(3,BookingRules.nights(in,in.plusDays(3)));}
    @Test void calculatesTotalWithTwoDecimalPlaces(){assertEquals(new BigDecimal("57000.00"),BookingRules.total(new BigDecimal("28500"),2));}
    @Test void rejectsCheckoutBeforeOrOnCheckin(){LocalDate in=LocalDate.now().plusDays(2);assertThrows(ApiException.class,()->BookingRules.nights(in,in));assertThrows(ApiException.class,()->BookingRules.nights(in,in.minusDays(1)));}
    @Test void rejectsPastCheckin(){assertThrows(ApiException.class,()->BookingRules.nights(LocalDate.now().minusDays(1),LocalDate.now().plusDays(1)));}
}
