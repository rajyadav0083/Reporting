package com.automation.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;

public class ExtentReporter implements ITestReporter {

    private static ExtentReports extent = ExtentManager.getInstance();

    // Map: feature name → feature node
    private static ConcurrentHashMap<String, ExtentTest> featureMap = new ConcurrentHashMap<>();

    // Thread-local scenario node for parallel safety
    private static ThreadLocal<ExtentTest> scenario = new ThreadLocal<>();
    @Override
    	// Start or reuse Feature
	  public synchronized void startFeature(String featureName) {
	  featureMap.computeIfAbsent(featureName, name ->
	  extent.createTest(name)); }
	    
    @Override
    public synchronized void startScenario(String featureName, String scenarioName) {
        ExtentTest featureNode = featureMap.get(featureName);
        if (featureNode == null) {
            featureNode = extent.createTest(featureName);
            featureMap.put(featureName, featureNode);
        }
        ExtentTest scenarioNode = featureNode.createNode(scenarioName);
        scenario.set(scenarioNode);
    }

    @Override
    // Log green highlighted Step
    public synchronized void logStep(String stepDetail) {
        scenario.get().pass(
            MarkupHelper.createLabel("<b>STEP: </b>"+stepDetail, ExtentColor.GREEN)            
        );
    }
    @Override
    // Log Step with Screenshot (green label)
    public synchronized void logStepWithScreenshot(String stepDetail, String screenshotPath) {
        try {
            scenario.get().info(
                (Throwable) MarkupHelper.createLabel("<b>STEP: </b>"+stepDetail, ExtentColor.GREEN),
                MediaEntityBuilder.createScreenCaptureFromPath(copyScreenshot(screenshotPath)).build()
            );
        } catch (IOException e) {
            scenario.get().warning("Screenshot failed: " + e.getMessage());
        }
    }
    @Override
    // Log green highlighted Step
    public synchronized void logStepFail(String errorMessage,String screenshotPath) {
    	try {
            if (screenshotPath != null) {
                scenario.get().log(Status.FAIL,
                        MarkupHelper.createLabel("<b>STEP: </b>"+errorMessage, ExtentColor.RED),
                        MediaEntityBuilder.createScreenCaptureFromPath(copyScreenshot(screenshotPath)).build());
            } else {
                scenario.get().log(Status.FAIL,
                        MarkupHelper.createLabel(errorMessage, ExtentColor.RED));
            }
        } catch (IOException e) {
            scenario.get().warning("Failed to attach failure screenshot: " + e.getMessage());
        }
    }
    @Override
    // Log green highlighted Sub-step
    public synchronized void logSubStep(String subStepDetail) {
        scenario.get().info(
                MarkupHelper.createLabel(subStepDetail, ExtentColor.BLUE));
    }

    @Override
    // Log Sub-step with Screenshot 
    public synchronized void logSubStepWithScreenshot(String subStepDetail, String screenshotPath) {
        try {
            scenario.get().info(subStepDetail,
                MediaEntityBuilder.createScreenCaptureFromPath(copyScreenshot(screenshotPath)).build()
            );
        } catch (IOException e) {
            scenario.get().warning("Screenshot failed: " + e.getMessage());
        }
    }
    @Override
    // Log green highlighted Step
    public synchronized void logSubStepFail(String errorMessage,String screenshotPath) {
    	try {
            if (screenshotPath != null) {
                scenario.get().log(Status.FAIL,
                        MarkupHelper.createLabel("<b>STEP: </b>"+errorMessage, ExtentColor.RED),
                        MediaEntityBuilder.createScreenCaptureFromPath(copyScreenshot(screenshotPath)).build());
            } else {
                scenario.get().log(Status.FAIL,
                        MarkupHelper.createLabel(errorMessage, ExtentColor.RED));
            }
        } catch (IOException e) {
            scenario.get().warning("Failed to attach failure screenshot: " + e.getMessage());
        }
    }
    
    @Override
    // Mark Scenario as Passed
    public synchronized void markScenarioPassed() {
        scenario.get().log(Status.PASS,
                MarkupHelper.createLabel("Scenario PASSED successfully", ExtentColor.GREEN));
    }

    // Mark Scenario as Failed
    public synchronized void markScenarioFailed(String errorMessage, String screenshotPath) {
        try {
            if (screenshotPath != null) {
                scenario.get().log(Status.FAIL,
                        MarkupHelper.createLabel(errorMessage, ExtentColor.RED),
                        MediaEntityBuilder.createScreenCaptureFromPath(copyScreenshot(screenshotPath)).build());
            } else {
                scenario.get().log(Status.FAIL,
                        MarkupHelper.createLabel(errorMessage, ExtentColor.RED));
            }
        } catch (IOException e) {
            scenario.get().warning("Failed to attach failure screenshot: " + e.getMessage());
        }
    }
    @Override
    // Mark Scenario as Skipped
    public synchronized void markScenarioSkipped(String reason) {
        scenario.get().log(Status.SKIP,
                MarkupHelper.createLabel(reason, ExtentColor.ORANGE));
    }
    @Override
    // Flush Report
    public synchronized void flush() {
        extent.flush();
    }

    // Utility: Copy screenshot to /reports/screenshots
    private static String copyScreenshot(String screenshotPath) throws IOException {
        String reportDir = System.getProperty("user.dir") + "/reports/screenshots/";
        File destDir = new File(reportDir);
        if (!destDir.exists()) destDir.mkdirs();

        File srcFile = new File(screenshotPath);
        String destFilePath = reportDir + System.currentTimeMillis() + "_" + srcFile.getName();
        Files.copy(srcFile.toPath(), new File(destFilePath).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return destFilePath;
    }
}
