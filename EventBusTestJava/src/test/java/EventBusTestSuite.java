import org.greenrobot.eventbus.EventBusBasicTest;
import org.greenrobot.eventbus.EventBusBuilderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// Gradle does not pick up tests in src dir, so help Gradle with a suite
// (Tests are in main src so we can use them for Android too)
@RunWith(Suite.class)
@SuiteClasses({
        EventBusBasicTest.class,
        EventBusBuilderTest.class
})
public class EventBusTestSuite {
}
