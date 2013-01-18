package com.github.snowindy.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.snowindy.download.MultiThreadDownloaderTests;
import com.github.snowindy.download.RangeDownloaderTests;
import com.github.snowindy.util.HttpUtilsTests;

@RunWith(Suite.class)
@SuiteClasses({ RangeDownloaderTests.class, MultiThreadDownloaderTests.class, HttpUtilsTests.class })
public class IntegrationTests {

}
