package com.sample;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        InputStreamReader in= new InputStreamReader(System.in);
        BufferedReader input = new BufferedReader(in);
 
        String str;

        while (true) {
            try {
                str = input.readLine();
            } catch (IOException e) {
                break;
            }
            if (str == null) {
                break;
            }
            JSONObject j1 = getLine(str);
            String s1 = (String) j1.get("RULEAPI");
            if (s1 == null) {
                String t1 = (String) j1.get("temp");
                String ts1 = (String) j1.get("timestamp");
                if (t1 != null && ts1 == null) {
                    TempReading r1 = new TempReading(Integer.parseInt(t1), ts1);
                    // session.fireAllRules();
		}
            } else {
                String name1 = (String) j1.get("name");
                String type1 = (String) j1.get("type");
                String cond1 = (String) j1.get("cond");
                if (typ1 == "num") {
                    String flds[] = cond1.split()
                } else if (typ1 == "str") {
                    String flds[] = cond1.split()
                } else if (typ1 == "logical") {
                    String flds[] = cond1.split()
                } else if (typ1 == "interval") {
                    String flds[] = cond1.split()
                }
                System.out.println("Type: " + typ1);
            }
        }
    }

    public void build_rule() {
        try {
	    KieContainer container = new DroolsTest().build(KieServices.Factory.get());
	    KieSession session = container.newKieSession();
            TempThreshold temp1 = new TempThreshold(35);
            session.insert(temp1);
            CountThreshold cnt1 = new CountThreshold(2);
            session.insert(cnt1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void process_msg() {
        // session.insert(r1);
    }

    public static JSONObject getLine(String str) {
        JSONObject j1 = null;
        try {
            j1 = (JSONObject) JSONValue.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return j1;
    }

    public KieContainer build(KieServices kieServices, String timeLimit) {
	KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
	ReleaseId rid = kieServices.newReleaseId("com.sample.drools", "model-test", "1.0-SNAPSHOT");
	kieFileSystem.generateAndWritePomXML(rid);

	addRule(kieFileSystem, timeLimit);

	KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
	kieBuilder.buildAll();
	if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
	    throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
	}
		
	return kieServices.newKieContainer(rid);
    }

    private void getRule(String name, String type, String cond) {
        if (cond == "simple") {
            return getSimpleRule(name, cond)
        } else if (cond == "logical") {
            return getLogicalRule(name, cond)
        } else if (cond == "interval") {
            return getIntervalRule(name, cond)
        } else {
            assert 0
        }

    private void getSimpleRule(String name, String cond) {
        String flds[] = cond.split(" ", 1)
        String ruleStr = "rule " + name + " when\n" +
            "Map( this[\"" + flds[0] + "\"] " + flds[1] + " )\n" + 
            "then\n" +
            "insertLogical( new Fact(\"" + name + "\") );
            "end";
        return ruleStr
    }
    private void getComplexRule(String name, String cond) {
        String flds[] = cond.split(" ")
        String ruleStr = "rule " + name + " when\n" +
            "exists( Fact(\"" + flds[0] + "\") " + flds[1] + Fact(\"" + flds[2] + "\") )\n" + 
            "then\n" +
            "insertLogical( new Fact(\"" + name + "\") );
            "end";
        return ruleStr
    }


    private void addRule(KieFileSystem kieFileSystem, String timeLimit) {
        String ruleStr = "import " + TempThreshold.class.getCanonicalName() + ";\n" +
            "import " + CountThreshold.class.getCanonicalName() + ";\n" +
            "import " + TempReading.class.getCanonicalName() + ";\n" +
            "declare TempReading\n" +
            "@role( event )\n" +
            "@timestamp( timestamp.getTime() )\n" +
            "end\n" +
            "rule R when\n" +
            "$t1 : TempThreshold( )\n" + 
            "$c1 : CountThreshold( )\n" + 
            "$m1 : TempReading( temp >= $t1.max )\n" + 
            "Number( doubleValue >= $c1.max ) from accumulate(\n" +
            "TempReading( temp >= $t1.max, this after[0s," + timeLimit + "] $m1 ),\n" +
	    "init( double total = 0; ),\n" +
            "action( total += 1; ),\n" +
            "reverse( total -= 1; ),\n" +
            "result( total ))\n" +
            "then\n" +
            "System.out.println(\"Temp over max\");\n" +
            "end";

        kieFileSystem.write("src/main/resources/rule-1.drl", ruleStr);
    }

    public static class TempThreshold {
	public int max;

        TempThreshold(int val) {
            max = val;
        }
    }

    public static class CountThreshold {
        public int max;

        CountThreshold(int val) {
            max = val;
        }
    }

    public static class TempReading {

        private static final Logger LOGGER = LoggerFactory.getLogger(TempReading.class);
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMdd:HHmmssSSS");

        private int temp;
        private Date timestamp;

        TempReading(int t1, String ts1) {
            temp = t1;
            setTimestamp(ts1);
        }

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
	    } catch (Exception pe) {
             LOGGER.error("Error parsing timestamp: " + eventTimestamp);
         }
        }
    }
}
