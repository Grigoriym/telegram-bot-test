package com.grappim.menu;

import lombok.Getter;
import lombok.Setter;

/**
 * IDE: IntelliJ IDEA Created by grigo on Oct, 14, 2018 Project: telegram-bot-test
 */

public class MenuItem {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String action;

  public MenuItem() {
  }

  public MenuItem(String name, String action) {
    this.name = name;
    this.action = action;
  }
}
