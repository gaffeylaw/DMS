package dms.quartz.job;

import dms.quartz.core.QuartzSchedulerSingleton;
import dms.quartz.utils.RowReader;

import dms.quartz.utils.DBConnUtils;

import java.io.Serializable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import org.hexj.excelhandler.reader.ExcelReaderUtil;
import dms.quartz.core.QuartzSchedulerSingleton;

import dms.quartz.utils.JobUtils;

import java.util.Date;

public class DBJob implements Job, Serializable {
    private static final long serialVersionUID = 1L;

    public DBJob() {
        super();
    }

    public void execute(JobExecutionContext context) {


        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();


        String connType = jobDataMap.getString("connType");


        if ("jdbcURL".equals(connType)) {



        } else if ("jdbcDS".equals(connType)) {

            String jndiName = jobDataMap.getString("jndiName");
            String jobId = jobDataMap.getString("jobName");

            DBConnUtils dbUtils = new DBConnUtils();

            try {

                Connection conn =
                    dbUtils.getJNDIConnectionByContainer(jndiName);
                //System.out.println("ssss:"+conn);
                //start by wtg
                int startLine = Integer.parseInt(jobDataMap.getString("startLine"));
                String tempId = jobDataMap.getString("tempId");
                String comId = jobDataMap.getString("comId");
                String tempTable = jobDataMap.getString("tempTable");
                int colSize = Integer.parseInt(jobDataMap.getString("colSize"));
                String userId = jobDataMap.getString("userId");
                String tempName = jobDataMap.getString("tempName");
                String filePath = jobDataMap.getString("filePath");
                String impPro = jobDataMap.getString("impPro");
                String afterPro = jobDataMap.getString("afterPro");
                String mode = jobDataMap.getString("mode");
                String locale = jobDataMap.getString("locale");
                
                RowReader reader = new RowReader(conn,startLine,tempId,comId,tempTable,colSize,userId,tempName);
                ExcelReaderUtil.readExcel(reader, filePath, true);
                reader.close();
                conn.commit();
                //执行导入程序
                CallableStatement cs = conn.prepareCall("{CALl "+impPro+"(?,?,?,?,?)}");
                cs.setString(1, tempId);
                cs.setString(2, comId);
                cs.setString(3, userId);
                cs.setString(4, mode);
                cs.setString(5, locale);
                cs.execute();
                
                //执行善后程序
                if(afterPro != null && !"".equals(afterPro)){
                    CallableStatement css = conn.prepareCall("{CALl "+afterPro+"(?,?,?,?,?)}");
                    css.setString(1, tempId);
                    css.setString(2, comId);
                    css.setString(3, userId);
                    css.setString(4, mode);
                    css.setString(5, locale);
                    css.execute();
                    css.close();
                }
                //end by wtg
                cs.close();
                conn.close();

            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("DBJob 数据库存储过程调用失败，请检查jdbc DS信息!");
            }

            JobUtils jobUtils = new JobUtils();
            jobUtils.updateJobStatus(jndiName, jobId);
        }


    }
}
