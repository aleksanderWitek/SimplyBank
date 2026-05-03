package com.alex;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("SimplyBank — Integration Tests")
@SelectPackages("com.alex")
@IncludeClassNamePatterns(".*IntegrationTest")
public class IntegrationTestSuite {
}
