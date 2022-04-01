package joist.codegen.passes;

import java.io.File;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joist.codegen.Codegen;
import joist.util.Read;

public class OutputPass implements Pass<Codegen> {

  private static final Logger log = LoggerFactory.getLogger(OutputPass.class);

  public void pass(Codegen codegen) {
    // sanity check the config
    for (String badSkipped : codegen.getConfig().getStaleSkippedCollections()) {
      throw new IllegalStateException("BAD CONFIGURATION: Collection marked 'setCollectionSkipped' was not available " + badSkipped);
    }

    codegen.getOutputSourceDirectory().output();
    codegen.getOutputCodegenDirectory().output();
    if (codegen.getConfig().pruneCodegenDirectory) {
      codegen.getOutputCodegenDirectory().pruneIfNotTouchedWithinUsedPackages();
    }
    if (codegen.getConfig().pruneSourceDirectory) {
      for (File directory : codegen.getOutputSourceDirectory().getUsedDirectories()) {
        for (File file : directory.listFiles()) {
          if (file.isFile() && !codegen.getOutputSourceDirectory().getTouched().contains(file)) {
            String contents = Read.fromFile(file, Charset.defaultCharset());
            String className = file.getName().replaceAll("\\.[a-zA-Z]+", "");
            String codegenName = className + "Codegen";
            if (contents.contains(className + " extends " + codegenName)) {
              file.delete();
              log.warn("Removing old file {}", file);
            }
          }
        }
      }
    }
  }

}
