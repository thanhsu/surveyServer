package com.survey.utils.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

public class Launcher extends io.vertx.core.Launcher {

  public static void main(String[] args) {
    new Launcher().dispatch(args);
  }

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    super.beforeDeployingVerticle(deploymentOptions);

    if (deploymentOptions.getConfig() == null) {
      deploymentOptions.setConfig(new JsonObject());
    }

    File conf = new File("config/config.json");
    deploymentOptions.getConfig().mergeIn(getConfiguration(conf));
  }

  @Override
  public void beforeStartingVertx(VertxOptions options) {
    // Customize metrics options
    DropwizardMetricsOptions metrics =
        new DropwizardMetricsOptions().setJmxDomain("TTL").setJmxEnabled(true).setEnabled(true);

    options.setMetricsOptions(metrics);
  }

  private JsonObject getConfiguration(File config) {
    JsonObject conf = new JsonObject();
    if (config.isFile()) {
      System.out.println("Reading config file: " + config.getAbsolutePath());
      try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
        String sconf = scanner.next();
        try {
          conf = new JsonObject(sconf);
        } catch (DecodeException e) {
          e.printStackTrace();
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Config file not found: " + config.getAbsolutePath());
    }
    return conf;
  }
}
