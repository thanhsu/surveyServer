package com.survey.constant;

import java.io.Serializable;
import java.util.List;

public class UserPushBean implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 654066604L;
  protected String mvUUID;
  protected String mvClientID;
  protected String mvLoginIP;
  protected List<String> mvTopic;

  public String getMvUUID() {
    return mvUUID;
  }

  public void setMvUUID(String mvUUID) {
    this.mvUUID = mvUUID;
  }

  public String getMvClientID() {
    return mvClientID;
  }

  public void setMvClientID(String mvClientID) {
    this.mvClientID = mvClientID;
  }

  public String getMvLoginIP() {
    return mvLoginIP;
  }

  public void setMvLoginIP(String mvLoginIP) {
    this.mvLoginIP = mvLoginIP;
  }

  public List<String> getMvTopic() {
    return mvTopic;
  }

  public void setMvTopic(List<String> mvTopic) {
    this.mvTopic = mvTopic;
  }
}
