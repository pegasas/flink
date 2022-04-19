/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.connectors.kinesis.util;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.testutils.MiniClusterResource;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/** Test for {@link JobManagerWatermarkTracker}. */
public class JobManagerWatermarkTrackerTest {

    @ClassRule
    public static final MiniClusterResource FLINK =
            new MiniClusterResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberTaskManagers(1)
                            .setNumberSlotsPerTaskManager(1)
                            .build());

    @Test
    public void testUpateWatermark() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new TestSourceFunction(new JobManagerWatermarkTracker("fakeId")))
                .addSink(new SinkFunction<Integer>() {});
        env.execute();
    }

    private static class TestSourceFunction extends RichSourceFunction<Integer> {

        private final JobManagerWatermarkTracker tracker;

        public TestSourceFunction(JobManagerWatermarkTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            tracker.open(getRuntimeContext());
        }

        @Override
        public void run(SourceContext<Integer> ctx) {
            Assert.assertEquals(998, tracker.updateWatermark(998));
            Assert.assertEquals(999, tracker.updateWatermark(999));
        }

        @Override
        public void cancel() {}
    }
}
