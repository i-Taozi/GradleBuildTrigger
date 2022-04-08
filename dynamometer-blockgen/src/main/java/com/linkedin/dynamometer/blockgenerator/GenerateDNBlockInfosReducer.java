/**
 * Copyright 2017 LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.dynamometer.blockgenerator;

import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;


/**
 * This Reducer class generates a simple text file for each DN, listing the
 * blocks to be generated.
 *
 * Input: {@link BlockInfo}'s from {@link XMLParserMapper}
 *
 * Output: A text file named as dni-XXX, where i is the ID of the DN and
 * XXX is a reducer ID. Each line in the file is in format:
 * blockID,blockGenStamp,blockSize
 */
public class GenerateDNBlockInfosReducer extends Reducer<IntWritable, BlockInfo, NullWritable, Text> {
  private static final Log LOG = LogFactory.getLog(GenerateDNBlockInfosReducer.class);

  private MultipleOutputs<NullWritable, Text> multiOutputs;

  @Override
  public void setup(Reducer<IntWritable, BlockInfo, NullWritable, Text>.Context context) {
    multiOutputs = new MultipleOutputs<>(context);
  }

  @Override
  public void cleanup(Context context) throws IOException, InterruptedException {
    multiOutputs.close();
    multiOutputs = null;
  }

  @Override
  public void reduce(IntWritable key, Iterable<BlockInfo> values, Context context)
      throws IOException, InterruptedException {
    long blockIndex = 0;
    int datanodeId = key.get();
    String dnFile = "dn" + datanodeId + "-a-" + context.getTaskAttemptID().getId();
    Iterator<BlockInfo> it = values.iterator();
    long startTimestamp = System.currentTimeMillis();
    long endTimestamp;

    Path baseOutputPath = FileOutputFormat.getOutputPath(context);
    String fullPath = new Path(baseOutputPath, dnFile).toString();

    Text out = new Text();
    while (it.hasNext()) {
      BlockInfo blockInfo = new BlockInfo(it.next());
      String blockLine = blockInfo.getBlockId() + "," + blockInfo.getBlockGenerationStamp() + "," + blockInfo.getSize();
      out.set(blockLine);
      multiOutputs.write(NullWritable.get(), out, fullPath);

      blockIndex++;

      //Report progress for every 1000 blocks
      if (blockIndex % 1000 == 0) {
        context.progress();
        endTimestamp = System.currentTimeMillis();
        System.out.println("Time taken to process 1000 records in ms:" + (endTimestamp - startTimestamp));
        startTimestamp = endTimestamp;
      }
    }

    LOG.info("Number of blocks processed:" + blockIndex);
  }
}