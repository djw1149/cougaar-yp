/*
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.yp;

// import all of uddi4j
import org.uddi4j.*;
import org.uddi4j.client.*;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.assertion.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.request.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class YPTest {
  public static void main(String[] arg) {
    YPTest ypt = new YPTest();
    ypt.execute();
    System.exit(0);
  }

  private void execute() {
    // normally, we'd do something like:
    //YPService yps = (YPService) myservicebroker.getService(this, YPService.class);
    // but so that we can test this in isolation:
    // hack to compile
    YPService yps = null;  // new FakeYPServer();

    String company = "%foo%";

    UDDIProxy proxy = yps.getYP(null); // default the context
    // MessageAddress serverName = "MyCommunity";
    // UDDIProxy proxy = yps.getYP(serverName); // zero arguments = default case

    // no need to set the inquiryURL or publishURL

    Vector names = new Vector();
    names.add(new Name("S"));

    // Setting FindQualifiers to 'caseSensitiveMatch'
    FindQualifiers findQualifiers = new FindQualifiers();
    Vector qualifier = new Vector();
    qualifier.add(new FindQualifier("caseSensitiveMatch"));
    findQualifiers.setFindQualifierVector(qualifier);

    try {
      // Find businesses by name
      // And setting the maximum rows to be returned as 5.
      BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,5);

      Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
      for (int i = 0; i < businessInfoVector.size(); i++) {
        BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);

        // Print name for each business
        System.out.println(businessInfo.getNameString());
      }

      // Handle possible errors
    } catch (UDDIException e) {
      DispositionReport dr = e.getDispositionReport();
      if (dr!=null) {
        System.out.println("UDDIException faultCode:" + e.getFaultCode() +
                           "\n operator:" + dr.getOperator() +
                           "\n generic:"  + dr.getGeneric() +
                           "\n errno:"    + dr.getErrno() +
                           "\n errCode:"  + dr.getErrCode() +
                           "\n errInfoText:" + dr.getErrInfoText());
      }
      e.printStackTrace();

      // Catch any other exception that may occur
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}    
  /*
    // this was from the original JAXR code - preserved because I want to 
    // add asynchronous mode to the UDDI interface.
    {
    try {
      // YPQuery implements BusinessQueryManager
      q.findOrganizations(fQualifiers, names, null, null, null, null);

      // blocking wait
      {
        YPResponse r = yps.submitQuery(q, null);
        try {
          r.waitForIsAvailable();
        } catch (InterruptedException ie) {}
        report(r);
      }

      // polling wait
      {
        YPResponse r = yps.submitQuery(q, null);
        while (! r.isAvailable()) {
          try {
            Thread.sleep(1000);       // wait a sec
          } catch (InterruptedException ie) {}
        }
        report(r);
      }

      // callback
      {
        Callback callback = new Callback();
        YPResponse r = yps.submitQuery(q, callback);
        //r.addCallback(callback); // will callback immediately if already has the answer
        try {
          callback.waitForCallback();
          report(r);
        } catch (InterruptedException ie) { }
      }
    } catch (JAXRException je) {
      je.printStackTrace();
    }
    // done
  }

  static class Callback implements Runnable {
    private boolean called = false;
    public synchronized void run() { called = true; this.notify(); }
    public synchronized boolean isCalled() { return called; }
    public synchronized void waitForCallback() throws InterruptedException
    { if (!called) this.wait(); }
    public synchronized void waitForCallback(long timeout) throws InterruptedException
    { if (!called) this.wait(timeout); }
  }
  */
