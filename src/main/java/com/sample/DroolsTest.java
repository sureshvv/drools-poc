package com.sample;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.KieBuilder;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.builder.Message;
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
	    KieContainer container = new DroolsTest().build(KieServices.Factory.get());
	    KieSession session = container.newKieSession();
            // load up the knowledge base
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
            session.insert(r1);
            TempReading r2 = new TempReading();
            r2.setTemp(34);
            r2.setTimestamp("20191001:000100000");
            session.insert(r2);
            session.fireAllRules();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public KieContainer build(KieServices kieServices) {
	KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
	ReleaseId rid = kieServices.newReleaseId("com.sample.drools", "model-test", "1.0-SNAPSHOT");
	kieFileSystem.generateAndWritePomXML(rid);

	addRule(kieFileSystem);

	KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
	kieBuilder.buildAll();
	if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
	    throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
	}
		
	return kieServices.newKieContainer(rid);
    }

    private void addRule(KieFileSystem kieFileSystem) {
        String ruleStr = "import " + TimeThreshold.class.getCanonicalName() + ";\n" +
            "import " + TempThreshold.class.getCanonicalName() + ";\n" +
            "import " + CountThreshold.class.getCanonicalName() + ";\n" +
            "import " + TempReading.class.getCanonicalName() + ";\n" +
            "declare TempReading\n" +
            "@role( event )\n" +
            "@timestamp( timestamp.getTime() )\n" +
            "end\n" +
            "rule R when\n" +
            "$m1 : TempReading( temp >= 35 )\n" + 
            "Number( doubleValue >= 2 ) from accumulate(\n" +
            "TempReading( temp >= 35, this after[0s, 2m] $m1 ),\n" +
	    "init( double total = 0; ),\n" +
            "action( total += 1; ),\n" +
            "reverse( total -= 1; ),\n" +
            "result( total ))\n" +
            "then\n" +
            "System.out.println(\"Temp over max\");\n" +
            "end";

        kieFileSystem.write("src/main/resources/rule-1.drl", ruleStr);
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
