package com.suoyasoft.boh.nc.vo;

public class MisAppropriationBody
{
  private String FormNo;
  private String Material_Code;
  private Double SendCount;
  private Double ReceiveCount;
  private String Comments;

  public String getFormNo()
  {
    return this.FormNo;
  }

  public void setFormNo(String formNo)
  {
    this.FormNo = formNo;
  }

  public String getMaterial_Code()
  {
    return this.Material_Code;
  }

  public void setMaterial_Code(String material_Code)
  {
    this.Material_Code = material_Code;
  }

  public Double getSendCount()
  {
    return this.SendCount;
  }

  public void setSendCount(Double sendCount)
  {
    this.SendCount = sendCount;
  }

  public Double getReceiveCount()
  {
    return this.ReceiveCount;
  }

  public void setReceiveCount(Double receiveCount)
  {
    this.ReceiveCount = receiveCount;
  }

  public String getComments()
  {
    return this.Comments;
  }

  public void setComments(String comments)
  {
    this.Comments = comments;
  }
}