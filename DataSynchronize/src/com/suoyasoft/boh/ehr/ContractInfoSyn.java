package com.suoyasoft.boh.ehr;

import com.suoyasoft.boh.ehr.vo.EHR_ContractInfo;
import com.suoyasoft.boh.ehr.vo.RSC_Employee_Contract;
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

public class ContractInfoSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  protected static Logger logger = Logger.getLogger(ContractInfoSyn.class.getName());
  private static final String TABLE_NAME = "RSC_Employee_Contract";
  private static final String KEY_WORD = "ContractID";
  private List<String> contractIDs = new ArrayList();

  public void synData()
  {
    try
    {
      List rscContracts = loadRSCEmployeeContracts();
      List ehrContractInfos = loadEHRContractInfo(rscContracts);

      insertEHRConstractInfo(ehrContractInfos);

      new Util().updateRSCSynStatus("RSC_Employee_Contract", this.contractIDs, "ContractID");
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

  private List<RSC_Employee_Contract> loadRSCEmployeeContracts()
  {
    List rst = new ArrayList();
    this.sql = "select a.*,b.ID,b.Store_Code as StoreCompany  from RSC_Employee_Contract a,RSC_Employee b where a.EmployeeCode=b.EmployeeCode and upper(subStr(b.Store_Code, 0, 2)) = 'CN'  and a.NcSynStatus=0";

    this.sql = String.format(this.sql, new Object[0]);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_Employee_Contract contract = new RSC_Employee_Contract();

        contract.setContractID(this.result.getInt("ContractID"));
        contract.setEmployeeCode(this.result.getString("EmployeeCode"));
        contract.setContractNo(this.result.getString("ContractNo"));
        contract.setContractType(this.result.getString("ContractType"));
        contract.setLimitType(this.result.getString("LimitType"));
        contract.setLimitMonth(this.result.getInt("LimitMonth"));
        contract.setChangeType(this.result.getString("ChangeType"));
        contract.setStartDate(this.result.getString("StartDate"));
        contract.setEndDate(this.result.getString("EndDate"));
        contract.setSignDate(this.result.getString("SignDate"));
        contract.setCompany(this.result.getString("StoreCompany"));
        contract.setComments(this.result.getString("Comments"));
        contract.setModifyDate(this.result.getString("ModifyDate"));
        contract.setID(this.result.getString("ID"));
        rst.add(contract);

        this.contractIDs.add(String.valueOf(contract.getContractID()));
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
      } catch (Exception localException3) {
      }
    }
    return rst;
  }

  private List<EHR_ContractInfo> loadEHRContractInfo(List<RSC_Employee_Contract> contracts)
  {
    List rst = new ArrayList();

    for (Iterator i = contracts.iterator(); i.hasNext(); )
    {
      RSC_Employee_Contract misContract = (RSC_Employee_Contract)i.next();
      EHR_ContractInfo ehrContract = new EHR_ContractInfo();

      ehrContract.psnbasdocid = String.valueOf(misContract.getContractNo());
      ehrContract.psncode = misContract.getEmployeeCode();
      ehrContract.pk_termtype = misContract.getLimitType();
      ehrContract.itermmonth = String.valueOf(misContract.getLimitMonth());
      ehrContract.begindate = misContract.getStartDate();
      ehrContract.enddate = misContract.getEndDate();
      ehrContract.dsigndate = misContract.getSignDate();
      ehrContract.ID = misContract.getID();

      if (misContract.getContractType().equals("017001"))
        ehrContract.iconttype = "1";
      else if (misContract.getContractType().equals("017002"))
        ehrContract.iconttype = "2";
      else if (misContract.getContractType().equals("017003"))
        ehrContract.iconttype = "3";
      else if (misContract.getContractType().equals("017004"))
        ehrContract.iconttype = "4";
      else if (misContract.getContractType().equals("017005"))
        ehrContract.iconttype = "5";
      else {
        ehrContract.iconttype = "6";
      }
      ehrContract.icontstate = ehrContract.iconttype;
      ehrContract.groupdef1 = misContract.getCompany();
      ehrContract.vmemo = ((misContract.getComments() == null) ? " " : misContract.getComments());
      ehrContract.VCONTRACTNUM = misContract.getContractNo();

      rst.add(ehrContract);
    }
    return rst;
  }

  private void insertEHRConstractInfo(List<EHR_ContractInfo> list)
    throws SQLException
  {
    String strInsert = "Insert into mid_psndoc_ctrt(psnbasdocid,psncode,icontstate,pk_termtype,itermmonth,begindate,enddate,dsigndate,iconttype,ID,groupdef1,vmemo,VCONTRACTNUM) values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";

    String strDelete = "delete From mid_psndoc_ctrt where psnbasdocid = '%s'";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        EHR_ContractInfo ehrContractInfo = (EHR_ContractInfo)i.next();

        this.sql = String.format(strDelete, new Object[] { ehrContractInfo.psnbasdocid });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
        this.sql = 
          String.format(strInsert, new Object[] { ehrContractInfo.psnbasdocid, ehrContractInfo.psncode, ehrContractInfo.icontstate, ehrContractInfo.pk_termtype, ehrContractInfo.itermmonth, 
          ehrContractInfo.begindate, ehrContractInfo.enddate, ehrContractInfo.dsigndate, ehrContractInfo.iconttype, ehrContractInfo.ID, 
          ehrContractInfo.groupdef1, ehrContractInfo.vmemo, ehrContractInfo.VCONTRACTNUM });
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
      catch (SQLException localSQLException) {
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
      catch (Exception localException1)
      {
      }
    }
  }
}