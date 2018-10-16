package com.grappim.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import lombok.Getter;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 16, 2018 Project: telegram-bot-test
 */

public class LoadProperties {

  @Getter
  private Properties prop;

  public LoadProperties() {
    loadProperties();
  }

  private void loadProperties() {
    InputStream inputStream = getClass().getResourceAsStream("/config.properties");
    prop = new Properties();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      prop.load(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
