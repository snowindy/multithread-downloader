package com.github.snowindy.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.snowindy.download.MultiThreadDownloaderTest;
import com.github.snowindy.download.RangeDownloaderTest;
import com.github.snowindy.util.HttpUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({ RangeDownloaderTest.class, MultiThreadDownloaderTest.class, HttpUtilsTest.class })
public class IntegrationTests {

}
