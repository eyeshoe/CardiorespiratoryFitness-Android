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

package org.sagebase.crf.step.heartrate.confidence_error;

import org.sagebase.crf.step.heartrate.OutputSolution;

public class ConfidenceSolution implements OutputSolution {

    public void displayUI() {
        System.out.println("We couldn't get a good read on your heartrate." +
                "Please retake this test. ");

        // Points the user back to the retake test button
    }
}