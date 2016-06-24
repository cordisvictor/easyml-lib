package net.sourceforge.easyml.marshalling.dtd;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Base64StrategyTest.class,
    BooleanStrategyTest.class,
    DateStrategyTest.class,
    DoubleStrategyTest.class,
    IntStrategyTest.class,
    StringStrategyTest.class
})
public class TestSuite {
}
