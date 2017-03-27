package com.suoyasoft.boh.ehr.vo;

public class RSC_Employee_Resign
{
  private int ResignID;
  private String EmployeeCode;
  private String ResignDate;
  private String ResignType;
  private String Reason1;
  private String Reason2;
  private String Reason3;
  private String Comments;
  private String ModifyDate;
  private String ID;
  private String PositionName;
  private String Store_Code;

  public String getPositionName()
  {
    return this.PositionName;
  }

  public String getID() {
    return this.ID;
  }

  public void setID(String id) {
    this.ID = id;
  }

  public void setPositionName(String positionName) {
    this.PositionName = positionName;
  }

  public String getStore_Code() {
    return this.Store_Code;
  }

  public void setStore_Code(String store_Code) {
    this.Store_Code = store_Code;
  }

  public int getResignID() {
    return this.ResignID;
  }

  public void setResignID(int resignID) {
    this.ResignID = resignID;
  }

  public String getEmployeeCode() {
    return this.EmployeeCode;
  }

  public void setEmployeeCode(String employeeCode) {
    this.EmployeeCode = employeeCode;
  }

  public String getResignDate() {
    return this.ResignDate;
  }

  public void setResignDate(String resignDate) {
    this.ResignDate = resignDate;
  }

  public String getResignType() {
    return this.ResignType;
  }

  public void setResignType(String resignType) {
    this.ResignType = resignType;
  }

  public String getReason1() {
    return this.Reason1;
  }

  public void setReason1(String reason1) {
    this.Reason1 = reason1;
  }

  public String getReason2() {
    return this.Reason2;
  }

  public void setReason2(String reason2) {
    this.Reason2 = reason2;
  }

  public String getReason3() {
    return this.Reason3;
  }

  public void setReason3(String reason3) {
    this.Reason3 = reason3;
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
}