package com.smartexpense;

import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Listener to print test case function names with pass/fail status
 * 
 * File Location: src/test/java/com/smartexpense/TestExecutionListener.java
 */
public class TestExecutionListener implements ITestListener {

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println(result.getMethod().getMethodName() + " - PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println(result.getMethod().getMethodName() + " - FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println(result.getMethod().getMethodName() + " - SKIPPED");
    }
}
