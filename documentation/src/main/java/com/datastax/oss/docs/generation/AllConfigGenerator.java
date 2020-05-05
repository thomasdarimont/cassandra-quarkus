/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.docs.generation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.annotation.processor.generate_doc.ConfigDocGeneratedOutput;
import io.quarkus.annotation.processor.generate_doc.ConfigDocItem;
import io.quarkus.annotation.processor.generate_doc.ConfigDocItemScanner;
import io.quarkus.annotation.processor.generate_doc.ConfigDocSection;
import io.quarkus.annotation.processor.generate_doc.ConfigDocWriter;
import io.quarkus.annotation.processor.generate_doc.DocGeneratorUtil;
import io.quarkus.bootstrap.resolver.AppModelResolverException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

public class AllConfigGenerator {
  public static void main(String[] args) throws AppModelResolverException, IOException {
    if (args.length != 1) {
      // exit 1 will break Maven
      throw new IllegalArgumentException("Usage: <version> <extension.json>");
    }
    String extensionFile = args[0];

    // This is where we produce the entire list of extensions
    File jsonFile = new File(extensionFile);
    if (!jsonFile.exists()) {
      System.err.println(
          "WARNING: could not generate all-config file because extensions list is missing: "
              + jsonFile);
      // exit 0 will break Maven
      return;
    }
    ObjectMapper mapper =
        new ObjectMapper(new YAMLFactory())
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
    MavenArtifactResolver resolver = MavenArtifactResolver.builder().build();

    // let's read it (and ignore the fields we don't need)
    ExtensionJson extensionJson = mapper.readValue(jsonFile, ExtensionJson.class);

    // now get all the listed extension jars via Maven
    Map<String, ExtensionJson> extensionsByConfigRoots = new HashMap<>();

    ArtifactRequest request = new ArtifactRequest();
    Artifact artifact =
        new DefaultArtifact(
            extensionJson.groupId, extensionJson.artifactId, "jar", extensionJson.version);
    request.setArtifact(artifact);

    // examine all the extension jars
    List<ArtifactRequest> deploymentRequests = new ArrayList<>(1);
    ArtifactResult result = resolver.resolve(artifact);

    try (ZipFile zf = new ZipFile(result.getArtifact().getFile())) {
      // collect all its config roots
      System.err.println("zf:" + zf.getName());
      collectConfigRoots(zf, extensionJson, extensionsByConfigRoots);
      System.err.println(extensionsByConfigRoots);

      // see if it has a deployment artifact we need to load
      ZipEntry entry = zf.getEntry("META-INF/quarkus-extension.properties");
      System.err.println("zf.entry:" + entry.getName());

      if (entry != null) {
        try (BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(zf.getInputStream(entry), StandardCharsets.UTF_8))) {
          Properties properties = new Properties();
          properties.load(reader);
          String deploymentGav = (String) properties.get("deployment-artifact");
          System.err.println("deploymentGav:" + deploymentGav);
          // if it has one, load it
          if (deploymentGav != null) {
            ArtifactRequest requestExecution = new ArtifactRequest();
            Artifact deploymentArtifact = new DefaultArtifact(deploymentGav);
            requestExecution.setArtifact(deploymentArtifact);
            deploymentRequests.add(requestExecution);
          }
        }
      }
    }

    // now examine all the extension deployment jars
    for (ArtifactResult deploymentResult : resolver.resolve(deploymentRequests)) {

      Artifact deploymentArtifact = deploymentResult.getArtifact();
      System.err.println("deploymentArtifact:" + deploymentArtifact);
      try (ZipFile zf = new ZipFile(deploymentArtifact.getFile())) {
        // collect all its config roots
        collectConfigRoots(zf, extensionJson, extensionsByConfigRoots);
      }
    }
    System.err.println(extensionsByConfigRoots);

    // load all the config items per config root
    ConfigDocItemScanner configDocItemScanner = new ConfigDocItemScanner();
    Map<String, List<ConfigDocItem>> docItemsByConfigRoots =
        configDocItemScanner.loadAllExtensionsConfigurationItems();
    System.out.println("docItemsByConfigRoots: " + docItemsByConfigRoots);
    Map<String, String> artifactIdsByName = new HashMap<>();
    ConfigDocWriter configDocWriter = new ConfigDocWriter();

