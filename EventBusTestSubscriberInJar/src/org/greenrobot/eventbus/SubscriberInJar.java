package org.greenrobot.eventbus;

import java.util.ArrayList;
import java.util.List;

/** Helper class used by test inside a jar. */
public class SubscriberInJar {
    List<String> collectedStrings = new ArrayList<String>();

    @Subscribe
    public void collectString(String string) {
        collectedStrings.add(string);
    }

    public List<String> getCollectedStrings() {
        return collectedStrings;
    }
}
