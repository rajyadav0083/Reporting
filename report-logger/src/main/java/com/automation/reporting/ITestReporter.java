package com.automation.reporting;

public interface ITestReporter {

    void startFeature(String featureName);

    void startScenario(String featureName, String scenarioName);

    void logStep(String stepDetail);

    void logSubStep(String subStepDetail);

    void logStepWithScreenshot(String stepDetail, String screenshotPath);

    void logSubStepWithScreenshot(String subStepDetail, String screenshotPath);

    void markScenarioPassed();

    void markScenarioFailed(String errorMessage, String screenshotPath);

    void markScenarioSkipped(String reason);

    void flush();

	void logStepFail(String errorMessage, String screenshotPath);

	void logSubStepFail(String errorMessage, String screenshotPath);
}
