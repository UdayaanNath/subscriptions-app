package org.demo.com.subscriptionsapp.util;

import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;

import java.util.Calendar;
import java.util.Date;

public final class DateUtils {

    private DateUtils() {
    }

    public static Date addDays(Date from, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static Date calculateExpiryDate(Date from, SubscriptionPlan plan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.add(Calendar.DATE, daysFor(plan));
        return calendar.getTime();
    }

    private static int daysFor(SubscriptionPlan plan) {
        return switch (plan) {
            case MONTHLY -> 30;
            case QUATERLY -> 90;
            case YEARLY -> 365;
        };
    }
}
