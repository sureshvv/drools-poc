package com.sample;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.event.process.*;
import org.kie.api.event.rule.*;



/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

    public static final void main(String[] args) {
        try {
            // load up the knowledge base
	    KieServices ks = KieServices.Factory.get();
            // KieBaseConfiguration config = ks.newKieBaseConfiguration();
            // config.setOption( EventProcessingOption.STREAM );
    	    KieContainer kContainer = ks.getKieClasspathContainer();
            KieSession kSession = kContainer.newKieSession("ksession-rules");
            kSession.addEventListener(new RuleRuntimeEventListener() {
                public void objectInserted(ObjectInsertedEvent event) {
                    System.out.println("Object inserted \n"
                            + event.getObject().toString());
                }
                public void objectUpdated(ObjectUpdatedEvent event) {
                    System.out.println("Object was updated \n"
                        + "new Content \n" + event.getObject().toString());
                }
                public void objectDeleted(ObjectDeletedEvent event) {
                    System.out.println("Object retracted \n"
                        + event.getOldObject().toString());
                }
            });

            kSession.addEventListener(new DefaultAgendaEventListener() {
                public void matchCreated(MatchCreatedEvent event) {
                    System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " can be fired in agenda");
                }
                public void matchCancelled(MatchCancelledEvent event) {
                    System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " cannot b in agenda");
                }
                public void beforeMatchFired(BeforeMatchFiredEvent event) {
                    System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " will be fired");
                }
                public void afterMatchFired(AfterMatchFiredEvent event) {
                    System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " has be fired");
                }
	    });

            // go !
            // TimeThreshold time1 = new TimeThreshold();
            // kSession.insert(time1);
            // TempThreshold temp1 = new TempThreshold();
            // kSession.insert(temp1);
            // CountThreshold cnt1 = new CountThreshold();
            // kSession.insert(cnt1);
            TempReading r1 = new TempReading();
            r1.setTemp(35);
            r1.setTimestamp("20191001:000000000");
            kSession.insert(r1);
            TempReading r2 = new TempReading();
            r2.setTemp(35);
            r2.setTimestamp("20191001:000100000");
            // Thread.sleep(1000);
            kSession.insert(r2);
            kSession.fireAllRules();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static class TimeThreshold {
        public static final String max = "2m";
    }

    public static class TempThreshold {
        public static final int max = 35;
    }

    public static class CountThreshold {
        public static final int max = 2;
    }

    public static class TempReading {

        private static final Logger LOGGER = LoggerFactory.getLogger(TempReading.class);
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMdd:HHmmssSSS");

        private int temp;
        private Date timestamp;

        public int getTemp() {
            return this.temp;
        }

        public void setTemp(int temp) {
            this.temp = temp;
        }

        public Date getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(String eventTimestamp) {
            try {
                this.timestamp = DATE_FORMAT.parse(eventTimestamp.trim());
	    } catch (ParseException pe) {
             LOGGER.error("Error parsing timestamp: " + eventTimestamp);
         }

        }
    }
}