    // build a list of sorted config items by extension
    List<ConfigDocItem> allItems = new ArrayList<>();
    SortedMap<String, List<ConfigDocItem>> sortedConfigItemsByExtension = new TreeMap<>();

    // sort extensions by name, assign their config items based on their config roots
    for (Entry<String, ExtensionJson> entry : extensionsByConfigRoots.entrySet()) {

      System.err.println("entry:" + entry);
      List<ConfigDocItem> items = docItemsByConfigRoots.get(entry.getKey());
      System.err.println("items:" + items);
      if (items != null) {
        String extensionName = entry.getValue().name;
        if (extensionName == null) {
          String extensionGav = entry.getValue().groupId + ":" + entry.getValue().artifactId;
          // compute the docs file name for this extension
          String docFileName = DocGeneratorUtil.computeExtensionDocFileName(entry.getKey());
          // now approximate an extension file name based on it
          extensionName = guessExtensionNameFromDocumentationFileName(docFileName);
          System.err.println(
              "WARNING: Extension name missing for "
                  + extensionGav
                  + " using guessed extension name: "
                  + extensionName);
          System.err.println("docFileName:" + docFileName);
          System.err.println("extensionName:" + extensionName);
        }

        artifactIdsByName.put(extensionName, entry.getValue().artifactId);
        List<ConfigDocItem> existingConfigDocItems =
            sortedConfigItemsByExtension.get(extensionName);
        if (existingConfigDocItems != null) {
          DocGeneratorUtil.appendConfigItemsIntoExistingOnes(existingConfigDocItems, items);
        } else {
          ArrayList<ConfigDocItem> configItems = new ArrayList<>();
          sortedConfigItemsByExtension.put(extensionName, configItems);
          configItems.addAll(items);
        }
      }
    }

    // now we have all the config items sorted by extension, let's build the entire list
    for (Map.Entry<String, List<ConfigDocItem>> entry : sortedConfigItemsByExtension.entrySet()) {

      final List<ConfigDocItem> configDocItems = entry.getValue();
      // sort the items
      DocGeneratorUtil.sort(configDocItems);
      // insert a header
      ConfigDocSection header = new ConfigDocSection();
      header.setSectionDetailsTitle(entry.getKey());
      header.setAnchorPrefix(artifactIdsByName.get(entry.getKey()));
      header.setName(artifactIdsByName.get(entry.getKey()));
      allItems.add(new ConfigDocItem(header, null));
      // add all the configs for this extension
      allItems.addAll(configDocItems);
    }

    // write our docs
    ConfigDocGeneratedOutput allConfigGeneratedOutput =
        new ConfigDocGeneratedOutput("quarkus-all-config.adoc", true, allItems, false);
    configDocWriter.writeAllExtensionConfigDocumentation(allConfigGeneratedOutput);
  }

  private static String guessExtensionNameFromDocumentationFileName(String docFileName) {
    // sanitise
    if (docFileName.startsWith("quarkus-")) {
      docFileName = docFileName.substring(8);
    }

    if (docFileName.endsWith(".adoc")) {
      docFileName = docFileName.substring(0, docFileName.length() - 5);
    }

    if (docFileName.endsWith("-config")) {
      docFileName = docFileName.substring(0, docFileName.length() - 7);
    }

    if (docFileName.endsWith("-configuration")) {
      docFileName = docFileName.substring(0, docFileName.length() - 14);
    }

    docFileName = docFileName.replace('-', ' ');
    return capitalize(docFileName);
  }

  private static String capitalize(String title) {
    char[] chars = title.toCharArray();
    boolean capitalize = true;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (Character.isSpaceChar(c)) {
        capitalize = true;
        continue;
      }
      if (capitalize) {
        if (Character.isLetter(c)) chars[i] = Character.toUpperCase(c);
        capitalize = false;
      }
    }
    return new String(chars);
  }

  private static void collectConfigRoots(
      ZipFile zf, ExtensionJson extension, Map<String, ExtensionJson> extensionsByConfigRoots)
      throws IOException {
    ZipEntry entry = zf.getEntry("META-INF/quarkus-config-roots.list");
    if (entry != null) {
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(zf.getInputStream(entry), StandardCharsets.UTF_8))) {
        // make sure we turn $ into . because javadoc-scanned class names are dot-separated
        reader
            .lines()
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .map(str -> str.replace('$', '.'))
            .forEach(klass -> extensionsByConfigRoots.put(klass, extension));
      }
    }
  }
}
