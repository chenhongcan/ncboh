package com.suoyasoft.boh.ehr.vo;

public class RSC_Employee_Contract
{
  private int ContractID;
  private String EmployeeCode;
  private String ContractNo;
  private String ContractType;
  private String LimitType;
  private int limitMonth;
  private String ChangeType;
  private String StartDate;
  private String EndDate;
  private String SignDate;
  private String Company;
  private String Comments;
  private String ModifyDate;
  private String ID;

  public String getID()
  {
    return this.ID;
  }

  public void setID(String id) {
    this.ID = id;
  }

  public int getContractID() {
    return this.ContractID;
  }

  public void setContractID(int contractID) {
    this.ContractID = contractID;
  }

  public String getEmployeeCode() {
    return this.EmployeeCode;
  }

  public void setEmployeeCode(String employeeCode) {
    this.EmployeeCode = employeeCode;
  }

  public String getContractNo() {
    return this.ContractNo;
  }

  public void setContractNo(String contractNo) {
    this.ContractNo = contractNo;
  }

  public String getContractType() {
    return this.ContractType;
  }

  public void setContractType(String contractType) {
    this.ContractType = contractType;
  }

  public String getLimitType() {
    return this.LimitType;
  }

  public void setLimitType(String limitType) {
    this.LimitType = limitType;
  }

  public int getLimitMonth() {
    return this.limitMonth;
  }

  public void setLimitMonth(int limitMonth) {
    this.limitMonth = limitMonth;
  }

  public String getChangeType() {
    return this.ChangeType;
  }

  public void setChangeType(String changeType) {
    this.ChangeType = changeType;
  }

  public String getStartDate() {
    return this.StartDate;
  }

  public void setStartDate(String startDate) {
    this.StartDate = startDate;
  }

  public String getEndDate() {
    return this.EndDate;
  }

  public void setEndDate(String endDate) {
    this.EndDate = endDate;
  }

  public String getSignDate() {
    return this.SignDate;
  }

  public void setSignDate(String signDate) {
    this.SignDate = signDate;
  }

  public String getCompany() {
    return this.Company;
  }

  public void setCompany(String company) {
    this.Company = company;
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