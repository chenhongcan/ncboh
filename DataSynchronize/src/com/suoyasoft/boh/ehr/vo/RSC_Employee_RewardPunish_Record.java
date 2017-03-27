package com.suoyasoft.boh.ehr.vo;

public class RSC_Employee_RewardPunish_Record
{
  private int RewardPunishID;
  private String EmployeeCode;
  private String RewardPunishType;
  private String Measures;
  private String SysDate;
  private String SysDate1;
  private String Comments;
  private String ModifyDate;
  private String mechanism;
  private int Status;
  private String ID;
  private String Property;
  private String Reason;

  public String getID()
  {
    return this.ID;
  }

  public void setID(String id) {
    this.ID = id;
  }

  public int getRewardPunishID() {
    return this.RewardPunishID;
  }

  public void setRewardPunishID(int rewardPunishID) {
    this.RewardPunishID = rewardPunishID;
  }

  public String getEmployeeCode() {
    return this.EmployeeCode;
  }

  public void setEmployeeCode(String employeeCode) {
    this.EmployeeCode = employeeCode;
  }

  public String getRewardPunishType() {
    return this.RewardPunishType;
  }

  public void setRewardPunishType(String rewardPunishType) {
    this.RewardPunishType = rewardPunishType;
  }

  public String getMeasures() {
    return this.Measures;
  }

  public void setMeasures(String measures) {
    this.Measures = measures;
  }

  public String getSysDate() {
    return this.SysDate;
  }

  public void setSysDate(String sysDate) {
    this.SysDate = sysDate;
  }

  public String getComments() {
    return this.Comments;
  }

  public void setComments(String comments) {
    this.Comments = comments;
  }

  public String getModifyDate() {
    return this.ModifyDate;
  }

  public void setModifyDate(String modifyDate) {
    this.ModifyDate = modifyDate;
  }

  public String getMechanism() {
    return this.mechanism;
  }

  public void setMechanism(String mechanism) {
    this.mechanism = mechanism;
  }

  public String getSysDate1() {
    return this.SysDate1;
  }

  public void setSysDate1(String sysDate1) {
    this.SysDate1 = sysDate1;
  }

  public int getStatus() {
    return this.Status;
  }

  public void setStatus(int status) {
    this.Status = status;
  }

  public String getProperty() {
    return this.Property;
  }

  public void setProperty(String property) {
    this.Property = property;
  }

  public String getReason() {
    return this.Reason;
  }

  public void setReason(String reason) {
    this.Reason = reason;
  }
}