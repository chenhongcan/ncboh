package com.suoyasoft.boh.ehr;

import com.suoyasoft.boh.ehr.vo.EHR_Punish;
import com.suoyasoft.boh.ehr.vo.EHR_Reward;
import com.suoyasoft.boh.ehr.vo.RSC_Employee_RewardPunish_Record;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class RewardPunishSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  protected static Logger logger = Logger.getLogger(RewardPunishSyn.class.getName());
  private static final int REWARD = 1;
  private List<String> rewardPunishIDs = new ArrayList();
  private static final String TABLE_NAME = "RSC_Employee_RewardPunish_Reco";
  private static final String KEY_WORD = "RewardPunishID";

  public void synData()
  {
    try
    {
      List misRecords = loadMisRewardPunish();
      List punishInfos = loadEHRPunishInfos(misRecords);
      List rewardInfos = loadEHRRewardInfos(misRecords);
      insertPunishInfo(punishInfos);
      insertRewardInfo(rewardInfos);

      new Util().updateRSCSynStatus("RSC_Employee_RewardPunish_Reco", this.rewardPunishIDs, "RewardPunishID");
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

  private List<RSC_Employee_RewardPunish_Record> loadMisRewardPunish()
  {
    List rst = new ArrayList();

    this.sql = "Select a.*,b.ID as ID from RSC_Employee_RewardPunish_Reco a,RSC_Employee b where a.EmployeeCode = b.EmployeeCode  and a.NcSynStatus=0 and upper(subStr(b.Store_Code, 0, 2)) = 'CN'";

    this.sql = String.format(this.sql, new Object[0]);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_Employee_RewardPunish_Record record = new RSC_Employee_RewardPunish_Record();
        record.setRewardPunishID(this.result.getInt("RewardPunishID"));
        record.setEmployeeCode(this.result.getString("EmployeeCode"));
        record.setRewardPunishType(this.result.getString("RewardPunishType"));
        record.setMeasures(this.result.getString("Measures"));
        record.setSysDate(this.result.getString("SysDate"));
        record.setSysDate1(this.result.getString("SysDate1"));
        record.setComments(this.result.getString("Comments"));
        record.setModifyDate(this.result.getString("ModifyDate"));
        record.setMechanism(this.result.getString("Mechanism"));
        record.setStatus(this.result.getInt("Status"));
        record.setID(this.result.getString("ID"));
        record.setProperty(this.result.getString("Property"));
        record.setReason(this.result.getString("Reason"));

        rst.add(record);
        this.rewardPunishIDs.add(String.valueOf(record.getRewardPunishID()));
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
      catch (Exception e)
      {
        logger.error(this.sql);
        logger.error(e.getMessage());
      }
    }
    return rst;
  }

  private List<EHR_Punish> loadEHRPunishInfos(List<RSC_Employee_RewardPunish_Record> misRecords)
  {
    List rst = new ArrayList();
    for (Iterator i = misRecords.iterator(); i.hasNext(); )
    {
      RSC_Employee_RewardPunish_Record misRecord = (RSC_Employee_RewardPunish_Record)i.next();
      if (misRecord.getStatus() != 1) {
        EHR_Punish info = new EHR_Punish();

        info.psnbasdocid = misRecord.getEmployeeCode();
        info.psncode = misRecord.getEmployeeCode();
        info.groupdef2 = misRecord.getRewardPunishType();
        info.vpunishorg = misRecord.getProperty();
        info.vpunishmeas = misRecord.getMeasures();
        info.dpunishdate = misRecord.getSysDate();
        info.vpunishmatter = misRecord.getReason();
        info.groupdef1 = misRecord.getSysDate1();
        info.info1 = ((misRecord.getComments() == null) ? "" : misRecord.getComments());
        info.ID = misRecord.getID();

        rst.add(info); }
    }
    return rst;
  }

  private List<EHR_Reward> loadEHRRewardInfos(List<RSC_Employee_RewardPunish_Record> misRecords)
  {
    List rst = new ArrayList();
    for (Iterator i = misRecords.iterator(); i.hasNext(); )
    {
      RSC_Employee_RewardPunish_Record record = (RSC_Employee_RewardPunish_Record)i.next();
      if (record.getStatus() != 1)
        continue;
      EHR_Reward info = new EHR_Reward();

      info.psnbasdocid = record.getEmployeeCode();
      info.psncode = record.getEmployeeCode();
      info.dencourdate = record.getSysDate();
      info.groupdef1 = record.getRewardPunishType();
      info.vencourmatter = record.getReason();
      info.vencourorg = record.getProperty();
      info.vencourmeas = record.getMeasures();
      info.info1 = ((record.getComments() == null) ? "" : record.getComments());
      info.ID = record.getID();
      rst.add(info);
    }
    return rst;
  }

  private void insertPunishInfo(List<EHR_Punish> punishInfos)
    throws SQLException
  {
    String strInsert = "insert into hi_psndoc_pun(psnbasdocid,psncode,groupdef2,vpunishorg,vpunishmeas,dpunishdate,vpunishmatter,ID,groupdef1,info1) values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";

    String strDelete = "delete From hi_psndoc_pun where psncode='%s'";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator i = punishInfos.iterator(); i.hasNext(); )
      {
        EHR_Punish info = (EHR_Punish)i.next();

        this.sql = String.format(strDelete, new Object[] { info.psncode });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();

        this.sql = String.format(strInsert, new Object[] { info.psnbasdocid, info.psncode, info.groupdef2, info.vpunishorg, info.vpunishmeas, info.dpunishdate, info.vpunishmatter, info.ID, info.groupdef1, info.info1 });
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
      catch (Exception localException1) {
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

  private void insertRewardInfo(List<EHR_Reward> rewardInfos)
    throws SQLException
  {
    String strInsert = "insert into mid_psndoc_enc(psnbasdocid,psncode,groupdef1,vencourorg,vencourmeas,dencourdate,vencourmatter,ID,info1) values('%s','%s','%s','%s','%s','%s','%s','%s','%s')";

    String strDelete = "delete From mid_psndoc_enc where psncode='%s'";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);
      for (Iterator i = rewardInfos.iterator(); i.hasNext(); )
      {
        EHR_Reward info = (EHR_Reward)i.next();

        this.sql = String.format(strDelete, new Object[] { info.psncode });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();

        this.sql = String.format(strInsert, new Object[] { info.psnbasdocid, info.psncode, info.groupdef1, info.vencourorg, info.vencourmeas, info.dencourdate, info.vencourmatter, info.ID, info.info1 });
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