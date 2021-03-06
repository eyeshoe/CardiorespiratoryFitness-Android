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

package org.sagebase.crf.step.heartrate.camera_error;

import org.sagebase.crf.step.active.HeartBeatSample;
import org.sagebase.crf.step.heartrate.ErrorAlgorithm;

public class CameraAlgorithm implements ErrorAlgorithm {

    public void getPreviousState() {

    }

    public static double algorithm() {
        boolean cameraResult = HeartBeatSample.isCoveringLens();
        if (cameraResult) {
            return 1.0;
        }
        return 0.0;
    }
}
