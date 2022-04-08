/**
 * Copyright 2017 LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.dynamometer.workloadgenerator;

import com.linkedin.dynamometer.workloadgenerator.audit.AuditCommandParser;
import com.linkedin.dynamometer.workloadgenerator.audit.AuditLogDirectParser;
import com.linkedin.dynamometer.workloadgenerator.audit.AuditLogHiveTableParser;
import com.linkedin.dynamometer.workloadgenerator.audit.AuditReplayMapper;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authorize.AuthorizationException;
import org.apache.hadoop.security.authorize.ImpersonationProvider;
import org.apache.htrace.commons.logging.Log;
import org.apache.htrace.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class TestWorkloadGenerator {
  private static final Log LOG = LogFactory.getLog(TestWorkloadGenerator.class);
  
  private Configuration conf;
  private MiniDFSCluster miniCluster;
  private FileSystem dfs;

  @Before
  public void setup() throws Exception {
    conf = new Configuration();
    conf.setClass(CommonConfigurationKeysPublic.HADOOP_SECURITY_IMPERSONATION_PROVIDER_CLASS,
        AllowUserImpersonationProvider.class, ImpersonationProvider.class);
    miniCluster = new MiniDFSCluster.Builder(conf).build();
    miniCluster.waitClusterUp();
    dfs = miniCluster.getFileSystem();
    dfs.mkdirs(new Path("/tmp"), new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL));
    dfs.setOwner(new Path("/tmp"), "hdfs", "hdfs");
  }

  @After
  public void tearDown() throws Exception {
    if (miniCluster != null) {
      miniCluster.shutdown();
      miniCluster = null;
    }
  }

  @Test
  public void testAuditWorkloadDirectParserWithOutput() throws Exception {
    String workloadInputPath =
        TestWorkloadGenerator.class.getClassLoader().getResource("audit_trace_direct").toString();
    String auditOutputPath = "/tmp/trace_output_direct";
    conf.set(AuditReplayMapper.INPUT_PATH_KEY, workloadInputPath);
    conf.set(AuditReplayMapper.OUTPUT_PATH_KEY, auditOutputPath);
    conf.setLong(AuditLogDirectParser.AUDIT_START_TIMESTAMP_KEY, 60*1000);
    testAuditWorkloadWithOutput(auditOutputPath);
  }

  @Test
  public void testAuditWorkloadHiveParserWithOutput() throws Exception {
    String workloadInputPath =
        TestWorkloadGenerator.class.getClassLoader().getResource("audit_trace_hive").toString();
    String auditOutputPath = "/tmp/trace_output_hive";
    conf.set(AuditReplayMapper.INPUT_PATH_KEY, workloadInputPath);
    conf.set(AuditReplayMapper.OUTPUT_PATH_KEY, auditOutputPath);
    conf.setClass(AuditReplayMapper.COMMAND_PARSER_KEY, AuditLogHiveTableParser.class, AuditCommandParser.class);
    testAuditWorkloadWithOutput(auditOutputPath);
  }

  /**
   * {@link ImpersonationProvider} that confirms the user doing the impersonating is the same as the user
   * running the MiniCluster.
   */
  private static class AllowUserImpersonationProvider extends Configured implements ImpersonationProvider {
    public void init(String configurationPrefix) {
      // Do nothing
    }
    public void authorize(UserGroupInformation user, String remoteAddress) throws AuthorizationException {
      try {
        if (!user.getRealUser().getShortUserName().equals(UserGroupInformation.getCurrentUser().getShortUserName())) {
          throw new AuthorizationException();
        }
      } catch (IOException ioe) {
        throw new AuthorizationException(ioe);
      }
    }
  }

  private void testAuditWorkloadWithOutput(String auditOutputPath) throws Exception {
    long workloadStartTime = System.currentTimeMillis() + 10000;
    Job workloadJob = WorkloadDriver.getJobForSubmission(conf, dfs.getUri().toString(),
        workloadStartTime, AuditReplayMapper.class);
    boolean success = workloadJob.waitForCompletion(true);
    assertTrue("workload job should succeed", success);
    Counters counters = workloadJob.getCounters();
    assertEquals(6, counters.findCounter(AuditReplayMapper.REPLAYCOUNTERS.TOTALCOMMANDS).getValue());
    assertEquals(1, counters.findCounter(AuditReplayMapper.REPLAYCOUNTERS.TOTALINVALIDCOMMANDS).getValue());
    assertTrue(dfs.getFileStatus(new Path("/tmp/test1")).isFile());
    assertTrue(dfs.getFileStatus(new Path("/tmp/testDirRenamed")).isDirectory());
    assertFalse(dfs.exists(new Path("/denied")));

    assertTrue(dfs.exists(new Path(auditOutputPath)));
    try (FSDataInputStream auditOutputFile = dfs.open(new Path(auditOutputPath, "part-r-00000"))) {
      String auditOutput = IOUtils.toString(auditOutputFile);
      LOG.info(auditOutput);
      assertTrue(auditOutput.matches(".*(hdfs,WRITE,[A-Z]+,[13]+,[0-9]+\\n){3}.*"));
      // Matches three lines of the format "hdfs,WRITE,name,count,time"
      // Using [13] for the count group because each operation is run either
      // 1 or 3 times but the output order isn't guaranteed
    }
  }
}
