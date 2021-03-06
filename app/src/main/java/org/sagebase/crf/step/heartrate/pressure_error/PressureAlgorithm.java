/*
 *    Copyright 2018 Sage Bionetworks
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package org.sagebase.crf.step.heartrate.pressure_error;

import org.sagebase.crf.step.active.HeartBeatSample;
import org.sagebase.crf.step.heartrate.ErrorAlgorithm;

public class PressureAlgorithm implements ErrorAlgorithm {

    public void getPreviousState() {

    }

    // Get all the rgb stuff, then look at the gyroscope stuff
    public static double algorithm() {
        if(HeartBeatSample.isPressureExcessive()) {
            return 1.0;
        }
        else{
            return 0.0;
        }
    }
}
