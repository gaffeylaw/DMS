package dgpt;

import common.ADFUtils;
import common.DmsLog;
import common.DmsUtils;

import common.JSFUtils;

import common.lov.DmsComBoxLov;

import common.lov.ValueSetRow;

import dcm.DcmDataDisplayBean;
import dcm.DcmQueryDescriptor;
import dcm.PcColumnDef;

import dcm.PcDataTableModel;

import dcm.PcExcel2003WriterImpl;
import dcm.PcExcel2007WriterImpl;

import dcm.SPRowReader;

import dms.login.Person;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Types;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import java.util.UUID;

import javax.el.ELContext;

import javax.el.ExpressionFactory;

import javax.el.MethodExpression;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichDialog;
import oracle.adf.view.rich.component.rich.RichPanelWindow;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.component.rich.output.RichPanelCollection;

import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

import org.apache.myfaces.trinidad.model.UploadedFile;

import org.hexj.excelhandler.reader.ExcelReaderUtil;

import sun.print.DialogTypeSelection;

import utils.system;

public class BPCostBean {
    private Person curUser;
    //页面绑定组件
    private RichPanelCollection panelaCollection;
    private RichPopup errorWindow;
    private RichInputFile fileInput;
    private RichPopup dataImportWnd;
    private RichPopup statusWindow;
    private RichPopup adminBlockPop;

    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();
    private FilterableQueryDescriptor queryDescriptor=new DcmQueryDescriptor();
    
    private String year;
    private List<SelectItem> yearList;
    private String entity;
    private String pLine;
    private String pname;
    private List<SelectItem> pnameList;
    private String version;
    private List<SelectItem> versionList;
    private String proType;
    private String hLine;
    private String yLine;
    private String pStart;
    private String pEnd;
    private String connectId;
    public static final String TYPE_BASE="BASE";
    private String isBlock;
    private boolean isManager;
    private boolean isEDITABLE ;
    private boolean isSelected;
    //是否是2007及以上格式
    private boolean isXlsx = true;
    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(DcmDataDisplayBean.class);
    private RichPopup dataExportWnd;
    private DmsComBoxLov proLov;
    DmsLog dmsLog = new DmsLog();
    private RichTable subTable;
    private RichTable subTable2;
    
    //复制到滚动计划成本
    private String newConnectId;
    private String delConnect="";

