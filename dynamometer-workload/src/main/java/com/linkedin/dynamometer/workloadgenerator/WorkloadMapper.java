/**
 * Copyright 2017 LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.dynamometer.workloadgenerator;

import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;


/**
 * Represents the base class for a generic workload-generating mapper. By default, it will expect to use
 * {@link TimedInputFormat} as its {@link InputFormat}. Subclasses requiring a reducer or expecting
 * a different {@link InputFormat} should override the {@link #configureJob(Job)} method.
 */
public abstract class WorkloadMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
  /**
   * Get the description of the behavior of this mapper.
   */
  public abstract String getDescription();

  /**
   * Get a list of the description of each configuration that this mapper accepts.
   */
  public abstract List<String> getConfigDescriptions();

  /**
   * Verify that the provided configuration contains all configurations
   * required by this mapper.
   */
  public abstract boolean verifyConfigurations(Configuration conf);

  /**
   * Setup input and output formats and optional reducer.
   */
  public void configureJob(Job job) {
    job.setInputFormatClass(TimedInputFormat.class);

    job.setNumReduceTasks(0);
    job.setOutputKeyClass(NullWritable.class);
    job.setOutputValueClass(NullWritable.class);
    job.setOutputFormatClass(NullOutputFormat.class);
  }
}
