package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.RSC_IMS_Form;
import com.suoyasoft.boh.utils.NcMidConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class RequireappSyn
{
  private Connection bohConnection;
  private PreparedStatement statement;
  private ResultSet resultSet;
  protected static Logger logger = Logger.getLogger(RequireappSyn.class.getName());

  List<String> okFormNos = new ArrayList();
  private String sqlString = "";

  private void synData()
  {
    List requireappList;
    try
    {
      requireappList = loadRequireapp();
    }
    catch (Exception localException)
    {
    }
  }

  private List loadRequireapp()
  {
    List rst = new ArrayList();
    this.sqlString = "查询审核后的订货单sql语句";

    logger.debug(this.sqlString);
    try
    {
      this.bohConnection = NcMidConn.getInstance().getConnection();
      this.statement = this.bohConnection.prepareStatement(this.sqlString);
      this.resultSet = this.statement.executeQuery();
      while (this.resultSet.next())
      {
        RSC_IMS_Form form = new RSC_IMS_Form();
        rst.add(form);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(this.sqlString);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.statement.close();
        this.bohConnection.close(); } catch (Exception localException3) {
      }
    }
    return null;
  }
}