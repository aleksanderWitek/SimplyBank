package com.alex;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("SimplyBank — Unit Tests")
@SelectPackages("com.alex")
@IncludeClassNamePatterns(".*Test")
@ExcludeClassNamePatterns(".*IntegrationTest")
public class UnitTestSuite {
}
