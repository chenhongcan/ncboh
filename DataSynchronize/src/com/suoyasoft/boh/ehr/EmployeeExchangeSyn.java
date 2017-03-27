package com.suoyasoft.boh.ehr;

import com.suoyasoft.boh.ehr.vo.EHR_Loan;
import com.suoyasoft.boh.ehr.vo.RSC_Employee_Store_Exchange;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class EmployeeExchangeSyn
{
  private List<String> employeeCodes = new ArrayList();
  private List<String> employeeCodes_Tran = new ArrayList();
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  protected static Logger logger = Logger.getLogger(EmployeeExchangeSyn.class.getName());

  public void synData()
  {
    try
    {
      Calendar cd = Calendar.getInstance();
      cd.add(5, -1);
      String modifyDate = new SimpleDateFormat("yyyy-MM-dd").format(cd.getTime());

      List misExchangeDatas = loadExchangeDatas();
      List misTransferDatas = loadTransferDatas(modifyDate);

      List ehrLoanDatas = loadEHRLoanDatas(misExchangeDatas);
      List ehrTransferDatas = loadEHRTransferDatas(misTransferDatas);

      ehrLoanDatas.addAll(ehrTransferDatas);

      insertEHRLoan(ehrLoanDatas);

      new Util().updateRSCEmployeeExchange(this.employeeCodes, "RSC_Employee_Store_Exchange");

      new Util().updateRSCEmployeeExchange(this.employeeCodes_Tran, "RSC_Employee_Transfer");
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private List<RSC_Employee_Store_Exchange> loadExchangeDatas()
  {
    List rst = new ArrayList();

    this.sql = "Select t1.*,t2.AfterDept as AfterDept,t2.BeforeDept,t2.AfterSalary,t2.Transfer_Type2  From RSC_Employee_Store_Exchange t1,RSC_Employee_Transfer t2  Where t1.EmployeeCode = t2.EmployeeCode and t1.BeginDate = t2.TransferDate and t1.NCSynStatus=0 and ExchangeType=2  and upper(subStr(t1.FromStore, 0, 2)) = 'CN' and upper(subStr(t1.ToStore, 0, 2)) = 'CN'";

    this.sql = String.format(this.sql, new Object[0]);
    logger.debug(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_Employee_Store_Exchange exchange = new RSC_Employee_Store_Exchange();

        exchange.setExchangeID(this.result.getInt("ExchangeID"));
        exchange.setEmployeeCode(this.result.getString("EmployeeCode"));
        exchange.setEmployeeName(this.result.getString("EmployeeName"));

        exchange.setFromStore(this.result.getString("FromStore"));
        exchange.setFromStoreName(this.result.getString("FromStoreName"));
        exchange.setToStore(this.result.getString("ToStore"));
        exchange.setToStoreName(this.result.getString("ToStoreName"));
        exchange.setID(this.result.getString("ID"));
        exchange.setSex(this.result.getString("Sex"));
        exchange.setLevel(this.result.getString("Level"));
        exchange.setBeginDate(this.result.getString("BeginDate"));
        exchange.setEndDate(this.result.getString("EndDate"));
        exchange.setStatus(this.result.getString("Status"));
        exchange.setBizDate(this.result.getString("BizDate"));
        exchange.setAfterDept(this.result.getString("AfterDept"));
        exchange.setBeforeDept(this.result.getString("BeforeDept"));
        exchange.setExchangeType(this.result.getString("Transfer_Type2"));
        exchange.setAfterSalary(this.result.getString("AfterSalary"));

        rst.add(exchange);

        this.employeeCodes.add(this.result.getString("EmployeeCode"));
      }
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
      } catch (Exception localException2) {
      }
    }
    return rst;
  }

  private List<RSC_Employee_Store_Exchange> loadTransferDatas(String bizDate)
  {
    List rst = new ArrayList();

    this.sql = "select t1.*,t2.ToStore as ToStore from (select a.*,b.ID as ID,b.Store_Code as Store_Code,b.EmployeeName as EmployeeName from RSC_Employee_Transfer a,RSC_Employee b where a.EmployeeCode = b.EmployeeCode and a.NCSynStatus=0 and upper(subStr(b.Store_Code, 0, 2)) = 'CN' and a.transfer_type1 is null) t1 left join RSC_Employee_Store_Exchange t2 on t1.EmployeeCode = t2.EmployeeCode where upper(subStr(t2.ToStore, 0, 2)) = 'CN' and upper(subStr(t2.fromstore, 0, 2)) = 'CN' ";

    this.sql = String.format(this.sql, new Object[] { bizDate });
    logger.debug(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_Employee_Store_Exchange exchange = new RSC_Employee_Store_Exchange();

        exchange.setExchangeID(this.result.getInt("TransferID"));
        exchange.setEmployeeCode(this.result.getString("EmployeeCode"));
        exchange.setEmployeeName(this.result.getString("EmployeeName"));
        exchange.setFromStore(this.result.getString("Store_Code"));
        String tmp = this.result.getString("ToStore");
        exchange.setToStore((tmp == null) ? this.result.getString("Store_Code") : tmp);
        exchange.setID(this.result.getString("ID"));
        exchange.setBeginDate(this.result.getString("TransferDate"));
        exchange.setLevel(this.result.getString("AfterDept"));

        exchange.setExchangeType(this.result.getString("Transfer_Type2"));

        exchange.setAfterSalary(this.result.getString("AfterSalary"));

        exchange.setBeforeDept(this.result.getString("BeforeDept"));

        rst.add(exchange);

        this.employeeCodes_Tran.add(this.result.getString("EmployeeCode"));
      }
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
      } catch (Exception localException2) {
      }
    }
    return rst;
  }

  private List<EHR_Loan> loadEHRLoanDatas(List<RSC_Employee_Store_Exchange> misExchangeDatas)
  {
    List rst = new ArrayList();

    for (Iterator i = misExchangeDatas.iterator(); i.hasNext(); )
    {
      RSC_Employee_Store_Exchange misExchange = (RSC_Employee_Store_Exchange)i.next();
      EHR_Loan ehrLoan = new EHR_Loan();

      ehrLoan.psncode = misExchange.getEmployeeCode();
      ehrLoan.psnname = misExchange.getEmployeeName();
      ehrLoan.id = misExchange.getID();
      ehrLoan.postdoc = misExchange.getAfterDept();
      ehrLoan.deptdocafter = misExchange.getToStore();
      ehrLoan.deptdocbefore = misExchange.getFromStore();
      ehrLoan.Effectdate = misExchange.getBeginDate();
      ehrLoan.info1 = misExchange.getBeforeDept();
      ehrLoan.info2 = misExchange.getExchangeType();
      ehrLoan.info3 = misExchange.getAfterSalary();

      rst.add(ehrLoan);
    }
    return rst;
  }

  private List<EHR_Loan> loadEHRTransferDatas(List<RSC_Employee_Store_Exchange> misTransferDatas)
  {
    List rst = new ArrayList();

    for (Iterator i = misTransferDatas.iterator(); i.hasNext(); )
    {
      RSC_Employee_Store_Exchange misExchange = (RSC_Employee_Store_Exchange)i.next();
      EHR_Loan ehrLoan = new EHR_Loan();

      ehrLoan.psncode = misExchange.getEmployeeCode();
      ehrLoan.psnname = misExchange.getEmployeeName();
      ehrLoan.id = misExchange.getID();
      ehrLoan.postdoc = misExchange.getLevel();
      ehrLoan.deptdocafter = misExchange.getToStore();
      ehrLoan.deptdocbefore = misExchange.getFromStore();
      ehrLoan.Effectdate = misExchange.getBeginDate();
      ehrLoan.info1 = misExchange.getBeforeDept();
      ehrLoan.info2 = misExchange.getExchangeType();
      ehrLoan.info3 = misExchange.getAfterSalary();

      rst.add(ehrLoan);
    }
    return rst;
  }

  private void insertEHRLoan(List<EHR_Loan> ehrLoanDatas)
  {
    String strDelete = "delete From mid_loan where psncode = '%s'";
    String strInsert = "Insert into mid_loan(psncode,psnname,id,postdoc,deptdocafter,deptdocbefore,Effectdate,info1,info2,info3) values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator i = ehrLoanDatas.iterator(); i.hasNext(); )
      {
        EHR_Loan loan = (EHR_Loan)i.next();

        this.sql = String.format(strDelete, new Object[] { loan.psncode });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();

        this.sql = String.format(strInsert, new Object[] { loan.psncode, loan.psnname, loan.id, loan.postdoc, loan.deptdocafter, loan.deptdocbefore, loan.Effectdate, loan.info1, loan.info2, loan.info3 });
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
      } catch (Exception localException1) {
      }
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.midConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}