package com.suoyasoft.boh.ehr;

import com.suoyasoft.boh.ehr.vo.EHR_Attendance;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class EmployeeAttendanceDataSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  private SimpleDateFormat fdf = new SimpleDateFormat("yyyy-MM-01");
  public static final int WORKTIMEDAY = 26;
  protected static Logger logger = Logger.getLogger(EmployeeExchangeSyn.class.getName());

  public void synData()
  {
    String beginDate = "";
    String endDate = "";
    String firstDate = "";

    Calendar cal = Calendar.getInstance();
    if (Util.IsTestRun().booleanValue())
    {
      cal.add(2, -1);
      cal.set(5, 26);
      System.out.println(this.sdf.format(cal.getTime()));
    }

    if (cal.get(5) != 26)
    {
      return;
    }
    cal.set(5, 25);
    endDate = this.sdf.format(cal.getTime());
    firstDate = this.fdf.format(cal.getTime());

    cal.add(2, -1);
    cal.set(5, 26);
    beginDate = this.sdf.format(cal.getTime());
    try
    {
      List<EHR_Attendance> ehrDatas = null;
      List<EHR_Attendance> longExEmps = null;

      ehrDatas = loadCommonData1(beginDate, firstDate, endDate, null);
      insertEHRAttendance(ehrDatas);

      ehrDatas = loadCommonData2(beginDate, firstDate, endDate, null);
      insertEHRAttendance(ehrDatas);

      longExEmps = loadLongExchangeEmp(beginDate, endDate);
      ehrDatas.clear();
      for (EHR_Attendance e : longExEmps)
      {
        if (e.begindate.substring(0, 7).equals(beginDate.substring(0, 7)))
        {
          if (!(beginDate.equals(e.begindate)))
          {
            ehrDatas.addAll(loadCommonData1(beginDate, e.begindate, endDate, e.psncode));
          }

          ehrDatas.addAll(loadCommonData1(e.begindate, firstDate, endDate, e.psncode));

          ehrDatas.addAll(loadCommonData2(beginDate, firstDate, endDate, e.psncode));
        }
        else
        {
          ehrDatas.addAll(loadCommonData1(beginDate, firstDate, endDate, e.psncode));

          if (!(firstDate.equals(e.begindate)))
          {
            ehrDatas.addAll(loadCommonData1(firstDate, e.begindate, endDate, e.psncode));
          }

          ehrDatas.addAll(loadCommonData2(beginDate, e.begindate, endDate, e.psncode));
        }
      }
      insertEHRAttendance(ehrDatas);
      logger.info("工时同步完成!");
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private List<EHR_Attendance> loadCommonData1(String beginDate, String firstDate, String endDate, String eCode)
  {
    try
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(this.sdf.parse(firstDate));
      cal.add(5, -1);
      firstDate = this.sdf.format(cal.getTime());
    } catch (Exception localException) {
    }
    this.sql = 
      "Select e.Store_Code as Store_Code, ''{0}'' as BeginDate, ''{1}'' as EndDate, e.employeecode, e.employeename, e.id ,round(nvl(a1.ActualWorkTime,0),2) as CommonWorkTime ,round(nvl(a2.ActualWorkTime,0),2) as HolidayWorkTime ,Case s.is24Hours When 1 Then a3.NightWorkTime Else 0 End as NightWorkTime ,0 as AmountPre, '' '' as CommentsPre ,0 as AmountDeb, '' '' as CommentsDeb ,0 as AmountPayClothing ,0 as AmountBackClothing ,'' '' as DeliveryComments, 0 as DeliveryAmount ,nvl ( ( Select max(AfterSalary) as CurrentSalary From rsc_Employee_Transfer  Where EmployeeCode = e.EmployeeCode And TransferDate =  (  Select max(TransferDate) from rsc_Employee_Transfer  Where employeecode = e.employeecode and IsAdjust = 1 And TransferDate < ''{1}'' ) ),e.SalaryPerHour ) as SalaryPerHour From Rsc_Employee e Left join Rsc_Store s On s.store_code = e.store_code Left join (select EmployeeCode,sum(actualworktime) as ActualWorkTime From RSC_Employee_Worktime where holidayType =0 and bizdate between ''{0}'' and ''{1}'' and store_Code like ''CN%'' group by EmployeeCode) a1 on a1.EmployeeCode = e.EmployeeCode Left join (select EmployeeCode,sum(actualworktime) as ActualWorkTime From RSC_Employee_Worktime where holidayType<>0 and bizdate between ''{0}'' and ''{1}'' and store_Code like ''CN%'' group by EmployeeCode) a2 on a2.EmployeeCode = e.EmployeeCode Left join (select EmployeeCode,sum(NightWorkTime) as NightWorkTime From RSC_Employee_Worktime where BizDate between ''{0}'' and ''{1}'' and store_Code like ''CN%'' group by EmployeeCode) a3 on a3.EmployeeCode = e.EmployeeCode ";

    if (eCode == null)
    {
      EmployeeAttendanceDataSyn tmp55_54 = this; tmp55_54.sql = tmp55_54.sql + 
        "Where e.EmployeeCode not in  (Select EmployeeCode From Rsc_Employee_Store_Exchange Where exchangeType = 2 And beginDate >= ''{0}'' And beginDate <= ''{2}'' group by EmployeeCode )";
    }
    else
    {
      EmployeeAttendanceDataSyn tmp85_84 = this; tmp85_84.sql = tmp85_84.sql + "Where e.EmployeeCode = ''" + eCode + "''";
    }
    MessageFormat mf = new MessageFormat(this.sql);
    this.sql = mf.format(new String[] { beginDate, firstDate, endDate });
    return loadDatas(this.sql);
  }

  private List<EHR_Attendance> loadCommonData2(String beginDate, String firstDate, String endDate, String eCode)
  {
    this.sql = 
      "Select distinct e.store_code, ''{0}'' as BeginDate, ''{1}'' as EndDate, e.employeecode, e.employeename, e.id ,round(nvl(a1.ActualWorkTime,0),2) as CommonWorkTime ,round(nvl(a2.ActualWorkTime,0),2) as HolidayWorkTime ,Case s.is24Hours When 1 Then a3.NightWorkTime Else 0 End as NightWorkTime ,nvl(a4.Amount,0) as AmountPre, nvl(a4.Comments,'' '') as CommentsPre ,nvl(a5.Amount,0) as AmountDeb, nvl(a5.Comments,'' '') as CommentsDeb ,nvl(a6.Amount,0) as AmountPayClothing ,nvl(a7.Amount,0) as AmountBackClothing ,nvl(a8.Comments,'' '') as DeliveryComments, nvl(a8.amount,0) as DeliveryAmount ,nvl ( ( Select max(AfterSalary) as CurrentSalary  From rsc_Employee_Transfer  Where EmployeeCode = e.EmployeeCode  And TransferDate =  (  Select max(TransferDate) from rsc_Employee_Transfer  Where employeecode = e.employeecode and IsAdjust = 1 And TransferDate <= ''{1}''  )  ),e.SalaryPerHour ) as SalaryPerHour From Rsc_Employee e Left join Rsc_Store s On s.store_code = e.store_code Left join (select EmployeeCode,sum(actualworktime) as ActualWorkTime From RSC_Employee_Worktime where holidayType =0 and bizdate between ''{0}'' and ''{1}'' and store_Code like ''CN%'' group by EmployeeCode) a1 on a1.EmployeeCode = e.EmployeeCode Left join (select EmployeeCode,sum(actualworktime) as ActualWorkTime From RSC_Employee_Worktime where holidayType<>0 and bizdate between ''{0}'' and ''{1}'' and store_Code like ''CN%'' group by EmployeeCode) a2 on a2.EmployeeCode = e.EmployeeCode Left join (select EmployeeCode,sum(NightWorkTime) as NightWorkTime From RSC_Employee_Worktime where BizDate between ''{0}'' and ''{1}'' and store_Code like ''CN%'' group by EmployeeCode) a3 on a3.EmployeeCode = e.EmployeeCode Left join (select Amount,Comments,EmployeeCode From RSC_Employee_PremiumDebit where \"SysDate\" like ''{3}'' and type=''029001'' and store_Code like ''CN%'') a4 on a4.EmployeeCode = e.EmployeeCode  Left join (select Amount,Comments,EmployeeCode From RSC_Employee_PremiumDebit where \"SysDate\" like ''{3}'' and type=''029002'' and store_Code like ''CN%'') a5 on a5.EmployeeCode = e.EmployeeCode  Left join (select Amount,Comments,EmployeeCode From RSC_Employee_PremiumDebit where \"SysDate\" like ''{3}'' and type=''029003'' and store_Code like ''CN%'') a6 on a6.EmployeeCode = e.EmployeeCode  Left join (select Amount,Comments,EmployeeCode From RSC_Employee_PremiumDebit where \"SysDate\" like ''{3}'' and type=''029004'' and store_Code like ''CN%'') a7 on a7.EmployeeCode = e.EmployeeCode  Left join (select Amount,Comments,EmployeeCode From RSC_Employee_PremiumDebit where \"SysDate\" like ''{3}'' and type=''029005'' and store_Code like ''CN%'') a8 on a8.EmployeeCode = e.EmployeeCode  ";

    if (eCode == null)
    {
      EmployeeAttendanceDataSyn tmp12_11 = this; tmp12_11.sql = tmp12_11.sql + 
        "Where e.EmployeeCode not in (Select EmployeeCode From Rsc_Employee_Store_Exchange Where exchangeType = 2 And beginDate >= ''{2}'' And beginDate <= ''{1}'' group by EmployeeCode )";
    }
    else
    {
      EmployeeAttendanceDataSyn tmp42_41 = this; tmp42_41.sql = tmp42_41.sql + "Where e.EmployeeCode = ''" + eCode + "''";
    }
    MessageFormat mf = new MessageFormat(this.sql);
    this.sql = mf.format(new String[] { firstDate, endDate, beginDate, endDate.substring(0, 7) + "%" });
    return loadDatas(this.sql);
  }

  private List<EHR_Attendance> loadLongExchangeEmp(String beginDate, String endDate)
  {
    MessageFormat mf = new MessageFormat(
      "Select EmployeeCode, max(beginDate) as ExchangeDate From Rsc_Employee_Store_Exchange Where exchangeType = 2 And beginDate >= ''{0}'' And beginDate <= ''{1}'' group by EmployeeCode ");

    this.sql = mf.format(new String[] { beginDate, endDate });

    List<EHR_Attendance> rst = new ArrayList();
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();
      while (this.result.next())
      {
        EHR_Attendance worktime = new EHR_Attendance();
        worktime.begindate = this.result.getString("ExchangeDate");
        worktime.psncode = this.result.getString("EmployeeCode");
        rst.add(worktime);
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

  private List<EHR_Attendance> loadDatas(String sql)
  {
    logger.debug(sql);
    List rst = new ArrayList();
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();
      String tmp = "";
      while (this.result.next())
      {
        EHR_Attendance worktime = new EHR_Attendance();
        worktime.psncode = this.result.getString("EmployeeCode");
        worktime.begindate = this.result.getString("BeginDate");
        worktime.enddate = this.result.getString("EndDate");
        worktime.ID = this.result.getString("ID");
        worktime.groupdef1 = this.result.getString("SalaryPerHour");
        worktime.groupdef3 = this.result.getString("Store_Code");
        worktime.groupdef8 = this.result.getString("HolidayWorkTime");
        worktime.groupdef9 = this.result.getString("CommonWorkTime");
        worktime.info1 = this.result.getString("AmountPre");
        worktime.info2 = this.result.getString("AmountDeb");
        worktime.info3 = this.result.getString("AmountBackClothing");
        worktime.info4 = this.result.getString("AmountPayClothing");
        worktime.info5 = this.result.getString("CommentsPre");
        worktime.info6 = this.result.getString("CommentsDeb");
        tmp = this.result.getString("NightWorkTime");
        worktime.info7 = ((tmp.equals("null")) ? "0" : tmp);
        worktime.info8 = this.result.getString("DeliveryAmount");
        worktime.info9 = this.result.getString("DeliveryComments");
        rst.add(worktime);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(sql);
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

  private void insertEHRAttendance(List<EHR_Attendance> attendanceDatas)
    throws SQLException
  {
    String strInsert = "Insert into mid_psndoc_pun(psncode,begindate,enddate,groupdef1,groupdef8,groupdef9,groupdef3,ID,info1,info2,info3,info4,info5,info6,info7,info8,info9) values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator i = attendanceDatas.iterator(); i.hasNext(); )
      {
        EHR_Attendance attendance = (EHR_Attendance)i.next();

        this.sql = String.format(
          strInsert, new Object[] { 
          attendance.psncode, attendance.begindate, attendance.enddate, attendance.groupdef1, attendance.groupdef8, 
          attendance.groupdef9, attendance.groupdef3, attendance.ID, attendance.info1, attendance.info2, 
          attendance.info3, attendance.info4, attendance.info5, attendance.info6, attendance.info7, attendance.info8, attendance.info9 });

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
}