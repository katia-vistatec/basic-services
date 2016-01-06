package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.filter.ExpressionFilter;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.util.Enumeration;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 06.01.2016.
 */
public class LoggingHelper {

    /**
     * Sets specific LoggingFilters and initiates suppression of specified Exceptions in Log4j.
     * @param x Class of Exception
     **/
    public static void loggerIgnore(Class<Throwable> x){
        loggerIgnore(x.getName());
    }

    /**
     * Sets specific LoggingFilters and initiates suppression of specified Exceptions in Log4j.
     * @param x String name of Exception
     **/
    public static void loggerIgnore(String x) {

        String newExpression="EXCEPTION ~="+x;
        Enumeration<Appender> allAppenders= Logger.getRootLogger().getAllAppenders();
        Appender appender;

        while (allAppenders.hasMoreElements()) {
            appender=allAppenders.nextElement();
            String oldExpression;
            ExpressionFilter exp;
            try {
                exp = ((ExpressionFilter) appender.getFilter());
                oldExpression = exp.getExpression();
                if (!oldExpression.contains(newExpression)) {
                    exp.setExpression(oldExpression + " || " + newExpression);
                    exp.activateOptions();
                }
            } catch (NullPointerException e) {
                exp = new ExpressionFilter();
                exp.setExpression(newExpression);
                exp.setAcceptOnMatch(false);
                exp.activateOptions();
                appender.clearFilters();
                appender.addFilter(exp);
            }
        }
    }

    /**
     * Clears specific LoggingFilters and stops their suppression of Exceptions in Log4j.
     * @param x Class of Exception
     **/
    public static void loggerUnignore(Class<Throwable> x) {
        loggerUnignore(x.getName());
    }

    /**
     * Clears specific LoggingFilters and stops their suppression of Exceptions in Log4j.
     * @param x String name of Exception
     **/
    public static void loggerUnignore(String x) {
        Enumeration<Appender> allAppenders= Logger.getRootLogger().getAllAppenders();
        Appender appender;

        while (allAppenders.hasMoreElements()) {
            appender=allAppenders.nextElement();
            ExpressionFilter exp = ((ExpressionFilter) appender.getFilter());
            exp.setExpression(exp.getExpression().replace("|| EXCEPTION ~=" + x, "").replace("EXCEPTION ~=" + x + "||", ""));
            exp.activateOptions();
            appender.clearFilters();
            appender.addFilter(exp);
        }
    }

    /**
     * Clears all LoggingFilters for all Appenders. Stops suppression of Exceptions in Log4j.
     **/
    public static void loggerClearFilters() {
        Enumeration<Appender> allAppenders = Logger.getRootLogger().getAllAppenders();
        Appender appender;

        while (allAppenders.hasMoreElements()) {
            appender = allAppenders.nextElement();
            appender.clearFilters();
        }
    }
}
