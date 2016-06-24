package net.sourceforge.easyml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    net.sourceforge.easyml.util.TestSuite.class,
    net.sourceforge.easyml.marshalling.dtd.TestSuite.class,
    StrategyRegistryTest.class,
    SecurityPolicyTest.class,
    XMLWriterTest.class,
    XMLReaderTest.class,
    EasyMLTest.class,
    MultiThreadEasyMLTest.class,
    ArrayCollectionsTest.class,
    BugsTest.class
})
public class AllTestSuite {
}
