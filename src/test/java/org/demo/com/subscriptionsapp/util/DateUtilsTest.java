package org.demo.com.subscriptionsapp.util;

import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilsTest {

    @Test
    void addDaysMovesForwardAndBackward() {
        Date from = date(2026, Calendar.MARCH, 1);

        assertEquals(date(2026, Calendar.MARCH, 31), DateUtils.addDays(from, 30));
        assertEquals(date(2026, Calendar.FEBRUARY, 28), DateUtils.addDays(from, -1));
        assertEquals(from, DateUtils.addDays(from, 0));
    }

    @Test
    void calculateExpiryDateUsesPlanLengths() {
        Date from = date(2026, Calendar.JANUARY, 1);

        assertEquals(date(2026, Calendar.JANUARY, 31), DateUtils.calculateExpiryDate(from, SubscriptionPlan.MONTHLY));
        assertEquals(date(2026, Calendar.APRIL, 1), DateUtils.calculateExpiryDate(from, SubscriptionPlan.QUATERLY));
        assertEquals(date(2027, Calendar.JANUARY, 1), DateUtils.calculateExpiryDate(from, SubscriptionPlan.YEARLY));
    }

    @Test
    void addDaysCrossesALeapDay() {
        Date from = date(2024, Calendar.FEBRUARY, 28);
        Date result = DateUtils.addDays(from, 1);

        assertEquals(date(2024, Calendar.FEBRUARY, 29), result);
        assertTrue(result.getTime() - from.getTime() == TimeUnit.DAYS.toMillis(1));
    }

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }
}
