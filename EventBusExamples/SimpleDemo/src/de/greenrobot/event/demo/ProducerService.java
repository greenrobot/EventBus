/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.greenrobot.event.demo;

import de.greenrobot.event.EventBus;
import android.app.IntentService;
import android.content.Intent;

public class ProducerService extends IntentService {

    public ProducerService() {
        super("ProducerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Make some long computation here, we just sleep the thread
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            // We don't care of this
        }

        EventBus.getDefault().post("Message from ProduceService!");

    }

}
