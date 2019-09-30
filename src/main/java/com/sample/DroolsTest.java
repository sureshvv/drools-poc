package com.sample;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

    public static final void main(String[] args) {
        try {
            // load up the knowledge base
	        KieServices ks = KieServices.Factory.get();
    	    KieContainer kContainer = ks.getKieClasspathContainer();
        	KieSession kSession = kContainer.newKieSession("ksession-rules");

            // go !
            TempThreshold temp1 = new TempThreshold();
            kSession.insert(temp1);
            CountThreshold cnt1 = new CountThreshold();
            kSession.insert(cnt1);
            TempReading r1 = new TempReading();
            r1.setTemp(35);
            kSession.insert(r1);
            TempReading r2 = new TempReading();
            r2.setTemp(34);
            kSession.insert(r2);
            TempReading r3 = new TempReading();
            r3.setTemp(34);
            kSession.insert(r3);
            TempReading r4 = new TempReading();
            r4.setTemp(34);
            kSession.insert(r4);
            TempReading r5 = new TempReading();
            r5.setTemp(34);
            kSession.insert(r5);
            kSession.fireAllRules();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static class TempThreshold {

        public static final int max = 35;

    }


    public static class CountThreshold {

        public static final int max = 2;

    }

    public static class TempReading {

        private int temp;

        public int getTemp() {
            return this.temp;
        }

        public void setTemp(int temp) {
            this.temp = temp;
        }

    }

}
