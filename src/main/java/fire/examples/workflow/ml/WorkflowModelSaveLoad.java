package fire.examples.workflow.ml;

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

import fire.context.JobContext;
import fire.nodes.dataset.NodeDatasetStructured;
import fire.nodes.ml.*;
import fire.util.spark.CreateSparkContext;
import fire.workflowengine.ConsoleWorkflowContext;
import fire.workflowengine.DatasetType;
import fire.workflowengine.Workflow;
import fire.workflowengine.WorkflowContext;
import fire.context.JobContextImpl;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by jayantshekhar
 */
public class WorkflowModelSaveLoad {

    //--------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        // create spark context
        JavaSparkContext ctx = CreateSparkContext.create(args);
        // create workflow context
        WorkflowContext workflowContext = new ConsoleWorkflowContext();

        JobContext jobContext = new JobContextImpl(ctx, workflowContext);

        modelsavewf(jobContext);

        // stop the context
        ctx.stop();
    }


    //--------------------------------------------------------------------------------------

    // modelsave workflow
    private static void modelsavewf(JobContext jobContext) throws Exception {

        Workflow wf = new Workflow();
        // execute the workflow

        NodeDatasetStructured csv1 = new NodeDatasetStructured(1, "csv1 node", "data/cars.csv", DatasetType.CSV, ",",
                "c1 c2 c3 c4", "double double double double",
                "numeric numeric numeric numeric");

        wf.addNode(csv1);

        NodeVectorAssembler nva = new NodeVectorAssembler(4, "nva");
        nva.inputCols = new String []{"c1", "c2", "c3", "c4"};
        nva.outputCol = "features";
        wf.addLink(csv1, nva);

        //default value of k is 2, from above analysis it is clear there are 8 different patterns in data.
        NodeKMeans nkm = new NodeKMeans(5, "nkm");
        nkm.k = 12;
        nkm.maxIter = 10;
        nkm.tol = 1.0e-6;
        nkm.featuresCol = "features";
        wf.addLink(nva, nkm);

        NodeModelSave save = new NodeModelSave(6, "model save");
        save.path = "modelsave";
        save.overwrite = true;
        wf.addLink(nkm, save);

        NodeModelLoad load = new NodeModelLoad(10, "model load");
        load.path = "modelsave";
        wf.addLink(save, load);

        wf.execute(jobContext);

    }
}