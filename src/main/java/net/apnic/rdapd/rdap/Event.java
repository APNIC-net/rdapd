package net.apnic.rdapd.rdap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Event
{
    public static enum EventAction
    {
        LAST_CHANGED("last changed");

        private final String value;

        EventAction(String value)
        {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString()
        {
            return value;
        }
    }

    private static final String DATE_FORMAT_STR;
    private static final TimeZone EVENT_TIMEZONE;

    static
    {
        DATE_FORMAT_STR = "yyyy-MM-dd'T'HH:mm'Z'";
        EVENT_TIMEZONE = TimeZone.getTimeZone("UTC");
    }

    private final EventAction action;
    private final String actor;
    private final String dateStr;

    public Event(EventAction action, Date date)
    {
        this(action, null, date);
    }

    public Event(EventAction action, String dateStr)
    {
        this(action, null, dateStr);
    }

    public Event(EventAction action, String actor, Date date)
    {
        this.action = action;
        this.actor = actor;
        DateFormat df = new SimpleDateFormat(DATE_FORMAT_STR);
        df.setTimeZone(EVENT_TIMEZONE);
        dateStr = df.format(date);
    }

    public Event(EventAction action, String actor, String dateStr)
    {
        this.action = action;
        this.actor = actor;
        this.dateStr = dateStr;
    }

    @JsonProperty("eventAction")
    public EventAction getAction()
    {
        return action;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("eventActor")
    public String getActor()
    {
        return actor;
    }

    @JsonProperty("eventDate")
    public String getDateValueStr()
    {
        return dateStr;
    }
}
