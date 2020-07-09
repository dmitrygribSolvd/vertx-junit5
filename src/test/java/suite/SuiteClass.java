package suite;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

//junit4's Runner is using here for launching all junit5-tests
@RunWith(JUnitPlatform.class)
@SelectPackages({"examples","io.vertx.junit5"})
public class SuiteClass {}
