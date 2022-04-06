package crazypants.enderio.base.config.recipes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.Log;
import crazypants.enderio.base.config.recipes.xml.Serializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class RecipeFactory {

  private static final @Nonnull String ASSETS_FOLDER_CONFIG = "config/";

  private static final @Nonnull String DEFAULT_USER_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<enderio:recipes xmlns:enderio=\"http://enderio.com/recipes\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://enderio.com/recipes recipes.xsd \">\n"
      + "\n</enderio:recipes>\n";

  private final @Nonnull File configDirectory;
  private final @Nonnull String domain;

  public RecipeFactory(@Nonnull File configDirectory, @Nonnull String domain) {
    this.configDirectory = configDirectory;
    this.domain = domain;
  }

  private InputStream getResource(ResourceLocation resourceLocation) throws IOException {
    final ModContainer container = Loader.instance().activeModContainer();
    if (container != null) {
      final String resourcePath = String.format("/%s/%s/%s", "assets", resourceLocation.getResourceDomain(), resourceLocation.getResourcePath());
      final InputStream resourceAsStream = container.getMod().getClass().getResourceAsStream(resourcePath);
      if (resourceAsStream != null) {
        return resourceAsStream;
      } else {
        throw new IOException("Could not find resource " + resourceLocation);
      }
    } else {
      throw new RuntimeException("Failed to find current mod while looking for resource " + resourceLocation);
    }
  }

  public void placeXSD(String folderName) {
    final ResourceLocation xsdRL = new ResourceLocation(domain, "config/recipes/recipes.xsd");
    final File xsdFL = new File(configDirectory, folderName + "/recipes.xsd");
    copyCore_dontMakeShittyCoreModsPlease_thisIncludesShittyMixins(xsdRL, xsdFL);
  }

  public void createFolder(String name) {
    if (!configDirectory.exists()) {
      configDirectory.mkdir();
    }
    File folderF = new File(configDirectory, name);
    if (!folderF.exists()) {
      folderF.mkdir();
    }
  }

  public NNList<File> listXMLFiles(String pathName) {
    return new NNList<>(new File(configDirectory, pathName).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".xml") && !"sagmill_oresalleasy.xml".equals(name);
      }
    }));
  }

  public <T extends IRecipeRoot> T readCoreFile(T target, String rootElement, String fileName) throws IOException, XMLStreamException {
    final ResourceLocation coreRL = new ResourceLocation(domain, ASSETS_FOLDER_CONFIG + fileName);

    Log.debug("Reading core recipe file " + fileName);
    try (InputStream coreFileStream = getResource(coreRL)) {
      try {
        return readStax(target, rootElement, coreFileStream, "core recipe file '" + fileName + "'");
      } catch (XMLStreamException e) {
        try (InputStream resource = getResource(coreRL)) {
          printContentsOnError(resource, coreRL.toString());
        }
        throw e;
      } catch (InvalidRecipeConfigException irce) {
        irce.setFilename(fileName);
        throw irce;
      }
    }
  }

  public void copyCore(String fileName) {
    copyCore(fileName, null);
  }

  public void copyCore(String fileName, @Nullable String fallback) {
    final ResourceLocation coreRL = new ResourceLocation(domain, ASSETS_FOLDER_CONFIG + fileName);
    final File coreFL = new File(configDirectory, fileName);
    if (!copyCore_dontMakeShittyCoreModsPlease_thisIncludesShittyMixins(coreRL, coreFL) && fallback != null) {
      copyCore(fallback, null);
    }
  }

  public void createFileUser(String fileName) {
    final File userFL = new File(configDirectory, fileName);

    if (!userFL.exists()) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFL, false))) {
        writer.write(DEFAULT_USER_FILE);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static <T extends IRecipeRoot> T readFileUser(T target, String rootElement, String fileName, File userFL) throws IOException, XMLStreamException {
    if (userFL.exists()) {
      Log.info("Reading user recipe file " + fileName);
      try (InputStream userFileStream = userFL.exists() ? new FileInputStream(userFL) : null;) {
        try {
          return readStax(target, rootElement, userFileStream, "user recipe file '" + fileName + "'");
        } catch (XMLStreamException e) {
          try (FileInputStream stream = new FileInputStream(userFL)) {
            printContentsOnError(stream, userFL.toString());
          }
          throw e;
        } catch (InvalidRecipeConfigException irce) {
          irce.setFilename(fileName);
          throw irce;
        }
      }
    }
    Log.info("Skipping missing user recipe file " + fileName);
    return target;
  }

  public static <T extends IRecipeRoot> T readFileIMC(T target, String rootElement, String fileName) throws IOException, XMLStreamException {
    File file = new File(fileName);
    if (file.exists()) {
      Log.info("Reading IMC recipe file " + fileName);
      try (InputStream userFileStream = new FileInputStream(file)) {
        try {
          return readStax(target, rootElement, userFileStream, "IMC file '" + fileName + "'");
        } catch (InvalidRecipeConfigException irce) {
          irce.setFilename(fileName);
          throw irce;
        }
      }
    } else {
      throw new FileNotFoundException("IMC file '" + fileName + "' doesn't exist");
    }
  }

  protected static void printContentsOnError(InputStream stream, String filename) throws FileNotFoundException, IOException {
    try {
      Log.error("Failed to parse xml from file '", filename, "'. Content:");
      int data = 0;
      while (data != -1) {
        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
        for (int i = 0; i < 16; i++) {
          data = stream.read();
          if (data != -1) {
            sb1.append(String.format("%02x ", data));
            if (data > 32 && data < 128) {
              sb2.appendCodePoint(data);
            } else {
              sb2.append(".");
            }
          } else {
            sb1.append("   ");
          }
        }
        Log.error(sb1, sb2);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  public static boolean ENABLE_TESTING = false;

  protected static <T extends IRecipeRoot> T readStax(T target, String rootElement, InputStream in, String source)
      throws XMLStreamException, InvalidRecipeConfigException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
    StaxFactory factory = new StaxFactory(eventReader, source);

    final T result = factory.readRoot(target, rootElement);
    if (ENABLE_TESTING && result != null) {
      try {
        System.out.println(Serializer.serialize(result));
      } catch (Exception e) {
        // NOP
      }
    }
    return result;
  }

  public void cleanFolder(String folderName) {
    File folder = new File(configDirectory, folderName);
    if (folder.exists()) {
      String[] list = folder.list((file, name) -> name.toLowerCase(Locale.ENGLISH).endsWith(".xml") || name.toLowerCase(Locale.ENGLISH).endsWith(".pdf"));
      if (list != null) {
        for (String name : list) {
          File file = new File(folder, name);
          Log.debug("Removing existing core recipe template file ", file);
          file.setWritable(true, true);
          file.delete();
        }
      }
    }
  }

  private boolean copyCore_dontMakeShittyCoreModsPlease_thisIncludesShittyMixins(ResourceLocation resourceLocation, File file) {
    try (InputStream schemaIn = getResource(resourceLocation)) {
      file.setWritable(true, true);
      try (OutputStream schemaOut = new FileOutputStream(file)) {
        IOUtils.copy(schemaIn, schemaOut);
        return true;
      }
    } catch (IOException e) {
      Log.error("Copying default recipe file from " + resourceLocation + " to " + file + " failed. Reason:");
      e.printStackTrace();
      return false;
    }
  }

}
