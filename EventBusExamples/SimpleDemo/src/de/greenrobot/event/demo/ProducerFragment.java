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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ProducerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_producer, null);

        final EditText editText = (EditText) view.findViewById(R.id.message);
        Button sendMessage = (Button) view.findViewById(R.id.send_message);
        sendMessage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                EventBus.getDefault().post(message);
            }
        });

        Button startService = (Button) view.findViewById(R.id.start_service);
        startService.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ProducerService.class);
                getActivity().startService(i);
            }
        });

        return view;
    }

}
