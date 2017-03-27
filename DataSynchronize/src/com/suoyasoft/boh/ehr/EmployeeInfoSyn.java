package com.suoyasoft.boh.ehr;

import com.suoyasoft.boh.ehr.vo.EHR_EmployeeInfo;
import com.suoyasoft.boh.ehr.vo.EHR_WorkInfo;
import com.suoyasoft.boh.ehr.vo.RSC_Employee;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class EmployeeInfoSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  protected static Logger logger = Logger.getLogger(EmployeeInfoSyn.class.getName());

  private List<String> employeeCodes = new ArrayList();
  private List<String> errorCodes = new ArrayList();
  private static final String TABLE_NAME = "RSC_Employee";
  private String workPK = "";
  private DecimalFormat df;

  public void synData()
  {
    try
    {
      this.df = new DecimalFormat("#.00");

      List misEmployees = loadMisEmployee();
      List ehrEmployeeInfos = loadEHREmployeeInfo(misEmployees);

      insertEHRData(ehrEmployeeInfos);
      new Util().updateRSCSynStatus("RSC_Employee", this.employeeCodes, "EmployeeCode");
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private List<RSC_Employee> loadMisEmployee()
  {
    List rst = new ArrayList();
    this.sql = "Select e.*, tt.CurrentSalary as CurrentSalary From RSc_Employee e Left join ( Select t.EmployeeCode, t.TransferDate, max(t.AfterSalary) as CurrentSalary From Rsc_Employee_Transfer t, (Select EmployeeCode, max(TransferDate)as TransferDate From rsc_Employee_Transfer Group by EmployeeCode) e Where e.EmployeeCode = t.EmployeeCode And e.TransferDate = t.TransferDate Group by t.EmployeeCode, t.TransferDate )tt on e.employeeCode=tt.employeeCode  Where e.Store_Code like 'CN%' and e.NcSynStatus = 0";

    logger.debug(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();
      while (this.result.next())
      {
        RSC_Employee employee = new RSC_Employee();
        employee.setEmployeeCode(this.result.getString("EmployeeCode"));
        employee.setStore_Code(this.result.getString("Store_Code"));
        employee.setEmployeeName(this.result.getString("EmployeeName"));
        employee.setAlias(this.result.getString("Alias"));
        employee.setSalaryName(this.result.getString("SalaryName"));
        employee.setNative(this.result.getString("Native"));
        employee.setCity(this.result.getString("City"));
        employee.setID(this.result.getString("ID"));
        employee.setSex(this.result.getInt("Sex"));
        employee.setAddress(this.result.getString("Address"));
        employee.setLocation(this.result.getString("Location"));
        employee.setSalaryBank(this.result.getString("SalaryBank"));
        employee.setBankCity(this.result.getString("BankCity"));
        employee.setBankName(this.result.getString("BankName"));
        employee.setBankAccount(this.result.getString("BankAccount"));
        employee.setBlood(this.result.getString("Blood"));
        employee.setNation(this.result.getString("Nation"));
        employee.setAge(this.result.getInt("Age"));
        employee.setBirth(this.result.getString("Birth"));
        employee.setBornDate(this.result.getString("BornDate"));
        employee.setPolity(this.result.getString("Polity"));
        employee.setMarry(this.result.getString("Marry"));
        employee.setResident(this.result.getString("Resident"));
        employee.setTel(this.result.getString("Tel"));
        employee.setMobile(this.result.getString("Mobile"));
        employee.setEMail(this.result.getString("EMail"));
        employee.setEdcution(this.result.getString("Edcution"));
        employee.setHand(this.result.getString("Hand"));
        employee.setContactPerson(this.result.getString("ContactPerson"));
        employee.setContactAds(this.result.getString("ContactAds"));
        employee.setContactTel(this.result.getString("ContactTel"));
        employee.setSalaryPerHour(Float.valueOf(this.result.getFloat("SalaryPerHour")));
        employee.setContactRelation(this.result.getString("ContactRelation"));
        employee.setContactCompany(this.result.getString("ContactCompany"));
        employee.setEmployeeType(this.result.getString("EmployeeType"));
        employee.setJoinDate(this.result.getString("JoinDate"));
        employee.setSalaryPaymentType(this.result.getString("SalaryPaymentType"));
        employee.setLaborType(this.result.getString("LaborType"));
        employee.setBelongStoreCardNo(this.result.getString("BelongStoreCardNo"));
        employee.setBelongStore(this.result.getString("BelongStore"));
        employee.setWorkStoreCardNo(this.result.getString("WorkStoreCardNo"));
        employee.setWorkStore(this.result.getString("WorkStore"));
        employee.setPositionName(this.result.getString("PositionName"));
        employee.setContractCompany(this.result.getString("ContractCompany"));
        employee.setAgreementStartDate(this.result.getString("AgreementStartDate"));
        employee.setAgreementEndDate(this.result.getString("AgreementEndDate"));
        employee.setWorkPermitExpiredDate(this.result.getString("WorkPermitExpiredDate"));
        employee.setBornExpiredDate(this.result.getString("BornExpiredDate"));
        employee.setTempResidentDate(this.result.getString("TempResidentDate"));
        employee.setHealthNo(this.result.getString("HealthNo"));
        employee.setHealthExpiredDate(this.result.getString("HealthExpiredDate"));
        employee.setWorkType(this.result.getInt("WorkType"));
        employee.setStatus(this.result.getInt("Status"));
        employee.setIsBlackList(this.result.getInt("IsBlackList"));
        employee.setModifyDate(this.result.getString("ModifyDate"));
        employee.setResignDate(this.result.getString("ResignDate"));
        employee.setProperty(this.result.getString("property"));
        employee.setRecruitmentChannel(this.result.getString("RecruitmentChannel"));
        employee.setWorkDate(this.result.getString("WorkDate"));
        employee.setMgrDate(this.result.getString("MgrDate"));
        employee.setCurrentSalary(this.result.getString("CurrentSalary"));

        rst.add(employee);
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

  private List<EHR_EmployeeInfo> loadEHREmployeeInfo(List<RSC_Employee> misEmployeeInfo)
  {
    List rst = new ArrayList();
    for (Iterator i = misEmployeeInfo.iterator(); i.hasNext(); )
    {
      RSC_Employee misEmployee = (RSC_Employee)i.next();
      EHR_EmployeeInfo employeeInfo = new EHR_EmployeeInfo();
      EHR_WorkInfo workInfo = new EHR_WorkInfo();

      employeeInfo.psnbasdocid = misEmployee.getEmployeeCode();
      employeeInfo.psncode = misEmployee.getEmployeeCode();
      employeeInfo.deptcode = misEmployee.getStore_Code();
      employeeInfo.psnname = misEmployee.getEmployeeName();
      employeeInfo.payname = misEmployee.getSalaryName();
      employeeInfo.nativeplace = misEmployee.getNation();
      employeeInfo.cardid = misEmployee.getID();
      if (misEmployee.getSex() == 1)
        employeeInfo.sex = "ÄÐ";
      else
        employeeInfo.sex = "Å®";
      employeeInfo.idaddress = misEmployee.getAddress();
      employeeInfo.homeaddress = misEmployee.getLocation();
      employeeInfo.paybank = misEmployee.getSalaryBank();
      employeeInfo.openacccity = misEmployee.getBankCity();
      employeeInfo.bankname = misEmployee.getBankName();
      employeeInfo.openacc = misEmployee.getBankAccount();
      employeeInfo.bloodtype = ((misEmployee.getBlood() == null) ? " " : misEmployee.getBlood());
      employeeInfo.nationly = misEmployee.getNative();
      employeeInfo.birthday = misEmployee.getBornDate();
      employeeInfo.polity = misEmployee.getPolity();
      employeeInfo.marriage = misEmployee.getMarry();
      employeeInfo.currentaddress = misEmployee.getResident();
      employeeInfo.tel = ((misEmployee.getTel() == null) ? " " : misEmployee.getTel());
      employeeInfo.mobile = misEmployee.getMobile();
      employeeInfo.email = ((misEmployee.getEMail() == null) ? " " : misEmployee.getEMail());
      employeeInfo.scieti = misEmployee.getEdcution();
      employeeInfo.ls = misEmployee.getHand();
      employeeInfo.urgcyname = misEmployee.getContactPerson();
      employeeInfo.urgcyaddress = misEmployee.getContactAds();
      employeeInfo.urgcytel = misEmployee.getContactTel();
      employeeInfo.urgcyrelation = misEmployee.getContactRelation();
      employeeInfo.urgcyunit = ((misEmployee.getContactCompany() == null) ? " " : misEmployee.getContactCompany());
      employeeInfo.Recruitchanne = misEmployee.getRecruitmentChannel();
      employeeInfo.hukou = misEmployee.getProperty();
      if (misEmployee.getSalaryPaymentType().equals("0001A3100000000000H5"))
      {
        employeeInfo.info1 = "Y";
      }
      else
      {
        employeeInfo.info1 = "";
      }
      if (misEmployee.getWorkType() == 4)
      {
        employeeInfo.psntype = "2";
      }
      else
      {
        employeeInfo.psntype = "0";
      }

      employeeInfo.pk_psncl = misEmployee.getEmployeeType();
      employeeInfo.indutydate = misEmployee.getJoinDate();
      employeeInfo.keyacctype = misEmployee.getSalaryPaymentType();
      employeeInfo.worktype = misEmployee.getLaborType();
      employeeInfo.currentdept = misEmployee.getStore_Code();

      employeeInfo.pk_omduty = " ";
      employeeInfo.jobname = misEmployee.getPositionName();
      employeeInfo.company = ((misEmployee.getContractCompany() == null) ? " " : misEmployee.getContractCompany());
      employeeInfo.maturity = ((misEmployee.getHealthExpiredDate() == null) ? " " : misEmployee.getHealthExpiredDate());
      employeeInfo.billhealthcode = ((misEmployee.getHealthNo() == null) ? " " : misEmployee.getHealthNo());
      employeeInfo.city = ((misEmployee.getCity() == null) ? " " : misEmployee.getCity());
      employeeInfo.status = misEmployee.getStatus();

      workInfo.psnbasdocid = misEmployee.getEmployeeCode();
      this.workPK = workInfo.psnbasdocid;
      workInfo.psncode = misEmployee.getEmployeeCode();
      workInfo.pk_psncl = misEmployee.getEmployeeType();
      workInfo.indutydate = misEmployee.getJoinDate();
      workInfo.groupdef3 = misEmployee.getSalaryPaymentType();
      workInfo.groupdef4 = misEmployee.getLaborType();
      workInfo.pk_deptdoc = misEmployee.getStore_Code();

      workInfo.pk_om_job = misEmployee.getPositionName();
      workInfo.groupdef7 = ((misEmployee.getContractCompany() == null) ? " " : misEmployee.getContractCompany());
      workInfo.groupdef1 = ((misEmployee.getHealthExpiredDate() == null) ? " " : misEmployee.getHealthExpiredDate());
      workInfo.groupdef9 = ((misEmployee.getHealthNo() == null) ? " " : misEmployee.getHealthNo());
      workInfo.ID = misEmployee.getID();
      workInfo.worktime = misEmployee.getWorkDate();
      if ((misEmployee.getCurrentSalary() == null) || (misEmployee.getCurrentSalary() == "") || (misEmployee.getCurrentSalary() == "null"))
      {
        workInfo.info1 = String.valueOf(misEmployee.getSalaryPerHour());
      }
      else
      {
        workInfo.info1 = misEmployee.getCurrentSalary();
      }
      workInfo.info1 = this.df.format(Double.parseDouble(workInfo.info1));
      employeeInfo.workInfo = workInfo;

      rst.add(employeeInfo);
    }
    return rst;
  }

  private String formatEmployeeSQL(EHR_EmployeeInfo ehrEmployeeInfo)
  {
    String strInsert = "insert into mid_personnel(psnbasdocid,psncode,psndocidpk,deptcode,psnname,payname,nativeplace,cardid,sex,idaddress,homeaddress,paybank,openacccity,bankname,openacc,bloodtype,nationly,birthday,polity,marriage,currentaddress,tel,mobile,email,scieti,ls,urgcyname,urgcyaddress,urgcytel,urgcyrelation,urgcyunit,psntype,pk_psncl,indutydate,keyacctype,worktype,currentdept,pk_omduty,jobname,Recruitchanne,hukou,company,maturity,billhealthcode,city,info1) values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";

    this.sql = 
      String.format(strInsert, new Object[] { 
      ehrEmployeeInfo.psnbasdocid, ehrEmployeeInfo.psncode, this.workPK, ehrEmployeeInfo.deptcode, ehrEmployeeInfo.psnname, 
      ehrEmployeeInfo.payname, ehrEmployeeInfo.nativeplace, ehrEmployeeInfo.cardid, ehrEmployeeInfo.sex, ehrEmployeeInfo.idaddress, 
      ehrEmployeeInfo.homeaddress, ehrEmployeeInfo.paybank, ehrEmployeeInfo.openacccity, ehrEmployeeInfo.bankname, ehrEmployeeInfo.openacc, 
      ehrEmployeeInfo.bloodtype, ehrEmployeeInfo.nationly, ehrEmployeeInfo.birthday, ehrEmployeeInfo.polity, ehrEmployeeInfo.marriage, 
      ehrEmployeeInfo.currentaddress, ehrEmployeeInfo.tel, ehrEmployeeInfo.mobile, ehrEmployeeInfo.email, ehrEmployeeInfo.scieti, 
      ehrEmployeeInfo.ls, ehrEmployeeInfo.urgcyname, ehrEmployeeInfo.urgcyaddress, ehrEmployeeInfo.urgcytel, ehrEmployeeInfo.urgcyrelation, 
      ehrEmployeeInfo.urgcyunit, ehrEmployeeInfo.psntype, ehrEmployeeInfo.pk_psncl, ehrEmployeeInfo.indutydate, ehrEmployeeInfo.keyacctype, 
      ehrEmployeeInfo.worktype, ehrEmployeeInfo.currentdept, "", ehrEmployeeInfo.jobname, ehrEmployeeInfo.Recruitchanne, 
      ehrEmployeeInfo.hukou, ehrEmployeeInfo.company, ehrEmployeeInfo.maturity, ehrEmployeeInfo.billhealthcode, ehrEmployeeInfo.city, ehrEmployeeInfo.info1 });

    return this.sql;
  }

  private String formatWorkSQL(EHR_WorkInfo ehrWorkInfo)
  {
    String strInsert = "Insert into mid_bd_psndoc(psnbasdocid,psncode,pk_psncl,indutydate,groupdef3,groupdef4,pk_deptdoc,pk_om_job,groupdef7,groupdef1,groupdef9,ID,worktime,info1) values( '%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";

    this.sql = 
      String.format(strInsert, new Object[] { 
      ehrWorkInfo.psnbasdocid, ehrWorkInfo.psncode, ehrWorkInfo.pk_psncl, ehrWorkInfo.indutydate, ehrWorkInfo.groupdef3, 
      ehrWorkInfo.groupdef4, ehrWorkInfo.pk_deptdoc, ehrWorkInfo.pk_om_job, ehrWorkInfo.groupdef7, 
      ehrWorkInfo.groupdef1, ehrWorkInfo.groupdef9, ehrWorkInfo.ID, ehrWorkInfo.worktime, ehrWorkInfo.info1 });
    return this.sql;
  }

  private void insertEHRData(List<EHR_EmployeeInfo> lists)
  {
    String errorCode = "";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      for (Iterator i = lists.iterator(); i.hasNext(); )
      {
        this.midConn.setAutoCommit(false);
        EHR_EmployeeInfo emp = (EHR_EmployeeInfo)i.next();
        errorCode = emp.psncode;
        try
        {
          String delEmpSQL = String.format("Delete From mid_personnel where psncode='%s'", new Object[] { emp.psncode });
          logger.debug(delEmpSQL);
          this.stmt = this.midConn.prepareStatement(delEmpSQL);
          this.stmt.execute();
          this.stmt.close();

          String empSQL = formatEmployeeSQL(emp);
          logger.debug(empSQL);
          this.stmt = this.midConn.prepareStatement(empSQL);
          this.stmt.execute();
          this.stmt.close();

          if (emp.status == 1)
          {
            String delWorkSQL = String.format("Delete From mid_bd_psndoc where psncode='%s'", new Object[] { emp.psncode });
            logger.debug(delWorkSQL);
            this.stmt = this.midConn.prepareStatement(delWorkSQL);
            this.stmt.execute();
            this.stmt.close();

            String workSQL = formatWorkSQL(emp.workInfo);
            logger.debug(workSQL);
            this.stmt = this.midConn.prepareStatement(workSQL);
            this.stmt.execute();
            this.stmt.close();
          }
          this.midConn.commit();
          this.employeeCodes.add(emp.psncode);
        }
        catch (Exception e)
        {
          this.errorCodes.add(errorCode);
          logger.error(e);
          logger.error(this.sql);
          try
          {
            this.midConn.rollback();
          }
          catch (Exception localException1) {
          }
        }
      }
    }
    catch (Exception e) {
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.midConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}