    public BPCostBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        if("10000".equals(this.curUser.getId())){
            isManager = true;
            isEDITABLE = true;
        }else{
            isManager = false;
            isEDITABLE = false;
        }
        isSelected = true;
        this.dataModel = new PcDataTableModel();
        List<Map> d = new ArrayList<Map>();
        this.dataModel.setWrappedData(d);
        this.initList();
    }

    private void initList(){
        this.yearList = queryYears("HLS_YEAR_C");
        this.pnameList = queryValues("PRO_PLAN_COST_HEADER","PROJECT_NAME");
        this.initProLov(pnameList);
        this.versionList = queryValues1("PRO_PLAN_COST_HEADER","VERSION");
    }
    
    private void initProLov(List<SelectItem> pnameList){
        List<ValueSetRow> vsl = new ArrayList<ValueSetRow>();
        for(SelectItem sim : pnameList){
            ValueSetRow vsr = new ValueSetRow(sim.getLabel(),sim.getLabel(),sim.getLabel());
            vsl.add(vsr);
        }
        this.proLov = new DmsComBoxLov(vsl);
    }

    //年份下拉列表
    private List<SelectItem> queryYears(String source){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT CODE,MEANING FROM "+ source;
        List<SelectItem> values = new ArrayList<SelectItem>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString("CODE"),rs.getString("MEANING"));
                values.add(sim);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
    
    //其他下拉列表
    private List<SelectItem> queryValues(String source,String col){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "";
        if(this.curUser.getId().equals("10000")){
            sql = "SELECT DISTINCT P."+col+" FROM "+source+" P WHERE " + 
                "P.DATA_TYPE='"+this.TYPE_BASE+"'";
        }else{
            sql = "SELECT DISTINCT P."+col+" FROM "+source+" P WHERE P.PROJECT_NAME IN (" + 
            "SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_PRIVILEGE_V T WHERE ID = '"+this.curUser.getId()+"'"+
                ") AND DATA_TYPE =\'"+this.TYPE_BASE+"\'";
        } 
        List<SelectItem> values = new ArrayList<SelectItem>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString(col),rs.getString(col));
                values.add(sim);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
    
    //版本下拉列表
    private List<SelectItem> queryValues1(String source,String col){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT "+col+",VERSION_NAME FROM "+source+" WHERE DATA_TYPE =\'"+this.TYPE_BASE+"\'";
        List<SelectItem> values = new ArrayList<SelectItem>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString(col),rs.getString(col)+"-"+rs.getString("VERSION_NAME"));
                values.add(sim);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
    //获取表头信息
    private String getCom(){
        String text = this.year+"_"+this.entity+"_"+this.hLine+"_"+this.yLine+"_"+
                      this.pLine+"_"+this.pname+"_"+this.version+"_"+this.proType;
        return text;
    }
    
    //项目名称下拉框change
    public void projectChange(ValueChangeEvent valueChangeEvent) {
        pname =(String) valueChangeEvent.getNewValue();
        if(year!=null&&pname!=null){
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql = "SELECT DISTINCT VERSION,VERSION_NAME FROM PRO_PLAN_COST_HEADER WHERE DATA_TYPE =\'"+this.TYPE_BASE+"\'"+
                " AND PROJECT_NAME='"+pname+"' AND HLS_YEAR = '"+this.year+"'";
            List<SelectItem> values = new ArrayList<SelectItem>();
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                while(rs.next()){
                    SelectItem sim = new SelectItem(rs.getString("VERSION"),rs.getString("VERSION")+"-"+rs.getString("VERSION_NAME"));
                    values.add(sim);
                }
                rs.close();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.versionList = values;
        }
        if(year==null||version==null||pname==null){
                return;
        }else{
            isSelected = false;
            this.queryData();
            this.createTableModel();
        }
    }
    
    //版本下拉框change
    public void versionChange(ValueChangeEvent valueChangeEvent) {
        version =(String) valueChangeEvent.getNewValue();
        if(year==null||version==null||pname==null){
            return;
        }else{
            isSelected = false;
            this.queryData();
            this.createTableModel();
        }
    }
    
    //年下拉框change
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        if(year!=null&&pname!=null){
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql = "SELECT DISTINCT VERSION,VERSION_NAME FROM PRO_PLAN_COST_HEADER WHERE DATA_TYPE =\'"+this.TYPE_BASE+"\'"+
                " AND PROJECT_NAME='"+pname+"' AND HLS_YEAR = '"+this.year+"'";
            List<SelectItem> values = new ArrayList<SelectItem>();
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                while(rs.next()){
                    SelectItem sim = new SelectItem(rs.getString("VERSION"),rs.getString("VERSION")+"-"+rs.getString("VERSION_NAME"));
                    values.add(sim);
                }
                rs.close();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.versionList = values;
        }
        if(year==null||version==null||pname==null){
            return;
        }else{
            isSelected = false;
            this.queryData();
            this.createTableModel();
        }
    }
   //数据查询--头
    public void queryData(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT ENTITY_NAME,PRODUCT_LINE,PROJECT_TYPE,INDUSTRY_LINE,BUSINESS_LINE,CONNECT_ID,PROJECT_START,PROJECT_END,"
                    + "IS_BLOCK FROM PRO_PLAN_COST_HEADER WHERE VERSION = \'"+version+"\'";
        sql = sql +" AND HLS_YEAR=\'"+year+"\'";
        sql = sql + " AND PROJECT_NAME=\'"+pname+"\'";
        sql = sql + " AND DATA_TYPE = \'"+this.TYPE_BASE+"\'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            entity="";
            pLine="";
            proType="";
            hLine="";
            yLine="";
            connectId="";
            isBlock = "false";
            while(rs.next()){
                entity = rs.getString("ENTITY_NAME");
                pLine = rs.getString("PRODUCT_LINE");
                proType = rs.getString("PROJECT_TYPE");
                hLine = rs.getString("INDUSTRY_LINE");
                yLine = rs.getString("BUSINESS_LINE");
                connectId=rs.getString("CONNECT_ID");
                pStart = rs.getString("PROJECT_START");
                pEnd = rs.getString("PROJECT_END");
                isBlock = rs.getString("IS_BLOCK");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //构建数据表
    public void createTableModel(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        LinkedHashMap<String,String> labelMap = getLabelMap();
        String sql = this.querySql(labelMap);
        List<Map> data = new ArrayList<Map>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql.toString());
            while(rs.next()){
                Map row = new HashMap();
                for(Map.Entry<String,String> entry:labelMap.entrySet()){
                    if(entry.getValue().equals("PLAN_COST") || entry.getValue().startsWith("Y") ||
                       entry.getValue().equals("OCCURRED") ){
                        row.put(entry.getValue(), this.getPrettyNumber(rs.getString(entry.getValue())));
                    }else{
                        row.put(entry.getValue(), rs.getString(entry.getValue()));
                    }
                }
                row.put("ROW_ID", rs.getString("ROW_ID"));
                row.put("LGF_NUM", rs.getString("LGF_NUM"));
                row.put("LGF_TYPE", rs.getString("LGF_TYPE"));
                row.put("PLAN_QUANTITY", rs.getString("PLAN_QUANTITY"));
                row.put("PLAN_AMOUNT", rs.getString("PLAN_AMOUNT"));
                row.put("OCCURRED_QUANTITY", rs.getString("OCCURRED_QUANTITY"));
                row.put("OCCURRED_AMOUNT", rs.getString("OCCURRED_AMOUNT"));
                data.add(row);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.dataModel.setWrappedData(data);
        ((PcDataTableModel)this.dataModel).setLabelMap(labelMap);
        this.selectIsEditable();
    }
    //调整数字显示格式
    public static String getPrettyNumber(String number) {  
        if(number == null) return "";
        if(number.equals("0.0")){
            number = "";    
        }
        if(number.startsWith(".")){
            number = "0" + number;    
        }
        while(number.contains(".")&&number.endsWith("0")){
            number = number.substring(0,number.length()-2);    
        }
        return number;  
    }
    
    //查询语句
    private String querySql(LinkedHashMap<String,String> labelMap){
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        for(Map.Entry<String,String> entry : labelMap.entrySet()){
            sql.append(entry.getValue()).append(",");
        }
        sql.append("ROWID AS ROW_ID,LGF_NUM,LGF_TYPE,PLAN_QUANTITY,PLAN_AMOUNT," +
            "OCCURRED_QUANTITY,OCCURRED_AMOUNT FROM PRO_PLAN_COST_BODY WHERE CONNECT_ID = '").append(connectId).append("'");
        sql.append(" AND DATA_TYPE = '").append(this.TYPE_BASE).append("' ORDER BY WBS,NETWORK,TO_NUMBER(WORK_CODE)");
        return sql.toString();
    }
    //一行中，列的map
    private LinkedHashMap<String,String> getLabelMap(){
        if(pStart == null || pEnd == null){
            return new LinkedHashMap<String,String>();    
        }
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        labelMap.put("WBS", "WBS");
        labelMap.put("网络号", "NETWORK");
        labelMap.put("作业号", "WORK_CODE");
        labelMap.put("作业活动","WORK");
        labelMap.put("预算项编码", "TERM_CODE");
        labelMap.put("预算项","TERM");
        labelMap.put("预算科目", "COST_DETAIL");
        labelMap.put("工作中心","CENTER");
        labelMap.put("作业类型","WORK_TYPE");
        labelMap.put("物料编码","BOM_CODE");
        labelMap.put("单位","UNIT");
        labelMap.put("计划成本","PLAN_COST");
        labelMap.put("累计实际成本（初始化）", "OCCURRED");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
        List<Date> monthList;
        Date start;
        Date end ;
        try {
            start = sdf.parse(pStart);
            end = sdf.parse(pEnd);
            monthList = this.findDates(start, end);
            for(int i = 0 ; i < monthList.size() ; i++){
                labelMap.put(sdf.format(monthList.get(i)), "Y"+sdf.format(monthList.get(i)));
            }
            //labelMap.put("KEY"+(monthList.size()+10),"SUM_AFTER_JUL");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int flag = 1;
        boolean isReadonly = true;
        this.pcColsDef.clear();
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            if(flag>13){
                isReadonly = false;
            }
            if(flag<=11){
                PcColumnDef newCol = new PcColumnDef(map.getKey(),map.getValue(),isReadonly,"");
                this.pcColsDef.add(newCol);
            }else{
                PcColumnDef newCol = new PcColumnDef(map.getKey(),map.getValue(),isReadonly,"NUMBER");
                this.pcColsDef.add(newCol);
            }
            flag++;
            
        }
        ((PcDataTableModel)this.dataModel).setPcColsDef(this.pcColsDef);
        this.pcColsDef.add(new PcColumnDef("ROW_ID","ROW_ID",false,""));
        return labelMap;
    }
    
    //时间段
    public static List<Date> findDates(Date dBegin, Date dEnd) {  
            List lDate = new ArrayList();  
            lDate.add(dBegin);  
            Calendar calBegin = Calendar.getInstance();  
            // 使用给定的 Date 设置此 Calendar 的时间    
            calBegin.setTime(dBegin);  
            Calendar calEnd = Calendar.getInstance();  
            // 使用给定的 Date 设置此 Calendar 的时间    
            calEnd.setTime(dEnd);  
            // 测试此日期是否在指定日期之后    
            while (dEnd.after(calBegin.getTime())) {  
                // 根据日历的规则，为给定的日历字段添加或减去指定的时间量    
                calBegin.add(Calendar.MONTH, 1);  
                lDate.add(calBegin.getTime());  
            }  
            return lDate;  
        } 
    //
    public void rowSelectionListener(SelectionEvent selectionEvent) {
        RichTable table = (RichTable)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = rks.iterator().next();
            table.setRowKey(rowKey);
        }
    }
    //保存
    public void operation_save(ActionEvent actionEvent) {
        //清楚临时表错误表数据
        this.deleteTempAndError();
        //保存到临时表
        this.goToTemp();
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        //执行校验
        if(this.validation()){
            this.inputPro();
            dmsLog.operationLog(this.curUser.getAcc(),this.TYPE_BASE+"_"+this.connectId,this.getCom(),"UPDATE");
            for(Map<String,String> rowdata : modelData){
                if("UPDATE".equals(rowdata.get("OPERATION"))){
                    rowdata.put("OPERATION", null);
                }
            }
        }else{
            this.showErrorPop();
        }
    }
    
    public void goToTemp(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        StringBuffer sql = new StringBuffer();
        StringBuffer sql_value = new StringBuffer();
        sql_value.append(" VALUES(");
        sql.append("INSERT INTO PRO_PLAN_COST_BODY_TEMP(") ;
        //构建sql语句
        LinkedHashMap<String,String> map = this.getLabelMap();
        int last = map.size()+1;
        for(Map.Entry<String,String> entry : map.entrySet()){
            sql.append(entry.getValue()+",");
            sql_value.append("?,");
        }
        sql.append("ROW_ID,CONNECT_ID,CREATED_BY,ROW_NO,DATA_TYPE,");
        sql.append("LGF_NUM,LGF_TYPE,PLAN_QUANTITY,PLAN_AMOUNT,OCCURRED_QUANTITY,OCCURRED_AMOUNT)");
        sql_value.append("?,\'"+connectId+"\',\'"+this.curUser.getId()+"\',?,\'"+this.TYPE_BASE+"\',");
        sql_value.append("?,?,?,?,?,?)");
        PreparedStatement stmt = trans.createPreparedStatement(sql.toString()+sql_value.toString(), 0);
        //获取数据
        int rowNum = 1;
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        for(Map<String,String> rowdata : modelData){
            //if("UPDATE".equals(rowdata.get("OPERATION"))){
                try {
                    int i =1;
                    for(Map.Entry<String,String> entry : map.entrySet()){
                        stmt.setString(i++ , rowdata.get(entry.getValue()));
                    }
                    stmt.setString(last, rowdata.get("ROW_ID"));
                    stmt.setInt(last+1,rowNum);
                    stmt.setString(last+2, rowdata.get("LGF_NUM"));
                    stmt.setString(last+3, rowdata.get("LGF_TYPE"));
                    stmt.setString(last+4, rowdata.get("PLAN_QUANTITY"));
                    stmt.setString(last+5, rowdata.get("PLAN_AMOUNT"));
                    stmt.setString(last+6, rowdata.get("OCCURRED_QUANTITY"));
                    stmt.setString(last+7, rowdata.get("OCCURRED_AMOUNT"));
                    rowNum++;
                    stmt.addBatch();
                    stmt.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            //}        
        }
        trans.commit();
    }
    //清空临时表和错误表
    public void deleteTempAndError(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        //清空临时表数据
        String sqldelete = "DELETE FROM PRO_PLAN_COST_BODY_TEMP T WHERE T.CREATED_BY = \'"+this.curUser.getId()+"\'";
        Statement st = trans.createStatement(DBTransaction.DEFAULT);
        try {
            st.executeUpdate(sqldelete);
            trans.commit();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //清空错误表数据
        String sqlError = "DELETE FROM PRO_PLAN_COST_ERROR T WHERE T.CREATED_BY = \'"+this.curUser.getId()+"\'";
        Statement sta = trans.createStatement(DBTransaction.DEFAULT);
        try {
            sta.executeUpdate(sqlError);
            trans.commit();
            sta.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //校验程序
    public boolean validation(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
            CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.BPC_VALIDATION(?,?,?)}", 0);
            try {
                cs.setString(1, this.curUser.getId());
                cs.setString(2, this.TYPE_BASE.toString());
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.execute();
                if("N".equals(cs.getString(3))){
                    flag = false;
                }
                cs.close();
                trans.commit();
            } catch (SQLException e) {
                flag = false;
                e.printStackTrace();
            }
//        }
        
        return flag;
    }
    //导入程序
    public void inputPro(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.BPC_INPUTPRO(?)}", 0);
        try {
            cs.setString(1,this.curUser.getId() );
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //取消
    public void operation_reset(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().rollback();
        this.queryData();
        this.createTableModel();
    }

    //选中行，修改
    public void valueChangeLinstener(ValueChangeEvent valueChangeEvent) {
        Map rowMap = (Map)this.dataModel.getRowData();
        if (((PcDataTableModel)this.dataModel).getSelectedRows().size() > 1) {
            String msg =DmsUtils.getMsg("dcm.msg.can_not_select_multiple_row");
            JSFUtils.addFacesInformationMessage(msg);
            return;
        }
        if(rowMap.get("OPERATION") == null){
            rowMap.put("OPERATION", PcDataTableModel.OPERATE_UPDATE);    
        }                
    }
    //导出excel
    public void operation_export(FacesContext facesContext, OutputStream outputStream) {
        this.dataExportWnd.cancel();
        String type = this.isXlsx ? "xlsx" : "xls";
        try {
            if("xls".equals(type)){
                PcExcel2003WriterImpl writer = new PcExcel2003WriterImpl(
                                                   this.querySql(this.getLabelMap()),
                                                   "基准计划成本",
                                                    this.pcColsDef,
                                                    outputStream);
                writer.writeToFile();
            }else{
                PcExcel2007WriterImpl writer = new PcExcel2007WriterImpl(
                                                    this.querySql(this.getLabelMap()),
                                                    2,this.pcColsDef);
                writer.process(outputStream, "基准计划成本");
                outputStream.flush();
            }
        } catch (Exception e) {
            this._logger.severe(e);
        } 
       dmsLog.operationLog(this.curUser.getAcc(),this.TYPE_BASE+"_"+this.connectId,this.getCom(),"EXPORT");
    }
    
    //导出文件名
    public String getExportDataExcelName(){
        if(isXlsx){
            return "基准计划成本_"+this.connectId+".xlsx";
        }else{
            return "基准计划成本_"+this.connectId+".xls";
        }
    }
    
    public void showErrorPop(){
        ViewObject vo = ADFUtils.findIterator("ProPlanCostViewIterator").getViewObject();
        vo.setNamedWhereClauseParam("dataType", this.TYPE_BASE);
        vo.executeQuery();
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.errorWindow.show(ph);
    }
    
    public void beBlocked(ActionEvent actionEvent) {
        this.deleteTempAndError();
        //保存到临时表
        this.goToTemp();
        if(this.validation()){
            this.inputPro();
            String sql = "UPDATE PRO_PLAN_COST_HEADER SET (IS_BLOCK) = 'true' WHERE HLS_YEAR = \'"+year;
            sql = sql + "\' AND PROJECT_NAME =\'"+pname+"\' AND VERSION=\'"+version+"\'";
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            int flag =-1;
            try {
                flag = stat.executeUpdate(sql);
                trans.commit();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(flag!=-1){
                isBlock = "true";
            }
        }else{
            this.showErrorPop();
        }
        
    }

    public void outBlock(ActionEvent actionEvent) {
        String sql = "UPDATE PRO_PLAN_COST_HEADER SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+year;
        sql = sql + "\' AND PROJECT_NAME =\'"+pname+"\' AND VERSION=\'"+version+"\'";
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        int flag =-1;
        try {
            flag = stat.executeUpdate(sql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(flag!=-1){
            isBlock = "false";
        }
    }
    
    //导入按钮触发事件
    public void operation_import(ActionEvent actionEvent) {
        this.dataImportWnd.cancel();
        //表头为空
        if(this.year==null||this.pname==null||this.version==null){
            JSFUtils.addFacesErrorMessage("请选择表头信息");
            return;
        }
        //上传文件为空
        if (null == this.fileInput.getValue()) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.plz_select_import_file"));
            return;
        }
        //获取文件上传路径
        String filePath = this.uploadFile();
        
        if (null == filePath) {
            return;
        }
        
        //读取excel数据到数据库临时表
        try {
            if (!this.handleExcel(filePath, connectId)) {
            return;
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.fileInput.resetValue();
        //校验程序
        if(this.validation_import()){
                DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
                Statement stat = trans.createStatement(DBTransaction.DEFAULT);
                StringBuffer sql = new StringBuffer();
                sql.append("UPDATE PRO_PLAN_COST_BODY_TEMP T SET(T.PLAN_QUANTITY,T.PLAN_AMOUNT,T.OCCURRED_QUANTITY,T.OCCURRED_AMOUNT,T.LGF_NUM,T.LGF_TYPE) ")
                    .append("=(SELECT P.PLAN_QUANTITY,P.PLAN_AMOUNT,P.OCCURRED_QUANTITY,P.OCCURRED_AMOUNT,P.LGF_NUM,P.LGF_TYPE FROM PRO_PLAN_COST_BODY P WHERE P.CONNECT_ID = '").append(this.connectId)
                    .append("' AND T.ROW_ID = P.ROWID)").append(" WHERE T.CONNECT_ID='").append(this.connectId).append("'");
                try {
                    stat.executeUpdate(sql.toString());
                    trans.commit();
                    stat.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if(this.validation()){
                this.inputPro_import();
//                this.createTableModel();
            }else{
                this.showErrorPop();
            }
            }else {
            //若出现错误则显示错误信息提示框
            //JSFUtils.addFacesErrorMessage("WBS等字段不可修改");
            this.showErrorPop();
//            return;
        }
        //刷新数据
        this.createTableModel();
        dmsLog.operationLog(this.curUser.getAcc(),this.TYPE_BASE+"_"+this.connectId,this.getCom(),"IMPORT");
    }
    
    //校验程序
    public boolean validation_import(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.BPCIMPORT_VALIDATION(?,?,?,?)}", 0);
        try {
            cs.setString(1, this.curUser.getId());
            cs.setString(2, this.TYPE_BASE);
            cs.setString(3,this.connectId);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            if("N".equals(cs.getString(4))){
                flag = false;
            }
            cs.close();
            trans.commit();
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
    
    //导入导入
    public void inputPro_import(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.BPCIMPORT_INPUTPRO(?,?)}", 0);
        try {
            cs.setString(1,this.curUser.getId() );
            cs.setString(2,this.connectId);
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //读取excel数据到临时表
    private boolean handleExcel(String fileName, String curComRecordId) throws SQLException {
        DBTransaction trans =(DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        String combinationRecord = ObjectUtils.toString(curComRecordId);
        //清空已有临时表数据
        this.deleteTempAndError();
        UploadedFile file = (UploadedFile)this.fileInput.getValue();
        String fname = file.getFilename();
        String name = fname.substring(fname.indexOf("_")+1, fname.indexOf("."));
        if(!name.equals(this.connectId)){
            JSFUtils.addFacesErrorMessage("请选择正确的文件");
            return false;
        }
        SPRowReader spReader = new SPRowReader(trans,2,this.connectId,this.pcColsDef,this.curUser.getId(),this.TYPE_BASE,name);
        try {
                ExcelReaderUtil.readExcel(spReader, fileName, true);
                spReader.close();
            } catch (Exception e) {
                this._logger.severe(e);
                JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.excel_handle_error"));
                return false;
            }
        return true;
    }
    
    //文件上传
    private String uploadFile() {
        UploadedFile file = (UploadedFile)this.fileInput.getValue();
        if (!(file.getFilename().endsWith(".xls") ||
              file.getFilename().endsWith(".xlsx"))) {
            String msg = DmsUtils.getMsg("dcm.file_format_error");
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        File dmsBaseDir = new File("DMS/UPLOAD/基准计划成本" );
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        String fileName = "DMS/UPLOAD/" + "基准计划成本" + "/"+file.getFilename();
        try {
            InputStream inputStream = file.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);
            BufferedInputStream bufferedInputStream =new BufferedInputStream(inputStream);
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(outputStream);
            byte[] buffer = new byte[10240];
            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(buffer, 0, 10240)) !=-1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
            bufferedOutputStream.flush();
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            file.dispose();
        } catch (IOException e) {
            this._logger.severe(e);
            String msg = DmsUtils.getMsg("dcm.file_upload_error");
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        return (new File(fileName)).getAbsolutePath();
    }

    //显示冻结状态信息
    public void showBlockStatusPop(){
            ViewObject vo = ADFUtils.findIterator("PTBlockStatusIterator").getViewObject();
            vo.setNamedWhereClauseParam("dataType", this.TYPE_BASE);
            vo.setNamedWhereClauseParam("userId", this.curUser.getId());
            vo.executeQuery();
            RichPopup.PopupHints ph = new RichPopup.PopupHints();
            this.statusWindow.show(ph);
    }
    //判断显示哪个pop
    public void blockStatusPop(ActionEvent actionEvent) {
        if(this.curUser.getId().equals("10000")){
            this.showAdminStatusPop();
        }else{
            this.showBlockStatusPop();
        }
    }
    
    //显示admin用户冻结状态信息
    public void showAdminStatusPop(){
        ViewObject vo = ADFUtils.findIterator("adminBlockStatusIterator").getViewObject();
        vo.setNamedWhereClauseParam("dataType", this.TYPE_BASE);
//        vo.setNamedWhereClauseParam("userAcc", this.curUser.getAcc());
        vo.executeQuery();
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.adminBlockPop.show(ph);
    }
    
    //查询是否为可编辑此项目
    public void selectIsEditable(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT EDITABLE FROM SAP_DMS_PROJECT_PRIVILEGE_V WHERE ID = '"+this.curUser.getId()+"' " +
            "AND PRO_CODE ||'-'||PRO_DESC = '"+this.pname+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                if("Y".equals(rs.getString("EDITABLE"))){
                    isEDITABLE= true;
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void goUnBlock(ActionEvent actionEvent) {
        if(this.subTable.getSelectedRowKeys() == null) return ;
        RowKeySet rsk = this.subTable.getSelectedRowKeys();
        ViewObject vo = ADFUtils.findIterator("adminBlockStatusIterator").getViewObject();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Iterator itr = rsk.iterator();
        try {
            while(itr.hasNext()){
                List ls = (List)itr.next();
                Key key =(Key)ls.get(0);
                Row row = vo.getRow(key);
                StringBuffer sql = new StringBuffer();
                if(row.getAttribute("IsBlock").equals("已冻结")){
                    sql.append("UPDATE PRO_PLAN_COST_HEADER SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+row.getAttribute("Code"))
                        .append("' AND PROJECT_NAME = '"+row.getAttribute("ProjectName"))
                        .append("' AND IS_BLOCK='true' ").append("AND DATA_TYPE = '"+this.TYPE_BASE+"'");
                    stat.executeUpdate(sql.toString());
                }
                if(row.getAttribute("ProjectName").equals(this.pname)){
                    this.isBlock = "false";
                }
            }
            stat.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        vo.executeQuery();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.subTable);
    }
    
    public void goUnBlock2(ActionEvent actionEvent) {
        if(this.subTable2.getSelectedRowKeys() == null) return ;
        RowKeySet rsk = this.subTable2.getSelectedRowKeys();
        ViewObject vo = ADFUtils.findIterator("adminBlockStatusIterator").getViewObject();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Iterator itr = rsk.iterator();
        try {
            while(itr.hasNext()){
                List ls = (List)itr.next();
                Key key =(Key)ls.get(0);
                Row row = vo.getRow(key);
                StringBuffer sql = new StringBuffer();
                if(row.getAttribute("IsBlock").equals("已冻结")){
                    sql.append("UPDATE PRO_PLAN_COST_HEADER SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+row.getAttribute("Code"))
                        .append("' AND PROJECT_NAME = '"+row.getAttribute("ProjectName"))
                        .append("' AND IS_BLOCK='true' ").append("AND DATA_TYPE = '"+this.TYPE_BASE+"'");
                    stat.executeUpdate(sql.toString());
                }
                if(row.getAttribute("ProjectName").equals(this.pname)){
                    this.isBlock = "false";
                }
            }
            stat.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        vo.executeQuery();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.subTable2);
    }
    
    public void setDataImportWnd(RichPopup dataImportWnd) {
        this.dataImportWnd = dataImportWnd;
    }

    public RichPopup getDataImportWnd() {
        return dataImportWnd;
    }

    public void setStatusWindow(RichPopup statusWindow) {
        this.statusWindow = statusWindow;
    }

    public RichPopup getStatusWindow() {
        return statusWindow;
    }

    public void setAdminBlockPop(RichPopup adminBlockPop) {
        this.adminBlockPop = adminBlockPop;
    }

    public RichPopup getAdminBlockPop() {
        return adminBlockPop;
    }

    public void setIsEDITABLE(boolean isEDITABLE) {
        this.isEDITABLE = isEDITABLE;
    }

    public boolean isIsEDITABLE() {
        return isEDITABLE;
    }
    
    public void setPanelaCollection(RichPanelCollection panelaCollection) {
        this.panelaCollection = panelaCollection;
    }
    
    public RichPanelCollection getPanelaCollection() {
        return panelaCollection;
    }

    public void setPcColsDef(List<PcColumnDef> pcColsDef) {
        this.pcColsDef = pcColsDef;
    }

    public List<PcColumnDef> getPcColsDef() {
        return pcColsDef;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setYearList(List<SelectItem> yearList) {
        this.yearList = yearList;
    }

    public List<SelectItem> getYearList() {
        return yearList;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public void setPLine(String pLine) {
        this.pLine = pLine;
    }

    public String getPLine() {
        return pLine;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPname() {
        return pname;
    }

    public void setPnameList(List<SelectItem> pnameList) {
        this.pnameList = pnameList;
    }

    public List<SelectItem> getPnameList() {
        return pnameList;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersionList(List<SelectItem> versionList) {
        this.versionList = versionList;
    }

    public List<SelectItem> getVersionList() {
        return versionList;
    }

    public void setProType(String proType) {
        this.proType = proType;
    }

    public String getProType() {
        return proType;
    }

    public void setHLine(String hLine) {
        this.hLine = hLine;
    }

    public String getHLine() {
        return hLine;
    }

    public void setYLine(String yLine) {
        this.yLine = yLine;
    }

    public String getYLine() {
        return yLine;
    }

    public void setDataModel(CollectionModel dataModel) {
        this.dataModel = dataModel;
    }

    public CollectionModel getDataModel() {
        return dataModel;
    }

    public void setPStart(String pStart) {
        this.pStart = pStart;
    }

    public String getPStart() {
        return pStart;
    }

    public void setPEnd(String pEnd) {
        this.pEnd = pEnd;
    }

    public String getPEnd() {
        return pEnd;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setQueryDescriptor(FilterableQueryDescriptor queryDescriptor) {
        this.queryDescriptor = queryDescriptor;
    }

    public FilterableQueryDescriptor getQueryDescriptor() {
        return queryDescriptor;
    }

    public void setIsXlsx(boolean isXlsx) {
        this.isXlsx = isXlsx;
    }

    public boolean isIsXlsx() {
        return isXlsx;
    }

    public void setDataExportWnd(RichPopup dataExportWnd) {
        this.dataExportWnd = dataExportWnd;
    }

    public RichPopup getDataExportWnd() {
        return dataExportWnd;
    }
    public void setIsBlock(String isBlock) {
        this.isBlock = isBlock;
    }

    public String getIsBlock() {
        return isBlock;
    }

    public void setErrorWindow(RichPopup errorWindow) {
        this.errorWindow = errorWindow;
    }

    public RichPopup getErrorWindow() {
        return errorWindow;
    }

    public void errorPop(ActionEvent actionEvent) {
        this.showErrorPop();
    }
    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    public boolean isIsManager() {
        return isManager;
    }
    public void setFileInput(RichInputFile fileInput) {
        this.fileInput = fileInput;
    }

    public RichInputFile getFileInput() {
        return fileInput;
    }

    public void setProLov(DmsComBoxLov proLov) {
        this.proLov = proLov;
    }

    public DmsComBoxLov getProLov() {
        return proLov;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public void setSubTable(RichTable subTable) {
        this.subTable = subTable;
    }

    public RichTable getSubTable() {
        return subTable;
    }

    public void setSubTable2(RichTable subTable2) {
        this.subTable2 = subTable2;
    }

    public RichTable getSubTable2() {
        return subTable2;
    }

    public void copyToRpcost(ActionEvent actionEvent) {
        String s = UUID.randomUUID().toString();
        String sql = "SELECT COUNT(1) COUNT FROM PRO_PLAN_COST_HEADER T WHERE T.HLS_YEAR = '"+this.year+"' " +
            "AND T.PROJECT_NAME = '"+this.pname+"' AND T.DATA_TYPE = 'ROLL'";
        System.out.println(sql);
        String sqlConnect = "SELECT T.CONNECT_ID FROM PRO_PLAN_COST_HEADER T WHERE T.HLS_YEAR = '"+this.year+"' " +
            "AND T.PROJECT_NAME = '"+this.pname+"' AND T.DATA_TYPE = 'ROLL'";
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            int count = -1;
            while(rs.next()){
                count = Integer.parseInt(rs.getString("COUNT"));
            }
            if(count==0){
                //滚动计划成本没有任何一版数据，则直接同步
                this.newConnectId = s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
                if(this.copyValidate("COPY")){
                    this.popupMessage("info", "复制成功");
                }
            }else if(count==1){  
                ResultSet rsConnect = stat.executeQuery(sqlConnect);
                while(rsConnect.next()){
                    this.delConnect = rsConnect.getString("CONNECT_ID");
                    System.out.println(this.delConnect);
                }
                this.showPopupActionListener(actionEvent, "将覆盖滚动计划成本第一版本，覆盖后不可撤回", 
                                             "#{viewScope.BPCostBean.coverFirst}");
            }else if(count>=2){
                this.popupMessage("error", "滚动计划成本第二版本已存在，不可同步");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void coverFirst(DialogEvent dialogEvent){
        if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
            String s = UUID.randomUUID().toString();
            this.newConnectId = s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
            //先复制，后删除
            if(this.copyValidate("DELETE")){
                this.popupMessage("info", "复制成功");
            }
        }
    }
    
    /**
         * 创建Popup、Dialog及弹出该Popup
         * @param actionEvent
         * @param msg
         * @param exp
         */
        public void showPopupActionListener(ActionEvent actionEvent, String msg, String exp) {
            UIComponent comp = actionEvent.getComponent().getParent();
            RichPopup popup = new RichPopup();
            RichDialog dialog = new RichDialog();
            dialog.setTitle("提示");
            RichOutputText opText = new RichOutputText();
            opText.setValue(msg);
            dialog.getChildren().add(opText);
            FacesContext fctx = FacesContext.getCurrentInstance();
            ELContext elctx = fctx.getELContext();
            Application jsfApp = fctx.getApplication();
            ExpressionFactory exprFactory = jsfApp.getExpressionFactory();
            MethodExpression methodExpr = null;
            methodExpr = exprFactory.createMethodExpression(elctx, exp, null, new Class[] { DialogEvent.class });
            dialog.setDialogListener(methodExpr);
            popup.getChildren().add(dialog);
            comp.getChildren().add(popup);
            
            RichPopup.PopupHints ph = new RichPopup.PopupHints();
            popup.show(ph);
        }
    
    /**
        * 弹出提示窗口
        * @param level 级别（默认info）
        * @param detail 提示信息。
        */
     public static void popupMessage(String level,String detail){        
       FacesMessage fm = new FacesMessage(null,detail);//默认级别为info
       fm.setSummary("系统提示：");
       if("info".equals(level))
         fm.setSeverity(fm.SEVERITY_INFO);
       else if("warn".equals(level))
         fm.setSeverity(fm.SEVERITY_WARN);
       else if("error".equals(level))
         fm.setSeverity(fm.SEVERITY_ERROR);
       else if("fatal".equals(fm.SEVERITY_FATAL))
         fm.setSeverity(fm.SEVERITY_FATAL);
       FacesContext.getCurrentInstance().addMessage(null, fm);
       }
    
    public boolean copyValidate(String flags){
        boolean flag=true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.BPCOST_TORPCOST(?,?,?,?,?)}", 0);
        try {
            cs.setString(1,this.connectId );
            cs.setString(2,this.newConnectId);
            cs.setString(3, flags);
            cs.setString(4, this.delConnect);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.execute();
            System.out.println(cs.getString(5));
            if("N".equals(cs.getString(5))){
                System.out.println(flag);
                flag = false;
            }
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        System.out.println("end:"+flag);
        return flag;
    }

}
