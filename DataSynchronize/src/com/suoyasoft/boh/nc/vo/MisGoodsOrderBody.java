package com.suoyasoft.boh.nc.vo;

public class MisGoodsOrderBody
{
  private String Material_Code;
  private String FormNo;
  private Double OrderCount;
  private String Comments;

  public String getMaterial_Code()
  {
    return this.Material_Code;
  }

  public void setMaterial_Code(String material_Code) {
    this.Material_Code = material_Code;
  }

  public String getFormNo() {
    return this.FormNo;
  }

  public void setFormNo(String formNo) {
    this.FormNo = formNo;
  }

  public Double getOrderCount() {
    return this.OrderCount;
  }

  public void setOrderCount(Double orderCount) {
    this.OrderCount = orderCount;
  }

  public String getComments() {
    return this.Comments;
  }

  public void setComments(String comments) {
    this.Comments = comments;
  }
}