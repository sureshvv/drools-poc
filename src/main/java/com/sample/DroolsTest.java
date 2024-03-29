package com.sample;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; 
import java.util.Map;
import java.util.Iterator;

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
	List<String> all_rules = new ArrayList<String>();
        KieSession session = null;
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
            if (str.charAt(0) == '#') {
                continue;
            }
            JSONObject j1 = getLine(str.trim());
            String s1 = (String) j1.get("RULEAPI");
            if (s1 == null) {
                String ts1 = (String) j1.get("timestamp");
                if (ts1 == null) {
                    if (session != null) {
                        session.insert(j1);
		    }
                } else {
                    String t1 = (String) j1.get("temp");
                    Reading r1 = new Reading(Integer.parseInt(t1), ts1);
                    if (session != null) {
                        session.insert(r1);
		    }
                }
                session.fireAllRules();
            } else if (s1.equals("__build")) {
	        KieContainer container = build_rules(all_rules);
	        session = container.newKieSession();
            } else {
                String name = s1;
                String type = (String) j1.get("type");
                String cond = (String) j1.get("cond");
                String ruleStr = getRule(name, type, cond);
                all_rules.add(ruleStr);
            }
        }
    }

    public static KieContainer build_rules(List<String> rules) {
        try {
	    KieServices k1 = KieServices.Factory.get();
	    KieFileSystem kf1 = k1.newKieFileSystem();
	    ReleaseId rid = k1.newReleaseId("com.sample.drools", "model-test", "1.0-SNAPSHOT");
	    kf1.generateAndWritePomXML(rid);
	    Iterator iterator = rules.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                i += 1;
                String s1 = (String) iterator.next();
                kf1.write("src/main/resources/rule-" + i + ".drl", s1);
            }
            i += 1;
            String s1 = (String) getFinalRule();
            kf1.write("src/main/resources/rule-" + i + ".drl", s1);

	    KieBuilder kieBuilder = k1.newKieBuilder(kf1);
	    kieBuilder.buildAll();
	    if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
	        throw new RuntimeException("Build Errors:\n" +
                                           kieBuilder.getResults().toString());
            }
	    return k1.newKieContainer(rid);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
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

    private static String getRule(String name, String type, String cond) {
        if (type.equals("simple")) {
            return getSimpleRule(name, cond);
        } else if (type.equals("complex")) {
            return getComplexRule(name, cond);
        } else if (type.equals("interval")) {
            return getIntervalRule(name, cond);
        } else {
	    throw new RuntimeException("Invalid condition type: " + type);
        }
    }

    private static String getSimpleRule(String name, String cond) {
        String flds[] = cond.split(" ", 2);
        String ruleStr = "import " + Fact.class.getCanonicalName() + ";\n" +
            "import org.json.simple.JSONObject;\n" +
            "rule " + name + " when\n" +
            "JSONObject( this[\"" + flds[0] + "\"] " + flds[1] + " )\n" + 
            "then\n" +
            "Fact f1 = new Fact(\"" + name + "\");\n" +
            "insertLogical( f1 );\n" +
            "end";
        return ruleStr;
    }

    private static String getComplexRule(String name, String cond) {
        String flds[] = cond.split(" ");
        String mvel = "dialect \"mvel\"\n";
        String ruleStr = "import " + Fact.class.getCanonicalName() + ";\n" +
            "rule " + name + " when\n" +
            "exists( Fact(name == \"" + flds[0] + "\") ) " + flds[1] + " " +
            "exists( Fact(name == \"" + flds[2] + "\") )" +
            "then\n" +
            "Fact f1 = new Fact(\"" + name + "\");\n" +
            "insertLogical( f1 );\n" +
            "end";
        return ruleStr;
    }

    private static String getIntervalRule(String name, String cond) {
        String flds[] = cond.split(" ");
        String ruleStr = "import " + Reading.class.getCanonicalName() + ";\n" +
            "import " + Fact.class.getCanonicalName() + ";\n" +
            "declare Reading\n" +
            "@role( event )\n" +
            "@timestamp( timestamp.getTime() )\n" +
            "end\n" +
            "rule " + name + " when\n" +
            "m1 : Reading( " + flds[0] + " " + flds[1] + " " + flds[2] + " )\n" + 
            "Number( doubleValue >= " + flds[3] + " ) from accumulate(\n" +
	    "Reading( " + flds[0] + " " + flds[1] + " "  + flds[2] + ", " +
	    "this after[0s," + flds[4] + "] m1 ),\n" +
	    "init( double total = 0; ),\n" +
            "action( total += 1; ),\n" +
            "reverse( total -= 1; ),\n" +
            "result( total ))\n" +
            "then\n" +
            "Fact f1 = new Fact(\"" + name + "\");\n" +
            "insertLogical( f1 );\n" +
            "end";
        return ruleStr;
    }

    private static String getFinalRule() {
        String ruleStr = "import " + Fact.class.getCanonicalName() + ";\n" +
            "rule final when\n" +
            "f1 : Fact( )\n" + 
            "then\n" +
            "System.out.println(\"RULE: \" + f1.getName() + \" fired\");\n" +
            "end";
        return ruleStr;
    }

    public static class Fact {
        private String name;

        public Fact(String s1) {
            name = s1;
        }
        public String getName() {
            return this.name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Reading {

        private static final Logger LOGGER = LoggerFactory.getLogger(Reading.class);
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMdd:HHmmssSSS");

        private int temp;
        private Date timestamp;

        Reading(int t1, String ts1) {
            temp = t1;
            if (ts1 != null) {
                setTimestamp(ts1);
	    }
        }

        public int getTemp() {
            return this.temp;
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
