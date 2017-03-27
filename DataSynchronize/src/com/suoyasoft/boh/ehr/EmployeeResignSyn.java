package com.suoyasoft.boh.ehr;

import com.suoyasoft.boh.ehr.vo.EHR_ResignInfo;
import com.suoyasoft.boh.ehr.vo.RSC_Employee_Resign;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class EmployeeResignSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  protected static Logger logger = Logger.getLogger(RewardPunishSyn.class.getName());

  private List<String> resignIDS = new ArrayList();
  private static final String TABLE_NAME = "RSC_Employee_Resign";
  private static final String KEY_WORD = "ResignID";

  public void synData()
  {
    try
    {
      Calendar cd = Calendar.getInstance();
      cd.add(5, -1);

      String modifyDate = new SimpleDateFormat("yyyy-MM-dd").format(cd.getTime());

      List misRecords = loadMisResignData();
      List resignInfo = loadEHRResignInfo(misRecords);
      insertEHRWorkInfo(resignInfo);

      new Util().updateRSCSynStatus("RSC_Employee_Resign", this.resignIDS, "ResignID");
    }
    catch (SQLException se)
    {
      logger.error(se.getMessage());
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private List<RSC_Employee_Resign> loadMisResignData()
  {
    List rst = new ArrayList();

    this.sql = "Select a.ResignID as ResignID,a.EmployeeCode as EmployeeCode,a.ResignDate as ResignDate,a.ResignType as ResignType,a.Reason1 as Reason1,a.Reason2 as Reason2,a.Reason3 Reason3 ,a.Comments as Comments,a.ModifyDate as ModifyDate,b.Store_Code as Store_Code,b.PositionName as PositionName,b.ID as ID from RSC_Employee_Resign a,RSC_Employee b where a.EmployeeCode=b.EmployeeCode and a.NcSynStatus=0 and upper(subStr(b.Store_Code, 0, 2)) = 'CN'";

    this.sql = String.format(this.sql, new Object[0]);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_Employee_Resign record = new RSC_Employee_Resign();
        record.setResignID(this.result.getInt("ResignID"));
        record.setEmployeeCode(this.result.getString("EmployeeCode"));
        record.setResignDate(this.result.getString("ResignDate"));
        record.setResignType(this.result.getString("ResignType"));
        record.setReason1(this.result.getString("Reason1"));
        record.setReason2(this.result.getString("Reason2"));
        record.setReason3(this.result.getString("Reason3"));
        record.setComments(this.result.getString("Comments"));
        record.setModifyDate(this.result.getString("ModifyDate"));
        record.setPositionName(this.result.getString("PositionName"));
        record.setStore_Code(this.result.getString("Store_Code"));
        record.setID(this.result.getString("ID"));
        rst.add(record);
        this.resignIDS.add(String.valueOf(record.getResignID()));
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.misConn.close();
      }
      catch (Exception localException3)
      {
      }
    }

    return rst;
  }

  private List<EHR_ResignInfo> loadEHRResignInfo(List<RSC_Employee_Resign> misRecords)
  {
    List rst = new ArrayList();
    for (Iterator i = misRecords.iterator(); i.hasNext(); )
    {
      RSC_Employee_Resign record = (RSC_Employee_Resign)i.next();

      EHR_ResignInfo info = new EHR_ResignInfo();
      info.psnbasdocid = record.getEmployeeCode();
      info.psncode = record.getEmployeeCode();
      info.leavedate = record.getResignDate();
      info.pkomdutybefore = record.getPositionName();
      info.reason = record.getReason3();
      info.pk_corp = record.getStore_Code();
      info.type = record.getReason1();
      info.ID = record.getID();
      info.pk_psndoc = "";
      info.pkdeptbefore = record.getStore_Code();
      info.pkdeptafter = record.getStore_Code();

      rst.add(info);
    }
    return rst;
  }

  private void insertEHRWorkInfo(List<EHR_ResignInfo> infos)
    throws SQLException
  {
    String strInsert = "Insert into mid_psndoc_dimission(psnbasdocid,psncode,leavedate,pkomdutybefore,pkdeptbefore,pkdeptafter,type,reason,pk_corp,pk_psndoc,ID) values( '%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";

    String strDelete = "delete From mid_psndoc_dimission where psncode='%s'";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);
      for (Iterator i = infos.iterator(); i.hasNext(); )
      {
        EHR_ResignInfo info = (EHR_ResignInfo)i.next();

        this.sql = String.format(strDelete, new Object[] { info.psncode });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();

        this.sql = 
          String.format(strInsert, new Object[] { 
          info.psnbasdocid, info.psncode, info.leavedate, info.pkomdutybefore, info.pkdeptbefore, 
          info.pkdeptafter, info.type, info.reason, info.pk_corp, info.pk_psndoc, 
          info.ID });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }

      this.midConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.midConn.rollback();
      }
      catch (Exception localException1)
      {
      }
      logger.error(this.sql);
      logger.error(e.getMessage());
      throw new SQLException();
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.midConn.close();
      }
      catch (Exception localException2)
      {
      }
    }
  }
}