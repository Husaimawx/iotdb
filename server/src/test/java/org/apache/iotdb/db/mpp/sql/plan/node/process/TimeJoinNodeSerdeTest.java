/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.mpp.sql.plan.node.process;

import org.apache.iotdb.common.rpc.thrift.TConsensusGroupId;
import org.apache.iotdb.common.rpc.thrift.TConsensusGroupType;
import org.apache.iotdb.common.rpc.thrift.TRegionReplicaSet;
import org.apache.iotdb.db.exception.metadata.IllegalPathException;
import org.apache.iotdb.db.metadata.path.MeasurementPath;
import org.apache.iotdb.db.mpp.sql.plan.node.PlanNodeDeserializeHelper;
import org.apache.iotdb.db.mpp.sql.planner.plan.node.PlanNodeId;
import org.apache.iotdb.db.mpp.sql.planner.plan.node.process.TimeJoinNode;
import org.apache.iotdb.db.mpp.sql.planner.plan.node.source.SeriesScanNode;
import org.apache.iotdb.db.mpp.sql.statement.component.OrderBy;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.read.filter.TimeFilter;
import org.apache.iotdb.tsfile.read.filter.ValueFilter;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TimeJoinNodeSerdeTest {
  @Test
  public void testSerializeAndDeserialize() throws IllegalPathException {
    SeriesScanNode seriesScanNode1 =
        new SeriesScanNode(
            new PlanNodeId("TestSeriesScanNode"),
            new MeasurementPath("root.sg.d1.s1", TSDataType.INT32),
            Sets.newHashSet("s1", "s2"),
            OrderBy.TIMESTAMP_DESC,
            TimeFilter.gt(100),
            null,
            100,
            100,
            new TRegionReplicaSet(
                new TConsensusGroupId(TConsensusGroupType.DataRegion, 1), new ArrayList<>()));
    SeriesScanNode seriesScanNode2 =
        new SeriesScanNode(
            new PlanNodeId("TestSeriesScanNode"),
            new MeasurementPath("root.sg.d1.s2", TSDataType.INT32),
            Sets.newHashSet("s1", "s2"),
            OrderBy.TIMESTAMP_DESC,
            null,
            ValueFilter.gt(100),
            100,
            100,
            new TRegionReplicaSet(
                new TConsensusGroupId(TConsensusGroupType.DataRegion, 1), new ArrayList<>()));

    TimeJoinNode timeJoinNode =
        new TimeJoinNode(new PlanNodeId("TestTimeJoinNode"), OrderBy.TIMESTAMP_ASC);
    timeJoinNode.addChild(seriesScanNode1);
    timeJoinNode.addChild(seriesScanNode2);

    ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
    timeJoinNode.serialize(byteBuffer);
    byteBuffer.flip();
    assertEquals(PlanNodeDeserializeHelper.deserialize(byteBuffer), timeJoinNode);
  }
}
