package dcm;


import common.ADFUtils;
import common.DmsUtils;
import common.JSFUtils;
import common.TablePagination;

import oracle.adf.view.rich.component.rich.input.RichSelectOneListbox;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;

import oracle.adf.view.rich.event.ColumnSelectionEvent;
import oracle.adf.view.rich.render.ClientEvent;
import oracle.adf.view.rich.component.rich.output.RichProgressIndicator;

import oracle.adf.view.rich.event.LaunchPopupEvent;

import oracle.jbo.domain.Number;

import dcm.combinantion.CombinationEO;

import dcm.template.TemplateEO;

import dms.dynamicShell.TabContext;

import dms.login.Person;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPoll;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.output.RichPanelCollection;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;


import oracle.jbo.ApplicationModule;
import oracle.jbo.HiddenDefException;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;
import oracle.jbo.jbotester.load.SimpleDateFormatter;
import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.PollEvent;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;
import org.apache.myfaces.trinidad.event.RowDisclosureEvent;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.event.SortEvent;
import org.apache.myfaces.trinidad.model.BoundedRangeModel;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.SortCriterion;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.hexj.excelhandler.reader.ExcelReaderUtil;

import team.epm.dcm.view.DcmCombinationViewRowImpl;
import team.epm.dcm.view.DcmTemplateColumnViewRowImpl;
import team.epm.dcm.view.DcmTemplateViewRowImpl;

import workapproveflow.ApproveflowEngine;
import workapproveflow.WorkflowEngine;


public class DcmDataDisplayBean extends TablePagination {
    private CollectionModel dataModel;
    //模板信息
    private TemplateEO curTempalte;
    //模板列信息
    private List<ColumnDef> colsdef = new ArrayList<ColumnDef>();
    //组合信息
    private CombinationEO curCombiantion;
    //是否增量导入
    private boolean isIncrement = true;
    //是否是2007及以上格式
    private boolean isXlsx = true;
    //组合信息
    private List<ComHeader> templateHeader = new ArrayList<ComHeader>();
    //值集信息
    private List valueSet = new ArrayList();
    //是否可编辑
    private boolean isEditable = true;
    //当前组合是否可编辑
    private boolean curCombinationRecordEditable = true;
    //
    private String curCombiantionRecord;
    //日志
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(DcmDataDisplayBean.class);
    //页面绑定组件
    private RichInputFile fileInput;
    private Map headerComponents = new LinkedHashMap();
    private RichPanelCollection panelaCollection;
    private Person curUser;
    private RichPopup errorWindow;
    private RichPopup dataImportWnd;
    private RichPopup dataExportWnd;
    private RichPopup templateExportWnd;
    //排序
    private List<SortCriterion> sortCriterions;
    //搜索
    private FilterableQueryDescriptor queryDescriptor =
        new DcmQueryDescriptor();
    private Map filters;
    //计算程序窗口
    private RichPopup calcWnd;
    //计算程序的参数对应的值集
    List<CalcParameter> parameterList = new ArrayList<CalcParameter>();
    //计算程序参数选择控件
    private Map<String, RichSelectOneChoice> paraSocMap =
        new HashMap<String, RichSelectOneChoice>();
    //上一次操作的模式
    private String lastHandleModel = "default";
    // 工作流ID
    private String curWfId;
    //工作流运行ID
    private String curRunId;
    //步骤编码
    private int stepNo;
    //步骤任务
    private String curStepTask;
    //输入状态
    private String writeStatus;
    //审批状态
    private String approveStatus;
    //审批步骤编码
    private int approveStepNo = 0;
    //初始化
    private TabContext dmsTabContext;
    private RichPopup sortTipPopup;
    private RichTable displayTable;
    private RichPopup calcErrPop;
    private String calcErrMsg;
    private boolean isRolling = true;
    private Number rollingMonth;
    private boolean isEnd = true;
    private String reason;
    private boolean hasCalc = true;

    private RichSelectOneListbox richSoc;
    private RichPopup slcPop;

    private RichProgressIndicator exportProIndicator;
    private RichCommandButton exportButton;
    private RichPopup backStepPop;
    private String backReason;
    private RichSelectOneChoice backSoc;


    public DcmDataDisplayBean() {
        this.curUser =
                (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        this.dataModel = new DcmDataTableModel();
        this.initTemplate();
        this.initCombination();
        this.queryTemplateData();
        //判断模板是否在工作流中，是输入还是审批状态
        this.isWfTemplate();
        this.disableCalcBtn();
    }

    public CollectionModel getDataModel() {
        return this.dataModel;
    }

    public void disableCalcBtn() {
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String cSql =
            "SELECT 1 FROM DCM_TEMPLATE_CALC T WHERE T.TEMPLATE_ID = '" +
            this.curTempalte.getId() + "'";
        try {
            ResultSet rs = stat.executeQuery(cSql);
            if (rs.next()) {
                this.hasCalc = false;
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    //值发生改变时更改数据的状态为更新

    public void valueChangeListener(ValueChangeEvent valueChangeEvent) {
        Map rowData = (Map)this.dataModel.getRowData();
        //编辑时仅能选中一行
        if (displayTable.getSelectedRowKeys().size() > 1) {
            String msg =
                DmsUtils.getMsg("dcm.msg.can_not_select_multiple_row");
            JSFUtils.addFacesInformationMessage(msg);
            return;
        }
        if (null == rowData.get("OPERATION")) {
            rowData.put("OPERATION", DcmDataTableModel.OPERATE_UPDATE);
            this.makeDirty(true);
        }
    }

    //行选中时设置当前选中行
    private Integer oldRowKey;

    /*
     * 展示当前行为可下拉模式，也就是显示下拉框点击标志
     */

    private void showCurrentList() {

        if (displayTable == null) {
            Map nRowData = (Map)this.dataModel.getRowData(0);
            if (nRowData == null)
                return;
            nRowData.put("VISIBLE", "TRUE");
            oldRowKey = 0;
            return;
        }

        Integer nRowKey = (Integer)displayTable.getActiveRowKey();
        if (oldRowKey != null) {
            Map oRowData = (Map)displayTable.getRowData(oldRowKey);
            if (oRowData != null)
                oRowData.put("VISIBLE", "FALSE");
        }

        Map nRowData = (Map)displayTable.getRowData(nRowKey);
        if (nRowData != null)
            nRowData.put("VISIBLE", "TRUE");
        //System.out.println(oldRowKey + "   " + nRowKey);
        oldRowKey = nRowKey;
    }


    //新增数据行操作

    public void operation_new() {
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        Map newRow = new HashMap();
        for (ColumnDef col : this.colsdef) {
            newRow.put(col.getDbTableCol(), null);
        }

        newRow.put("OPERATION", DcmDataTableModel.OPERATE_CREATE);

        modelData.add(0, newRow);

        this.makeDirty(true); //调用这个方法会使得页面处于未保存状态，删除会提示
        if (oldRowKey != null)
            oldRowKey++;
        showCurrentList();
    }

    //删除数据行操作

    public void operation_delete() {
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        RowKeySet keySet = displayTable.getSelectedRowKeys();

        for (Object key : keySet) {
            Map rowData = (Map)this.dataModel.getRowData(key);
            //若为新增操作则直接从数据集删除数据
            if (DcmDataTableModel.OPERATE_CREATE.equals(rowData.get("OPERATION"))) {
                modelData.remove(rowData);
                this.makeDirty(true); //调用这个方法会使得页面处于未保存状态，删除会提示
            }
            //若为更新或数据未修改则直接将数据集数据标记为删除
            else if (DcmDataTableModel.OPERATE_UPDATE.equals(rowData.get("OPERATION")) ||
                     null == rowData.get("OPERATION")) {
                rowData.put("OPERATION", DcmDataTableModel.OPERATE_DELETE);
                this.makeDirty(true); //调用这个方法会使得页面处于未保存状态，删除会提示
            }
            //已经为删除状态的数据无需做任何处理
        }

    }
    //数据保存操作

    public void operation_save() {
        boolean flag = true;
        String curComRecordId = this.curCombiantionRecord;
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        StringBuffer sql_insert = new StringBuffer();
        StringBuffer sql_value = new StringBuffer();
        sql_insert.append("INSERT INTO \"").append(this.curTempalte.getTmpTable()).append("\"(");
        sql_insert.append("TEMPLATE_ID,COM_RECORD_ID,ORIGIN_ROWID");
        sql_insert.append(",CREATED_BY,UPDATED_BY,CREATED_AT,UPDATED_AT,SHEET_NAME,ROW_NO,EDIT_TYPE");
        sql_value.append(" VALUES('").append(this.curTempalte.getId()).append("'");
        sql_value.append(",'").append(ObjectUtils.toString(curComRecordId)).append("'");
        sql_value.append(",?,'").append(this.curUser.getId()).append("'");
        sql_value.append(",'").append(this.curUser.getId()).append("'");
        sql_value.append(",SYSDATE,SYSDATE,'NA',?,?");
        for (int i = 0; i < this.colsdef.size(); i++) {
            sql_insert.append(",COLUMN").append(i + 1);
            sql_value.append(",?");
        }
        sql_insert.append(")");
        sql_value.append(")");
        try {
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
            this.clearTmpTableAndErrTable(curComRecordId);
            //将数据插入临时表
            PreparedStatement stat =
                trans.createPreparedStatement(sql_insert.toString() +
                                              sql_value.toString(), 0);
            int rowNo = 1;
            for (Map rowData : modelData) {
                stat.setInt(2, rowNo);
                rowNo++;
                if (DcmDataTableModel.OPERATE_CREATE.equals(rowData.get("OPERATION"))) {
                    stat.setString(3, "NEW");
                } else if (DcmDataTableModel.OPERATE_DELETE.equals(rowData.get("OPERATION"))) {
                    stat.setString(3, "DELETE");
                } else if (DcmDataTableModel.OPERATE_UPDATE.equals(rowData.get("OPERATION"))) {
                    stat.setString(3, "UPDATE");
                }
                if (null != rowData.get("OPERATION")) {
                    for (int i = 0; i < this.colsdef.size(); i++) {
                        stat.setString(4 + i,
                                       (String)rowData.get(this.colsdef.get(i).getDbTableCol()));
                    }
                    stat.setString(1, (String)rowData.get("ROW_ID"));
                    stat.addBatch();
                }
            }
            stat.executeBatch();
            trans.commit();
            stat.close();
            flag = this.handleData("EDIT", curComRecordId);
            this.lastHandleModel = "EDIT";
            if (flag) {
                this.queryTemplateData();
            }


        } catch (Exception e) {
            flag = false;
            if (e.getMessage().length() > 2048) {
                this.writeErrorMsg(e.getMessage().substring(0, 2048),
                                   curComRecordId);
            } else {
                this.writeErrorMsg(e.getMessage(), curComRecordId);
            }
            this._logger.severe(e);
        }
        if (!flag) {
            this.showErrorPop();
        }
    }
    //数据重置则直接刷新数据

    public void operation_reset() {
        this.queryTemplateData();
        this.makeDirty(false); //调用这个方法会使得页面处于未保存状态，删除会提示
    }
    //数据导入操作

    public void operation_import(ActionEvent actionEvent) throws SQLException {
        this.dataImportWnd.cancel();
        String curComRecordId = this.curCombiantionRecord;
        //组合找不到
        if (this.curTempalte.getCombinationId() != null &&
            curComRecordId == null) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.inform.select_correct_combination"));
            return;
        }
        //上传文件为空
        if (null == this.fileInput.getValue()) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.plz_select_import_file"));
            return;
        }
        //获取文件上传路径
        String filePath = this.uploadFile();
        this.fileInput.resetValue();
        if (null == filePath) {
            return;
        }
        //读取excel数据到数据库临时表
        if (!this.handleExcel(filePath, curComRecordId)) {
            return;
        }
        this.lastHandleModel = this.isIncrement ? "INCREMENT" : "REPLACE";
        //进行数据处理（前置程序、校验和善后程序）
        if (this.handleData(this.isIncrement ? "INCREMENT" : "REPLACE",
                            curComRecordId)) {
            String msg = DmsUtils.getMsg("dcm.inform.data_import_success");
            JSFUtils.addFacesInformationMessage(msg);
        } else {
            //若出现错误则显示错误信息提示框
            this.showErrorPop();
        }
        //刷新数据
        this.queryTemplateData();
    }
    //执行数据校验和数据转移

    private boolean handleData(String mode, String curComRecordId) {
        boolean successFlag = true;
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        try {
            //执行前置程序
            if (this.curTempalte.getPreProgram() != null) {
                CallableStatement prcs =
                    trans.createCallableStatement("{CALl " +
                                                  this.curTempalte.getPreProgram() +
                                                  "(?,?,?,?,?)}", 0);
                prcs.setString(1, this.curTempalte.getId());
                prcs.setString(2, curComRecordId);
                prcs.setString(3, this.curUser.getId());
                prcs.setString(4, mode);
                prcs.setString(5, this.curUser.getLocale());
                prcs.execute();
                trans.commit();
                prcs.close();
            }
            //执行校验程序
            ViewObject vo =
                DmsUtils.getDcmApplicationModule().getDcmValidationQueryView();
            vo.setNamedWhereClauseParam("templateId",
                                        this.curTempalte.getId());
            vo.executeQuery();
            vo.reset();
            while (vo.hasNext()) {
                Row row = vo.next();
                CallableStatement cs =
                    trans.createCallableStatement("{CALL " + row.getAttribute("Program") +
                                                  "(?,?,?,?,?,?,?,?,?)}", 0);
                cs.setString(1, (String)row.getAttribute("ValidationId"));
                cs.setString(2, this.curTempalte.getId());
                cs.setString(3, curComRecordId);
                //获取校验对应的临时表列
                for (int i = 1; i <= this.colsdef.size(); i++) {
                    if (this.colsdef.get(i -
                                         1).getDbTableCol().equals((String)row.getAttribute("DbTableCol"))) {
                        cs.setString(4, "COLUMN" + i);
                        break;
                    }
                }
                cs.setString(5, (String)row.getAttribute("DbTableCol"));
                cs.setString(6, mode);
                cs.setString(7, this.curUser.getLocale());
                cs.setString(8, (String)row.getAttribute("Args"));
                cs.registerOutParameter(9, Types.VARCHAR);
                cs.execute();
                //若返回值为N则校验失败
                if ("N".equals(cs.getString(9))) {
                    successFlag = false;
                }
                trans.commit();
                cs.close();
            }
            if (successFlag) {
                //若校验通过则执行数据导入
                CallableStatement cs =
                    trans.createCallableStatement("{CALl " + this.curTempalte.getHandleProgram() +
                                                  "(?,?,?,?,?)}", 0);
                cs.setString(1, this.curTempalte.getId());
                cs.setString(2, curComRecordId);
                cs.setString(3, this.curUser.getId());
                cs.setString(4, mode);
                cs.setString(5, this.curUser.getLocale());
                cs.execute();
                trans.commit();
                cs.close();
                //执行善后程序
                if (this.curTempalte.getAfterProgram() != null) {
                    CallableStatement afcs =
                        trans.createCallableStatement("{CALl " +
                                                      this.curTempalte.getAfterProgram() +
                                                      "(?,?,?,?,?)}", 0);
                    afcs.setString(1, this.curTempalte.getId());
                    afcs.setString(2, curComRecordId);
                    afcs.setString(3, this.curUser.getId());
                    afcs.setString(4, mode);
                    afcs.setString(5, this.curUser.getLocale());
                    afcs.execute();
                    trans.commit();
                    afcs.close();
                }
            }
        } catch (Exception e) {
            successFlag = false;
            String msg = e.getMessage();
            if (msg.length() > 2048) {
                msg = msg.substring(0, 2048);
            }
            this.writeErrorMsg(msg, curComRecordId);
            this._logger.severe(e);
        }
        return successFlag;
    }
    //写入数据到错误表

    private void writeErrorMsg(String msg, String curComRecordId) {
        ViewObject vo =
            ADFUtils.findIterator("DcmErrorViewIterator").getViewObject();
        Row row = vo.createRow();
        row.setAttribute("TemplateId", this.curTempalte.getId());
        row.setAttribute("ComRecordId", curComRecordId);
        row.setAttribute("Msg", msg);
        row.setAttribute("SheetName", "NA");
        row.setAttribute("ValidationId",
                         UUID.randomUUID().toString().replace("-", ""));
        row.setAttribute("Level", "Error");
        vo.insertRow(row);
        vo.getApplicationModule().getTransaction().commit();
    }
    //获取当前的组合

    private String getCurCombinationRecord() {
        String comRecordId = null;
        if (this.curCombiantion != null) {
            String combinationCode = this.curCombiantion.getCode();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ID FROM ").append("\"").append(combinationCode.toUpperCase()).append("\"");
            sql.append(" WHERE 1=1");
            for (ComHeader h : this.templateHeader) {
                sql.append(" AND ");
                sql.append("\"").append(h.getCode()).append("\"='").append(h.getValue()).append("'");
            }
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
            Statement stat = trans.createStatement(1);
            try {
                //time
                ResultSet rs = stat.executeQuery(sql.toString());
                if (rs.next()) {
                    comRecordId = rs.getString("ID");
                }
                stat.close();
            } catch (SQLException e) {
                this._logger.severe(e);
            }
        }
        return comRecordId;
    }


    //获取当前组合的文本信息用于拼接文件名

    private String getCurComRecordText() {
        String text = "";

        if (this.curCombiantion != null) {
            for (ComHeader header : this.templateHeader) {
                for (SelectItem item : header.getValues()) {
                    if (header.getValue().equals(item.getValue())) {
                        text += "_" + item.getLabel();
                    }
                }
            }
        }
        return text;
    }
    //读取excel数据到临时表

    private boolean handleExcel(String fileName,
                                String curComRecordId) throws SQLException {
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        String combinationRecord = ObjectUtils.toString(curComRecordId);
        //清空已有零时表数据
        this.clearTmpTableAndErrTable(curComRecordId);
        RowReader reader =
            new RowReader(trans, (int)this.curTempalte.getDataStartLine().getValue(),
                          this.curTempalte.getId(), combinationRecord,
                          this.curTempalte.getTmpTable(), this.colsdef.size(),
                          this.curUser.getId(), this.curTempalte.getName(),
                          this.colsdef);
        try {
            ExcelReaderUtil.readExcel(reader, fileName, true);
            reader.close();
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
        String fileExtension =
            file.getFilename().substring(file.getFilename().lastIndexOf("."));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        File dmsBaseDir = new File("DMS/UPLOAD/" + this.curTempalte.getName());
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        String fileName = "DMS/UPLOAD/" + this.curTempalte.getName() + "/";
        if (this.curCombiantion == null) {
            fileName +=
                    this.curTempalte.getName() + "_" + this.curUser.getName() +
                    "_" + date + fileExtension;
        } else {
            fileName +=
                    this.getCurComRecordText() + "_" + this.curUser.getName() +
                    "_" + date + fileExtension;
        }
        try {
            InputStream inputStream = file.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);
            BufferedInputStream bufferedInputStream =
                new BufferedInputStream(inputStream);
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(outputStream);
            byte[] buffer = new byte[10240];
            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(buffer, 0, 10240)) !=
                   -1) {
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
    //数据导出

    public void operation_export(FacesContext facesContext,
                                 java.io.OutputStream outputStream) {

        isProcess = 1;

        String type = this.isXlsx ? "xlsx" : "xls";
        try {
            if ("xls".equals(type)) {
                Excel2003WriterImpl writer =
                    new Excel2003WriterImpl(this.getQuerySql(),
                                            this.curTempalte, this.colsdef,
                                            outputStream);
                writer.writoToFile();
            } else {
                Excel2007WriterImpl writer=new Excel2007WriterImpl(this.getQuerySql(),
                                                                   (int)this.curTempalte.getDataStartLine().getValue(),
                                                                   this.colsdef);
                writer.process(outputStream, this.curTempalte.getName());
            }
            outputStream.flush();
        } catch (Exception e) {
            this._logger.severe(e);
        } finally {
            isProcess = 0;
        }

    }
    //下载模板

    public void operation_download(FacesContext facesContext,
                                   java.io.OutputStream outputStream) {
        this.templateExportWnd.cancel();
        try {
            if (this.isXlsx) {
                if (this.curTempalte.getTemplateFile() == null) {
                    XSSFWorkbook wb = new XSSFWorkbook();
                    XSSFSheet sheet =
                        wb.createSheet(this.curTempalte.getName());
                    XSSFRow row =
                        sheet.createRow((int)this.curTempalte.getDataStartLine().getValue() -
                                        2);
                    for (int i = 0; i < this.colsdef.size(); i++) {
                        row.createCell(i).setCellValue(this.colsdef.get(i).getColumnLabel());
                    }
                    wb.write(outputStream);
                    outputStream.flush();
                } else {
                    FileInputStream inputStream =
                        new FileInputStream(this.curTempalte.getTemplateFile() +
                                            ".xlsx");
                    byte[] buffer = new byte[10240];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer, 0, 10240)) !=
                           -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            } else {
                if (this.curTempalte.getTemplateFile() == null) {
                    // 创建excel2003对象
                    Workbook wb = new HSSFWorkbook();
                    // 创建新的表单
                    Sheet sheet = wb.createSheet(this.curTempalte.getName());
                    // 创建新行
                    org.apache.poi.ss.usermodel.Row headerRow =
                        sheet.createRow((int)this.curTempalte.getDataStartLine().getValue() -
                                        2);
                    for (int i = 0; i < this.colsdef.size(); i++) {
                        headerRow.createCell(i).setCellValue(this.colsdef.get(i).getColumnLabel());
                    }
                    wb.write(outputStream);
                    outputStream.flush();
                } else {
                    FileInputStream inputStream =
                        new FileInputStream(this.curTempalte.getTemplateFile() +
                                            ".xls");
                    byte[] buffer = new byte[10240];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer, 0, 10240)) !=
                           -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            }
        } catch (Exception e) {
            this._logger.severe(e);
            String msg = DmsUtils.getMsg("dcm.template_download_error");
            JSFUtils.addFacesErrorMessage(msg);
            return;
        }
    }
    //初始化模板和模板列信息
    private List<ComboboxLOVBean> _comboboxLOVBeanList;
    //
    private List<List<SelectItem>> socValue =
        new ArrayList<List<SelectItem>>();
    private List<SelectItem> socValueList = new ArrayList<SelectItem>();

    private void initTemplate() {

        setComboboxLOVBeanList(new ArrayList());

        String curTemplateId =
            (String)ADFContext.getCurrent().getPageFlowScope().get("curTemplateId");
        dmsTabContext =
                (TabContext)ADFContext.getCurrent().getPageFlowScope().get("dmsTabContext");

        ViewObject templateView =
            DmsUtils.getDcmApplicationModule().getDcmTemplateView();
        templateView.executeQuery();
        Row[] rows =
            templateView.findByKey(new Key(new Object[] { curTemplateId,
                                                          ADFContext.getCurrent().getLocale().toString() }),
                                   1);

        if (rows.length > 0) {
            this.curTempalte = new TemplateEO((DcmTemplateViewRowImpl)rows[0]);
            //只读模板
            if ("Y".equals(this.curTempalte.getReadonly())) {
                this.isEditable = false;
            }
            //对该模板仅有只读权限
            if (this.isEditable) {
                this.isEditable = !this.isReadOnly();
            }
            //不支持增量导入则设置默认覆盖导入
            if (!this.curTempalte.getHandleMode().contains("I")) {
                this.isIncrement = false;
            }
            templateView.setCurrentRow(rows[0]);
            DcmTemplateViewRowImpl row = (DcmTemplateViewRowImpl)rows[0];
            RowIterator itr = row.getDcmTemplateColumnView();
            List<String> listData = new ArrayList<String>(); //把list放在下拉框模板中

            while (itr.hasNext()) {
                Row colRow = itr.next();
                ColumnDef colDef =
                    new ColumnDef((DcmTemplateColumnViewRowImpl)colRow);
                this.colsdef.add(colDef);
                //获取值列表

                if (colDef.getValueSetId() != null) {

                    List<SelectItem> tem =
                        this.fetchValueList(colDef.getValueSetId());
                    socValue.add(tem);
                    valueSet.add(tem);
                    List<ComboboxLOVBean.Attribute> list =
                        new ArrayList<ComboboxLOVBean.Attribute>();
                    list.add(new ComboboxLOVBean.Attribute(colDef.getColumnLabel(),
                                                           "name"));

                    ComboboxLOVBean bean = new ComboboxLOVBean(tem, list);
                    _comboboxLOVBeanList.add(bean);
                } else {
                    this.valueSet.add(null);
                    ComboboxLOVBean bean = new ComboboxLOVBean(null, null);
                    _comboboxLOVBeanList.add(bean);
                    socValue.add(new ArrayList<SelectItem>());
                }
            }
            ((DcmDataTableModel)this.dataModel).setColsdef(this.colsdef);

        } else {
            this._logger.severe(DmsUtils.getMsg("dcm.template_not_found"));
            throw new RuntimeException(DmsUtils.getMsg("dcm.template_not_found") +
                                       ":tempateId:" + curTemplateId);
        }
    }
    //获取值列表

    private List<SelectItem> fetchValueList(String vsId) {

        List<SelectItem> list = new ArrayList<SelectItem>();
        list.add(new SelectItem("", ""));
        Row[] vsRows =
            DmsUtils.getDmsApplicationModule().getDmsValueSetView().findByKey(new Key(new Object[] { vsId,
                                                                                                     ADFContext.getCurrent().getLocale().toString() }),
                                                                              1);
        if (vsRows.length > 0) {
            String vsCode = (String)vsRows[0].getAttribute("Source");
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT V.CODE, V.MEANING FROM \"").append(vsCode).append("\" V WHERE V.LOCALE = '").append(ADFContext.getCurrent().getLocale()).append("'").
                //            .append(" AND EXISTS(SELECT 1 FROM ")
                //            .append(" DMS_USER_VALUE_V T")
                //            .append(" WHERE T.USER_ID = '").append(this.curUser.getId()).append("'")
                //            .append(" AND T.VALUE_SET_ID = '").append(vsId).append("'")
                //            .append(" AND T.VALUE_ID=V.CODE)")
                append("ORDER BY V.IDX ");

            Statement stmt =
                DmsUtils.getDmsApplicationModule().getDBTransaction().createStatement(DBTransaction.DEFAULT);
            try {
                ResultSet rs = stmt.executeQuery(sql.toString());
                while (rs.next()) {
                    SelectItem itm = new SelectItem();
                    itm.setLabel(rs.getString("MEANING"));
                    itm.setValue(rs.getString("MEANING"));
                    list.add(itm);
                }
            } catch (SQLException e) {
                this._logger.severe(e);
            }
        }
        return list;
    }

    //查询数据

    private void queryTemplateData() {
        List<Map> data = new ArrayList<Map>();
        DBTransaction dbTransaction =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        String countSql = this.getCountSql(this.getQuerySql());
        PreparedStatement cstat =
            dbTransaction.createPreparedStatement(countSql, -1);
        try {
            ResultSet crs = cstat.executeQuery();
            if (crs.next()) {
                this.setTotalCount(crs.getInt(1));
            } else {
                this.setTotalCount(0);
            }
            crs.close();
            cstat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        String sql = this.getPaginationSql(this.getQuerySql());
        PreparedStatement stat =
            dbTransaction.createPreparedStatement(sql, -1);
        DecimalFormat dfm = new DecimalFormat();
        dfm.setMaximumFractionDigits(4);
        dfm.setGroupingUsed(false);
        ResultSet rs = null;
        try {
            rs = stat.executeQuery();
            while (rs.next()) {
                Map row = new HashMap();
                for (ColumnDef col : this.colsdef) {
                    Object obj =
                        rs.getObject(col.getDbTableCol().toUpperCase());
                    if (obj instanceof java.sql.Date) {
                        SimpleDateFormatter format =
                            new SimpleDateFormatter("yyyy-MM-dd hh:mm:ss");
                        obj = format.format((java.sql.Date)obj);
                    } else if (col.getDataType().equals("NUMBER")) {
                        try {
                            if (obj != null)
                                obj = dfm.format(Double.valueOf(obj.toString()));
                            obj = ObjectUtils.toString(obj);
                        } catch (Exception e) {
                            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.format.error"));
                            this._logger.severe(e);
                        }
                    } else {
                        obj = ObjectUtils.toString(obj);
                    }
                    row.put(col.getDbTableCol(), obj);
                }
                row.put("ROW_ID", rs.getString("ROW_ID"));
                data.add(row);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.query_data_error"));
            this._logger.severe(e);
        }
        this.dataModel.setWrappedData(data);

        //RowKeySet keySet = ((DcmDataTableModel)this.dataModel).getSelectedRows();
        //        //初始化数据默认显示第一条
        //        data.get(0).put("VISIBLE", "TRUE");
        //        oldRowKey = new Integer(0);
        showCurrentList();

        makeDirty(false); //调用这个方法会使得页面处于未保存状态，删除会提示
    }
    //获取数据查询语句

    private String getQuerySql() {
        StringBuffer sql_select = new StringBuffer();
        StringBuffer sql_from = new StringBuffer();
        StringBuffer sql_where = new StringBuffer();
        if (null != this.curTempalte.getDbView()) {
            sql_select.append("SELECT ROW_ID");
            sql_from.append(" ").append("FROM ").append(this.curTempalte.getDbView());
        } else {
            sql_select.append("SELECT ROWID ROW_ID");
            sql_from.append(" ").append("FROM ").append(this.curTempalte.getDbTable());
        }
        for (ColumnDef col : this.colsdef) {
            sql_select.append(",\"").append(col.getDbTableCol()).append("\"");
        }
        if (this.curCombiantion != null) {
            sql_where.append(" WHERE COM_RECORD_ID='").append(this.curCombiantionRecord).append("'");
        } else {
            sql_where.append(" WHERE COM_RECORD_ID IS NULL");
        }
        if (this.filters != null) {
            for (Object key : this.filters.keySet()) {
                if (!ObjectUtils.toString(this.filters.get(key)).trim().equals("")) {
                    String fv = ObjectUtils.toString(this.filters.get(key));
                    if ("NULL".equals(fv.toUpperCase())) {
                        sql_where.append(" AND \"").append(key).append("\" IS NULL");
                    } else {
                        sql_where.append(" AND UPPER(\"").append(key).append("\") LIKE UPPER('");
                        fv = fv.replace(' ', '%');
                        sql_where.append("%" + fv + "%");

                        sql_where.append("')");
                    }
                }
            }
        }
        if (this.sortCriterions == null) {
            sql_where.append(" ORDER BY IDX");
        } else {
            sql_where.append(" ORDER BY ");
            for (int i = 0; i < this.sortCriterions.size(); i++) {
                SortCriterion s = this.sortCriterions.get(i);
                if (i > 0) {
                    sql_where.append(",");
                }
                sql_where.append("\"").append(s.getProperty()).append("\"").append(s.isAscending() ?
                                                                                   " ASC" :
                                                                                   " DESC");
            }
        }
        return sql_select.toString() + sql_from.toString() +
            sql_where.toString();
    }

    //初始化组合信息

    private void initCombination() {
        if (null != this.curTempalte.getCombinationId()) {
            //初始化组合基本信息
            ViewObject cVo =
                DmsUtils.getDcmApplicationModule().getDcmCombinationView();
            Row rows[] =
                cVo.findByKey(new Key(new Object[] { this.curTempalte.getCombinationId(),
                                                     ADFContext.getCurrent().getLocale().toString() }),
                              1);
            if (rows.length > 0) {
                this.curCombiantion =
                        new CombinationEO((DcmCombinationViewRowImpl)rows[0]);
                ;
            }
            //初始化组合头信息
            ViewObject vo =
                DmsUtils.getDcmApplicationModule().getDcmComVsQueryView();
            vo.setNamedWhereClauseParam("combinationId",
                                        this.curTempalte.getCombinationId());
            vo.executeQuery();
            vo.reset();
            while (vo.hasNext()) {
                Row row = vo.next();
                ComHeader header = new ComHeader();
                header.setName((String)row.getAttribute("Name"));
                header.setIsAuthority((String)row.getAttribute("IsAuthority"));
                header.setSrcTable((String)row.getAttribute("Source"));
                header.setValueSetId((String)row.getAttribute("ValueSetId"));
                header.setCode((String)row.getAttribute("Code"));
                header.setDefaultCode((String)row.getAttribute("DefaultCode"));

                //下面的初始化已经将defaultCode的meaning变成了code
                this.initHeaderValueList(header);

                //设置默认值
                this.setDefaultHeaderValue(header);

                //如果某个值集存在默认值，那么就设置默认值
                if (row.getAttribute("DefaultCode") != null) {
                    //defaultCode已经不是meaning而是code了(this.initHeaderValueList(header);)
                    header.setValue(header.getDefaultCode());

                }
                this.templateHeader.add(header);
            }
            this.curCombiantionRecord = this.getCurCombinationRecord();
            this.setCurCombinationRecordEditable();
            //初始化滚动预算
            this.setReadonlyByRolling();
        }
    }

    private void setCurCombinationRecordEditable() {
        if (this.curCombiantionRecord == null) {
            this.curCombinationRecordEditable = false;
        } else {
            boolean flag = true;
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT T.STATUS FROM DCM_TEMPLATE_COMBINATION T WHERE T.TEMPLATE_ID='").append(this.curTempalte.getId()).append("' AND T.COM_RECORD_ID='").append(this.curCombiantionRecord).append("'");
            DBTransaction dbTransaction =
                (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
            Statement stat =
                dbTransaction.createStatement(DBTransaction.DEFAULT);
            try {
                //time
                ResultSet rs = stat.executeQuery(sql.toString());
                if (rs.next()) {
                    String status = rs.getString("STATUS");
                    if ("OPEN".equals(status)) {
                        for (ComHeader h : this.templateHeader) {
                            StringBuffer sql0 = new StringBuffer();
                            sql0.append("SELECT T.ENABLED FROM \"").append(h.getSrcTable()).append("\" T WHERE T.CODE='").append(h.getValue()).append("' AND T.LOCALE='").append(ADFContext.getCurrent().getLocale()).append("'");
                            //time
                            ResultSet rst = stat.executeQuery(sql0.toString());
                            if (rst.next()) {
                                String enabled = rst.getString("ENABLED");
                                if ("N".equals(enabled)) {
                                    flag = false;
                                    break;
                                }
                            } else {
                                flag = false;
                                break;
                            }
                            rst.close();
                        }
                    } else {
                        flag = false;
                    }
                } else {
                    flag = false;
                }
                rs.close();
                stat.close();
            } catch (SQLException e) {
                this._logger.severe(e);
            }
            this.curCombinationRecordEditable = flag;
        }
    }

    private void initHeaderValueList(ComHeader header) {
        List<SelectItem> values = new ArrayList<SelectItem>();
        DBTransaction dbTransaction =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT V.CODE,V.MEANING FROM \"");
        sql.append(header.getSrcTable()).append("\" V");
        sql.append(" WHERE V.LOCALE='").append(this.curUser.getLocale()).append("'");
        if ("Y".equals(header.getIsAuthority())) {
            sql.append(" AND EXISTS(SELECT 1 FROM ");
            sql.append(" DMS_USER_VALUE_V T");
            sql.append(" WHERE T.USER_ID = '").append(this.curUser.getId()).append("'");
            sql.append(" AND T.VALUE_SET_ID = '").append(header.getValueSetId()).append("'");
            sql.append(" AND T.VALUE_ID=V.CODE)");
        }
        sql.append(" ORDER BY V.IDX");
        PreparedStatement stat =
            dbTransaction.createPreparedStatement(sql.toString(), -1);
        try {
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                SelectItem item = new SelectItem();
                String meaning = rs.getString("MEANING");

                item.setLabel(rs.getString("MEANING"));
                item.setValue(rs.getString("CODE"));
                values.add(item);
                //在这个位置查找默认值的code
                if (meaning.equals(header.getDefaultCode()))
                    header.setDefaultCode(rs.getString("CODE"));

            }
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        header.setValues(values);
    }

    private void setDefaultHeaderValue(ComHeader header) {
        DBTransaction dbTransaction =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT V.CODE,V.MEANING FROM \"");
        sql.append(header.getSrcTable()).append("\" V");
        sql.append(" WHERE V.LOCALE='").append(this.curUser.getLocale()).append("'");
        sql.append(" AND EXISTS(SELECT 1 FROM \"").append(this.curCombiantion.getCode()).append("\" T,DCM_TEMPLATE_COMBINATION A ");
        sql.append(" WHERE T.\"").append(header.getCode()).append("\"=V.CODE");
        sql.append(" AND T.ID=A.COM_RECORD_ID");
        sql.append(" AND A.TEMPLATE_ID='").append(this.curTempalte.getId()).append("'");
        sql.append(" AND A.STATUS='OPEN'");
        for (ComHeader h : this.templateHeader) {
            if (!h.equals(header)) {
                sql.append("AND T.\"").append(h.getCode()).append("\"='");
                sql.append(h.getValue()).append("'");
            } else {
                break;
            }
        }
        sql.append(")");
        if ("Y".equals(header.getIsAuthority())) {
            sql.append(" AND EXISTS(SELECT 1 FROM ");
            sql.append(" DMS_USER_VALUE_V T");
            sql.append(" WHERE T.USER_ID = '").append(this.curUser.getId()).append("'");
            sql.append(" AND T.VALUE_SET_ID = '").append(header.getValueSetId()).append("'");
            sql.append(" AND T.VALUE_ID=V.CODE)");
        }
        sql.append(" ORDER BY V.IDX");
        PreparedStatement stat =
            dbTransaction.createPreparedStatement(sql.toString(), -1);
        try {
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                header.setValue(rs.getString("CODE"));
            }
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    public String getCombinationId() {
        return curTempalte.getCombinationId();
    }

    public void setTemplateHeader(List<ComHeader> templateHeader) {
        this.templateHeader = templateHeader;
    }

    public List<ComHeader> getTemplateHeader() {
        return templateHeader;
    }

    public void setFileInput(RichInputFile fileInput) {
        this.fileInput = fileInput;
    }

    public RichInputFile getFileInput() {
        return fileInput;
    }
    //选择不同的组合时的处理逻辑

    public void headerSelectChangeListener(ValueChangeEvent valueChangeEvent) {
        RichSelectOneChoice header =
            (RichSelectOneChoice)valueChangeEvent.getSource();
        int i = 0;
        for (Object key : this.headerComponents.keySet()) {
            //找到当前表头
            if (header.equals(this.headerComponents.get(key))) {
                this.templateHeader.get(i).setValue((String)valueChangeEvent.getNewValue());
            }
            i++;
        }
        this.curCombiantionRecord = this.getCurCombinationRecord();
        this.setCurCombinationRecordEditable();
        this.setCurPage(1);
        this.queryTemplateData();
        //查询组合是否需要提交审批
        //更改组合后，先默认赋值不能提交
        this.writeStatus = "Y";
        this.approveStatus = "Y";
        this.isWfTemplate();
//        if (this.writeStatus != "Y") {
//            this.isEditable = true;
//        }
        //设置滚动预算
        this.setReadonlyByRolling();
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();
        adfFacesContext.addPartialTarget(this.panelaCollection);
    }

    public Map getHeaderComponents() {
        return headerComponents;
    }

    public void setPanelaCollection(RichPanelCollection panelaCollection) {
        this.panelaCollection = panelaCollection;
    }

    public RichPanelCollection getPanelaCollection() {
        return panelaCollection;
    }

    public String getTemplateName() {
        return this.curTempalte.getName();
    }

    public void setIsIncrement(boolean isIncrement) {
        this.isIncrement = isIncrement;
    }

    public boolean isIsIncrement() {
        return isIncrement;
    }

    public void setIsXlsx(boolean isXlsx) {
        this.isXlsx = isXlsx;
    }

    public boolean isIsXlsx() {
        return isXlsx;
    }


    /**
     * 获取导出错误信息的文件名
     * @return
     */
    public String getExportErrExcelName() {

        return this.curTempalte.getName() + this.getCurComRecordText() +
            "_错误信息" + ".xls";

    }

    //获取导出数据时的文件名

    public String getExportDataExcelName() {

        if (this.isXlsx) {
            return this.curTempalte.getName() + this.getCurComRecordText() +
                ".xlsx";
        } else {
            return this.curTempalte.getName() + this.getCurComRecordText() +
                ".xls";
        }

    }
    //获取模板文件名

    public String getExportTemplateExcelName() {
        if (this.isXlsx) {
            return this.curTempalte.getName() + ".xlsx";
        } else {
            return this.curTempalte.getName() + ".xls";
        }
    }

    public void showErrors(ActionEvent actionEvent) {
        this.showErrorPop();
    }

    //显示错误信息窗口

    private void showErrorPop() {
        ViewObject vo =
            ADFUtils.findIterator("DcmErrorViewIterator").getViewObject();
        ViewCriteria viewCriteria = vo.createViewCriteria();
        ViewCriteriaRow vr = viewCriteria.createViewCriteriaRow();
        if (this.curCombiantion == null) {
            vr.setAttribute("ComRecordId", " is null");
        } else {
            vr.setAttribute("ComRecordId",
                            "='" + this.curCombiantionRecord + "'");
        }
        vr.setAttribute("TemplateId", "='" + this.curTempalte.getId() + "'");
        viewCriteria.addRow(vr);
        vo.applyViewCriteria(viewCriteria);
        vo.executeQuery();
        vo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.errorWindow.show(ph);
    }

    public void setErrorWindow(RichPopup errorWindow) {
        this.errorWindow = errorWindow;
    }

    public RichPopup getErrorWindow() {
        return errorWindow;
    }

    private void clearTmpTableAndErrTable(String comRecordId) throws SQLException {
        String clearTmpTableSql =
            "DELETE FROM \"" + this.curTempalte.getTmpTable() +
            "\" WHERE TEMPLATE_ID='" + this.curTempalte.getId() +
            "' AND COM_RECORD_ID " +
            (comRecordId == null ? "IS NULL" : ("='" + comRecordId + "'"));
        String clearErrTableSql =
            "DELETE FROM DCM_ERROR WHERE TEMPLATE_ID='" + this.curTempalte.getId() +
            "' AND COM_RECORD_ID " +
            (comRecordId == null ? "IS NULL" : ("='" + comRecordId + "'"));
        ApplicationModule dcmApplicationModule =
            DmsUtils.getDcmApplicationModule();
        //清空临时表相应数据
        dcmApplicationModule.getTransaction().executeCommand(clearTmpTableSql);
        //清空错误表相应数据
        dcmApplicationModule.getTransaction().executeCommand(clearErrTableSql);
        dcmApplicationModule.getTransaction().commit();
    }
    //calcName
    private List<SelectItem> calcNameList = new ArrayList<SelectItem>();
    //calcId对应的procedure
    Map<String, String> calcProMap = new HashMap<String, String>();
    //初始化计算窗口，查询模板计算程序

    public void showCalcWnd(ActionEvent actionEvent) {
        //每次打开窗口，Clear上次计算程序
        this.calcNameList.clear();
        this.calcProMap.clear();
        DCIteratorBinding calcIter =
            ADFUtils.findIterator("DcmTemplateCalcQueryVOIterator");
        ViewObject calcVo = calcIter.getViewObject();
        String whereClause = "TEMPLATE_ID ='" + this.curTempalte.getId() + "'";
        calcVo.setWhereClause(whereClause);
        calcVo.executeQuery();
        Row[] rows = calcIter.getAllRowsInRange();
        for (Row row : rows) {
            String calcName = row.getAttribute("CalcName").toString();
            String calcId = row.getAttribute("CalcId").toString();
            String calcProcedure =
                row.getAttribute("CalcProcedure").toString();
            this.calcProMap.put(calcId, calcProcedure);
            SelectItem item = new SelectItem();
            item.setLabel(calcName);
            item.setValue(calcId);
            calcNameList.add(item);
        }
        //设置默认值
        if (calcNameList.size() > 0) {
            RichSelectOneChoice soc3 =
                (RichSelectOneChoice)JSFUtils.findComponentInRoot("soc3");
            soc3.setValue(calcNameList.get(0).getValue());
            //调用valueChange方法触发参数改变
            ValueChangeEvent vce =
                new ValueChangeEvent(soc3, null, calcNameList.get(0).getValue());
            this.calcChange(vce);
        }
        RichPopup.PopupHints hint = new RichPopup.PopupHints();
        this.calcWnd.show(hint);
    }

    private List<SortCriterion> saveSortCriterions;

    public void sortListener(SortEvent sortEvent) {

        this.saveSortCriterions = sortEvent.getSortCriteria();
        TabContext tabContext = TabContext.getCurrentInstance();

        //如果当前页面不是dirty的话就直接刷新页面
        if (!tabContext.getTabs().get(tabContext.getSelectedTabIndex()).isDirty()) {
            sortTipDialogListener(null);
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();

        StringBuffer toSend = new StringBuffer();
        toSend.append("var popup = AdfPage.PAGE.findComponent('").append(sortTipPopup.getClientId(context)).append("'); ").append("if (!popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.show(hints);}");

        ExtendedRenderKitService service =
            Service.getRenderKitService(context, ExtendedRenderKitService.class);
        service.addScript(context, toSend.toString());
    }

    public FilterableQueryDescriptor getQueryDescriptor() {

        return this.queryDescriptor;
    }

    public void queryListener(QueryEvent queryEvent) {
        DcmQueryDescriptor descriptor =
            (DcmQueryDescriptor)queryEvent.getDescriptor();
        this.filters = descriptor.getFilterCriteria();
        this.setCurPage(1);
        this.queryTemplateData();
    }

    public List getValueSet() {
        return valueSet;
    }

    public void nextPage(ActionEvent event) {
        this.setCurPage(this.getCurPage() + 1);
        this.queryTemplateData();
    }

    public void prePage(ActionEvent event) {
        this.setCurPage(this.getCurPage() - 1);
        this.queryTemplateData();
    }

    public void refreshPage(ActionEvent event) {
        if (queryDescriptor.getFilterCriteria() != null) {
            queryDescriptor.getFilterCriteria().clear();
        }
        this.queryTemplateData();
    }

    public void gotoPage(ValueChangeEvent event) {
        this.setCurPage((Integer)event.getNewValue());
        this.queryTemplateData();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.panelaCollection);
    }

    public void firstPage(ActionEvent event) {
        this.setCurPage(1);
        this.queryTemplateData();
    }

    public void lastPage(ActionEvent event) {
        this.setCurPage(this.getTotalPage());
        this.queryTemplateData();
    }

    public void setDataImportWnd(RichPopup dataImportWnd) {
        this.dataImportWnd = dataImportWnd;
    }

    public RichPopup getDataImportWnd() {
        return dataImportWnd;
    }

    public void setDataExportWnd(RichPopup dataExportWnd) {
        this.dataExportWnd = dataExportWnd;
    }

    public RichPopup getDataExportWnd() {
        return dataExportWnd;
    }

    public void setTemplateExportWnd(RichPopup templateExportWnd) {
        this.templateExportWnd = templateExportWnd;
    }

    public RichPopup getTemplateExportWnd() {
        return templateExportWnd;
    }

    public boolean getIsEditable() {
        if (this.curCombiantion == null) {
            return this.isEditable;
        } else {
            return this.isEditable && this.curCombiantionRecord != null &&
                this.curCombinationRecordEditable;
        }
    }

    private boolean isReadOnly() {
        boolean readonly = false;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 1 ").append("  FROM DCM_USER_TEMPLATE_V T ").append(" WHERE T.READ_ONLY='Y' AND T.USER_ID = '").append(this.curUser.getId()).append("' ").append("   AND T.TEMPLATE_ID = '").append(this.curTempalte.getId()).append("'");
        Statement stmt =
            DmsUtils.getDcmApplicationModule().getDBTransaction().createStatement(DBTransaction.DEFAULT);
        try {
            ResultSet rs = stmt.executeQuery(sql.toString());
            if (rs.next()) {
                readonly = true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return readonly;
    }

    public boolean getCanIncrementImport() {
        return this.curTempalte.getHandleMode().contains("I") ? true : false;
    }

    public boolean getCanReplaceImport() {
        return this.curTempalte.getHandleMode().contains("R") ? true : false;
    }

    public boolean getIsReplaceDefault() {
        return !this.getCanIncrementImport();
    }

    public void setIsReplaceDefault(boolean flag) {

    }

    public void setCalcWnd(RichPopup calcWnd) {
        this.calcWnd = calcWnd;
    }

    public RichPopup getCalcWnd() {
        return calcWnd;
    }
    //当前计算程序的id
    private String curCalcId = "";
    //将计算程序的参数名按顺序存入key，将页面选择的参数值存到对应的value
    Map<String, String> parametersValueMap =
        new LinkedHashMap<String, String>();
    //计算程序改变，查询对应参数值集源表，对应值集

    public void calcChange(ValueChangeEvent valueChangeEvent) {
        //清除计算程序弹出款里的遗留参数
        if (this.parameterList != null) {
            for (Map.Entry<String, RichSelectOneChoice> entry :
                 paraSocMap.entrySet()) {
                entry.getValue().setValue(null);
            }
        }
        paraSocMap.clear();
        //每次改变程序，清除参数集合
        this.parameterList.clear();
        this.parametersValueMap.clear();
        //key：参数名 ， value：值集源表
        Map<String, String> vsMap = new LinkedHashMap<String, String>();
        //RichSelectOneChoice calcSoc = (RichSelectOneChoice)valueChangeEvent.getSource();
        //get calcId
        String calcId = valueChangeEvent.getNewValue().toString();
        this.curCalcId = calcId;
        //query calcParameter
        DCIteratorBinding paraIter =
            ADFUtils.findIterator("DcmCalcParameterQueryVOIterator");
        ViewObject paraVo = paraIter.getViewObject();
        String whereClause = "CALC_ID = '" + calcId + "'";
        paraVo.setWhereClause(whereClause);
        paraVo.executeQuery();
        paraVo.setWhereClause(null);
        Row[] rows = paraIter.getAllRowsInRange();
        //获得事务
        DBTransaction dbTransaction =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = dbTransaction.createStatement(DBTransaction.DEFAULT);
        try {
            for (Row row : rows) {
                String pName = row.getAttribute("PName").toString();
                String valueSetId = row.getAttribute("ValueSetId").toString();
                this.parametersValueMap.put(pName, "");
                StringBuffer sqlStr = new StringBuffer();
                sqlStr.append("select distinct t.source from dms_value_set t where t.code = '");
                sqlStr.append(valueSetId).append("'");
                ResultSet rs;
                //值集对应的表源
                String source = "";
                rs = stat.executeQuery(sqlStr.toString());
                if (rs.next()) {
                    source = rs.getString("SOURCE");
                }
                rs.close();
                vsMap.put(pName, source);
            }
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        //查询参数对应的值集
        queryVS(vsMap);
    }

    public void setCalcNameList(List<SelectItem> calcNameList) {
        this.calcNameList = calcNameList;
    }

    public List<SelectItem> getCalcNameList() {
        return calcNameList;
    }

    private void queryVS(Map<String, String> vsMap) {
        //获得事务
        DBTransaction dbTransaction =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = dbTransaction.createStatement(DBTransaction.DEFAULT);
        try {
            //查询出所有值并生成SelectItem
            for (Map.Entry<String, String> entry : vsMap.entrySet()) {
                String sqlStr =
                    "select code,meaning from " + entry.getValue() +
                    " where locale ='" + this.curUser.getLocale() +
                    "' order by idx";
                ResultSet rs;
                List<SelectItem> itemList = new ArrayList<SelectItem>();
                rs = stat.executeQuery(sqlStr);
                while (rs.next()) {
                    String code = rs.getString("CODE");
                    String meaning = rs.getString("MEANING");
                    SelectItem item = new SelectItem();
                    item.setLabel(meaning);
                    item.setValue(code);
                    itemList.add(item);
                }
                rs.close();
                //每个参数实例化一个CalcParameter类
                CalcParameter calcpara =
                    new CalcParameter(entry.getKey(), "", "", itemList);
                parameterList.add(calcpara);
            }
            stat.close();
        } catch (Exception e) {
            this._logger.severe(e);
        }
    }

    public void setParameterList(List<CalcParameter> parameterList) {
        this.parameterList = parameterList;
    }

    public List<CalcParameter> getParameterList() {
        return parameterList;
    }
    //获取参数，执行存储过程

    public void executeProcedure(ActionEvent actionEvent) {
        String calcPro = this.calcProMap.get(this.curCalcId);
        if (calcPro == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                                                         new FacesMessage("模板没有计算程序！"));
            return;
        }
        String p_template_id = this.curTempalte.getId();
        String p_com_record_id = this.curCombiantionRecord;
        String p_user_id = this.curUser.getId();
        String p_handle_mode = this.lastHandleModel;
        String p_locale = this.curUser.getLocale();
        String args = "";
        try {
            for (Map.Entry<String, String> entry :
                 this.parametersValueMap.entrySet()) {
                //若为空则抛出异常
                if (entry.getValue().equals("")) {
                    throw new NullPointerException();
                }
                //args = args + "#" + entry.getValue() ;
                args = args + "#" + entry.getKey() + ":" + entry.getValue();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                                                         new FacesMessage("存在未选择参数！"));
            return;
        }
        //System.out.println(calcPro+"&"+p_template_id+"&"+p_com_record_id+"&"+p_user_id+"&"+p_handle_mode+"&"+p_locale);
        //System.out.println(args);
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        CallableStatement cs =
            trans.createCallableStatement("{CALL " + calcPro +
                                          "(?,?,?,?,?,?,?)}", 0);
        try {
            cs.setString(1, p_template_id);
            cs.setString(2, p_com_record_id);
            cs.setString(3, p_user_id);
            cs.setString(4, p_handle_mode);
            cs.setString(5, p_locale);
            cs.setString(6, args);
            //获取返回值
            cs.registerOutParameter(7, Types.VARCHAR);
            cs.execute();
            if ("true".equals(cs.getString(7))) {
                FacesContext.getCurrentInstance().addMessage(null,
                                                             new FacesMessage("程序执行成功！"));
                trans.commit();
                //刷新数据
                this.queryTemplateData();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                                                             new FacesMessage("程序执行失败！"));
                trans.rollback();
            }
            cs.close();
        } catch (SQLException e) {
            if (e.toString().length() <= 200) {
                this.setCalcErrMsg(e.toString());
            } else {
                this.setCalcErrMsg(e.toString().substring(0, 200) + "......");
            }
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.calcErrPop.show(hint);
            this._logger.severe(e);
        }
    }
    //参数改变，将选择的值更新到Map中

    public void paraValueChange(ValueChangeEvent valueChangeEvent) {
        RichSelectOneChoice paraSoc =
            (RichSelectOneChoice)valueChangeEvent.getSource();
        try {
            String pName = paraSoc.getLabel();
            String paraValue = paraSoc.getValue().toString();
            //将选择的值put到对应的参数中
            if (this.parametersValueMap.containsKey(pName)) {
                this.parametersValueMap.put(pName, paraValue);
            }
        } catch (Exception e) {
            this._logger.severe("切换程序触发参数变化valueChangeEvent，导致空指针异常");
        }
    }
    // 数据保存后，关闭组合，提交审批

    public void commitApprove(ActionEvent actionEvent) {
        //修改输入状态表状态为已输入
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        StringBuffer updateWs = new StringBuffer();
        updateWs.append("UPDATE WORKFLOW_TEMPLATE_STATUS SET WRITE_STATUS = 'Y', ");
        updateWs.append("WRITE_BY = '").append(this.curUser.getId()).append("', ");
        updateWs.append("FINISH_AT = SYSDATE ");
        updateWs.append("WHERE RUN_ID = '").append(this.curRunId).append("' ");
        updateWs.append("AND TEMPLATE_ID = '").append(this.curTempalte.getId()).append("' ");
        updateWs.append("AND COM_ID ='").append(this.curCombiantionRecord).append("'");
        //关闭组合
        StringBuffer updateCom = new StringBuffer();
        updateCom.append("UPDATE DCM_TEMPLATE_COMBINATION SET STATUS = 'CLOSE' WHERE TEMPLATE_ID = '").append(this.curTempalte.getId()).append("' ");
        updateCom.append("AND COM_RECORD_ID = '").append(this.curCombiantionRecord).append("'");
        try {
            stat.executeUpdate(updateWs.toString());
            stat.executeUpdate(updateCom.toString());
            trans.commit();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        WorkflowEngine wfEngine = new WorkflowEngine();
        //判断下一步是否为审批，是否存在审批 runid,wfId,stepno
        Map<String, String> nextMap =
            wfEngine.queryNextStep(this.curWfId, this.curRunId, this.stepNo);
        String stepTask = nextMap.get("STEP_TASK");
        //存在，直接进入下一步，改变审批状态
        if (stepTask != null && stepTask.equals("APPROVE")) {
            ApproveflowEngine approveEgn = new ApproveflowEngine();
            try {
                //打开部门审批，发送邮件
                approveEgn.startApproveEntity(this.curRunId,
                                              this.curTempalte.getId(),
                                              this.curCombiantionRecord,
                                              this.getCurComRecordText(),
                                              this.curUser.getName());
                //改变工作流中审批步骤状态为进行中
                StringBuffer updateApp = new StringBuffer();
                updateApp.append("UPDATE DMS_WORKFLOW_STATUS SET STEP_STATUS = 'WORKING',START_AT = SYSDATE ");
                updateApp.append("WHERE WF_ID = '").append(this.curWfId).append("' ");
                updateApp.append("AND RUN_ID = '").append(this.curRunId).append("' ");
                updateApp.append("AND STEP_NO =").append(this.stepNo + 1);
                stat.executeUpdate(updateApp.toString());
                trans.commit();
            } catch (Exception e) {
                this._logger.severe(e);
            }
        } else {
            //下一步不是审批，进行父节点判断是否开始下一步
            wfEngine.startNext(this.curWfId, this.curRunId,
                               this.curTempalte.getId(),
                               this.curCombiantionRecord,
                               this.getCurComRecordText(), stepNo,
                               this.curStepTask, this.curUser.getName());
        }
        //检测步骤是否完成
        wfEngine.stepIsFinish(this.curWfId, this.curRunId, this.stepNo,
                              this.curStepTask);
        //刷新按钮不能点击
        this.curCombinationRecordEditable = false ; 
        this.writeStatus = "Y";
    }

    //审批通过

    public void approvePass(ActionEvent actionEvent) {
        ApproveflowEngine approveEgn = new ApproveflowEngine();
        //审批通过
        approveEgn.approvePass(this.curWfId, this.curRunId,
                               this.curTempalte.getId(),
                               this.curCombiantionRecord,
                               this.getCurComRecordText(),
                               this.curUser.getId(), this.curUser.getName(),
                               this.approveStepNo);
        WorkflowEngine workEngine = new WorkflowEngine();
        //检测步骤是否完成
        workEngine.stepIsFinish(this.curWfId, this.curRunId,
                                this.approveStepNo, "APPROVE");
        this.approveStatus = "Y";
        this.isEnd = true;
    }

    //审批拒绝

    public void approveRefuse(ActionEvent actionEvent) {
        ApproveflowEngine approveEgn = new ApproveflowEngine();
        approveEgn.approveRefuse(this.curRunId, this.curTempalte.getId(),
                                 this.curCombiantionRecord,
                                 this.getCurComRecordText(), this.reason,
                                 this.curUser.getName());
        WorkflowEngine workEngine = new WorkflowEngine();
        //检测步骤是否完成
        workEngine.stepIsFinish(this.curWfId, this.curRunId,
                                this.approveStepNo, "APPROVE");
        this.reason = "";
        this.approveStatus = "Y";
        this.isEnd = true;
    }

    //判断是否在工作流中，输入，审批。

    public void isWfTemplate() {
        //默认不可点击
        this.writeStatus = "Y";
        this.approveStatus = "Y";
        this.isEnd = true;
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //判断是否在工作流中，获取工作流信息
        StringBuffer sql = new StringBuffer();
        sql.append("select t3.wf_id,t2.wf_runid,t4.step_status,t4.STEP_TASK,t4.STEP_NO,t5.write_status from ");
        sql.append("dcm_template t1,dms_workflowinfo t2,dms_workflow_steps t3,dms_workflow_status t4,workflow_template_status t5 ");
        sql.append("where t2.id = t3.wf_id and t1.template_label = t3.label_object and t2.wf_runid = t4.run_id ");
        sql.append("and t3.step_no = t4.step_no and t1.id = t5.template_id and t4.run_id = t5.run_id and t4.step_no = t5.step_no ");
        sql.append("and t1.locale = t2.locale and t1.locale = t3.locale and t2.wf_status = 'Y' ");
        sql.append("and t1.locale = '").append(this.curUser.getLocale()).append("' ");
        sql.append("and t1.id = '").append(this.curTempalte.getId()).append("' ");
        sql.append("and t5.com_id = '").append(this.curCombiantionRecord).append("'");
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql.toString());
            String stepStatus = "";
            if (rs.next()) {
                stepStatus = rs.getString("STEP_STATUS");
                this.curWfId = rs.getString("WF_ID");
                this.curRunId = rs.getString("WF_RUNID");
                this.stepNo = rs.getInt("STEP_NO");
                this.writeStatus = rs.getString("WRITE_STATUS");
                this.curStepTask = rs.getString("STEP_TASK");
            }
            rs.close();
            //如果不是未输入状态，则其他情况都为Y，不可提交
            if ("N".equals(this.writeStatus) && "WORKING".equals(stepStatus)) {
                this.writeStatus = "N";
            } else {
                this.writeStatus = "Y";
            }
            //不为空则在工作流中，判断是否有审批，获取审批信息
            if (this.curRunId != null && this.curWfId != null) {
                StringBuffer approveSql = new StringBuffer();
                approveSql.append("SELECT APPROVAL_STATUS,STEP_NO FROM APPROVE_TEMPLATE_STATUS WHERE ");
                approveSql.append("RUN_ID = '").append(this.curRunId).append("' ");
                approveSql.append("AND TEMPLATE_ID = '").append(this.curTempalte.getId()).append("' ");
                approveSql.append("AND PERSON_ID = '").append(this.curUser.getId()).append("' ");
                approveSql.append("AND COM_ID = '").append(this.curCombiantionRecord).append("'");
                ResultSet aRs = stat.executeQuery(approveSql.toString());
                if (aRs.next()) {
                    String appStatus = aRs.getString("APPROVAL_STATUS");
                    this.approveStepNo = aRs.getInt("STEP_NO");
                    //close状态不能回退
                    if (appStatus.equals("Y") ||
                        appStatus.equals("APPROVEING")) {
                        this.isEnd = this.isEndNode(this.approveStepNo);
                    }

                    //System.out.println("retuen:"+isEnd);
                    //审批时可以回退
                    if (appStatus.equals("APPROVEING")) {
                        this.approveStatus = "N";
                        this.isEnd = false;
                    }
                    //System.out.println("isEnd:"+isEnd);
                }
                aRs.close();
            }
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    //判断是否最后一个父节点

    public boolean isEndNode(int step_no) {
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String nSql =
            "select step_no from approve_template_status t where t.run_id = '" +
            this.curRunId + "' and t.step_no > " + step_no +
            " and rownum = 1 order by step_no ";

        boolean flag = true;
        int nextStepNo = 0;
        try {
            ResultSet nRs = stat.executeQuery(nSql);
            if (nRs.next()) {
                nextStepNo = nRs.getInt("STEP_NO");
            }
            nRs.close();
            String rSql =
                "select 1 from approve_template_status a where a.entity_id in " +
                "(select ENTITY from dcm_entity_parent d where d.parent = " +
                "(select distinct parent from approve_template_status t1, dcm_entity_parent t2 " +
                "where t1.entity_id = t2.entity and t1.template_id = '" +
                this.curTempalte.getId() + "' " + "and t1.com_id = '" +
                this.curCombiantionRecord + "')) " + "and a.run_id = '" +
                this.curRunId + "' " + "and a.step_no = " + nextStepNo;
            ResultSet rRs = stat.executeQuery(rSql);
            if (rRs.next()) {
                flag = true;
            } else {
                flag = false;
            }
            rRs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return flag;
    }

    public void setReadonlyByRolling() {
        this.isRolling = false;
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询滚动预算配置
        String rSql =
            "SELECT YEAR,SCENARIO,VERSION,MONTH FROM DCM_ROLLING_COM";
        try {
            ResultSet rs = stat.executeQuery(rSql);
            while (rs.next()) {
                this.rollingMonth = new Number(rs.getInt("MONTH"));
                int i = 0;
                for (ComHeader hd : this.templateHeader) {
                    String code = hd.getCode();
                    String value = hd.getValue();
                    if (code.equals("YEARS") &&
                        rs.getString("YEAR").equals(value))
                        i++;
                    if (code.equals("SCENARIO") &&
                        rs.getString("SCENARIO").equals(value))
                        i++;
                    if (code.equals("VERSION") &&
                        rs.getString("VERSION").equals(value))
                        i++;
                }
                if (i == 3) {
                    this.isRolling = true;
                    break;
                }
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }

    public void setWriteStatus(String writeStatus) {
        this.writeStatus = writeStatus;
    }

    public String getWriteStatus() {
        return writeStatus;
    }

    public void setApproveStatus(String approveStatus) {
        this.approveStatus = approveStatus;
    }

    public String getApproveStatus() {
        return approveStatus;
    }


    public void setComboboxLOVBeanList(List<ComboboxLOVBean> _comboboxLOVBeanList) {
        this._comboboxLOVBeanList = _comboboxLOVBeanList;
    }

    public List<ComboboxLOVBean> getComboboxLOVBeanList() {
        return _comboboxLOVBeanList;
    }

    //回退到指定步骤
    public void backSpecifyStep(ActionEvent actionEvent) {
        this.isEnd = true;
        this.approveStatus = "Y";
        int specifyStepNo = Integer.parseInt(this.backSoc.getValue().toString());
        WorkflowEngine wfEngine = new WorkflowEngine();
        wfEngine.retreat(this.curWfId, this.curRunId, this.approveStepNo,specifyStepNo,
                         this.curTempalte.getId(), this.curCombiantionRecord,
                         this.curUser.getName(),this.backReason);
        this.backStepPop.cancel();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("回退成功！"));
    }
    
    //回退到父节点在工作流中的起点位置
    public void retreatStarted(ActionEvent actionEvent) {
        this.isEnd = true;
        this.approveStatus = "Y";
        WorkflowEngine wfEngine = new WorkflowEngine();
        wfEngine.retreatStarted(this.curWfId, this.curRunId,
                                this.approveStepNo, this.curTempalte.getId(),
                                this.curCombiantionRecord,
                                this.curUser.getName(),this.reason);
        this.reason = "";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("已回到初始状态！"));
    }
    //手动添加页面是否为dirty

    public void makeDirty(boolean isdiry) {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExtendedRenderKitService service =
            Service.getRenderKitService(facesContext,
                                        ExtendedRenderKitService.class);
        service.addScript(facesContext, "isdirty = " + isdiry + ";");
        dmsTabContext.markCurrentTabDirty(isdiry);

    }

    public void setSortTipPopup(RichPopup sortTipPopup) {
        this.sortTipPopup = sortTipPopup;
    }

    public RichPopup getSortTipPopup() {
        return sortTipPopup;
    }

    public void sortTipDialogListener(DialogEvent dialogEvent) {

        this.sortCriterions = this.saveSortCriterions;
        this.queryTemplateData();
    }

    public void setDisplayTable(RichTable displayTable) {
        this.displayTable = displayTable;
    }

    public RichTable getDisplayTable() {
        return displayTable;
    }

    public void setCalcErrPop(RichPopup calcErrPop) {
        this.calcErrPop = calcErrPop;
    }

    public RichPopup getCalcErrPop() {
        return calcErrPop;
    }

    public void setCalcErrMsg(String calcErrMsg) {
        this.calcErrMsg = calcErrMsg;
    }

    public String getCalcErrMsg() {
        return calcErrMsg;
    }

    public void setParaSocMap(Map<String, RichSelectOneChoice> paraSocMap) {
        this.paraSocMap = paraSocMap;
    }

    public Map<String, RichSelectOneChoice> getParaSocMap() {
        return paraSocMap;
    }

    public void setIsRolling(boolean isRolling) {
        this.isRolling = isRolling;
    }

    public boolean isIsRolling() {
        return isRolling;
    }

    public void setRollingMonth(Number rollingMonth) {
        this.rollingMonth = rollingMonth;
    }

    public Number getRollingMonth() {
        return rollingMonth;
    }

    public void setHasCalc(boolean hasCalc) {
        this.hasCalc = hasCalc;
    }

    public boolean isHasCalc() {
        return hasCalc;
    }

    //检验是否存在组合，如果存在组合但没有选择组合，那么无法导出数据

    public void validateExport(ActionEvent actionEvent) {

        if (templateHeader.size() > 0 && curCombiantionRecord == null) {

            FacesContext.getCurrentInstance().addMessage(null,
                                                         new FacesMessage("请先选择组合"));

        } else {
            //打开的导出框的时候应该是能重置进度款，定时器的状态。（360浏览器不大兼容所以需要到）
            if (exportProcessPoll != null && exportProIndicator != null) {
                exportProIndicator.setVisible(false);
                exportProcessPoll.setInterval(-1);


                exportButton.setVisible(true);
                AdfFacesContext.getCurrentInstance().addPartialTarget(exportButton);

                AdfFacesContext.getCurrentInstance().addPartialTarget(exportProIndicator);
                AdfFacesContext.getCurrentInstance().addPartialTarget(exportProcessPoll);
            }
            dataExportWnd.show(new RichPopup.PopupHints());

            AdfFacesContext.getCurrentInstance().addPartialTarget(dataExportWnd);
        }
    }

    public void setSocValueList(List<SelectItem> socValueList) {
        this.socValueList = socValueList;
    }

    public List<SelectItem> getSocValueList() {
        return socValueList;
    }

    private int svlSize = 0;

    public void vsc(ClientEvent clientEvent) {
        Map<String, Object> pMap = clientEvent.getParameters();
        String colLabel = pMap.get("colLabel").toString();
        colLabel = colLabel.replaceAll("[^0-9]", "");

        int idx = Integer.valueOf(colLabel);
        this.setSocValueList(socValue.get(idx));
        svlSize = socValue.get(idx).size();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.richSoc);
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.slcPop);
    }

    public void setRichSoc(RichSelectOneListbox richSoc) {
        this.richSoc = richSoc;
    }

    public RichSelectOneListbox getRichSoc() {
        return richSoc;
    }

    public void setSlcPop(RichPopup slcPop) {
        this.slcPop = slcPop;
    }

    public RichPopup getSlcPop() {
        return slcPop;
    }

    public void setSvlSize(int svlSize) {
        this.svlSize = svlSize;
    }

    public int getSvlSize() {
        return svlSize;
    }
 

    //判断是否在导出当中 0：正在导出 1：导出成功 -1：没有导出
    int isProcess = -1;


    /*
     * 定时器执行的方法，在导出的时候是无法执行定时器的，但是导出之后就会执行，因此一般只会执行一次
     * 执行一次之后就重置状态，关闭导出框，关闭定时器，隐藏进度条
     * */

    public void getExportPollProcess(PollEvent pollEvent) {

        if (isProcess == 0) {

            exportProcessPoll.setInterval(-1);
            dataExportWnd.cancel();
            isProcess = -1;
            exportProIndicator.setVisible(false);
            AdfFacesContext.getCurrentInstance().addPartialTarget(exportButton);
            AdfFacesContext.getCurrentInstance().addPartialTarget(exportProIndicator);
            AdfFacesContext.getCurrentInstance().addPartialTarget(exportProcessPoll);
            AdfFacesContext.getCurrentInstance().addPartialTarget(dataExportWnd);
        } else {

        }

    }

    private BoundedRangeModel rangeModel;
    //进度条

    public BoundedRangeModel getRangeModel() {
        if (rangeModel != null)
            return rangeModel;

        rangeModel = new RangeModel();

        return rangeModel;
    }

    public void setExportProIndicator(RichProgressIndicator exportProIndicator) {
        this.exportProIndicator = exportProIndicator;
    }

    public RichProgressIndicator getExportProIndicator() {
        return exportProIndicator;
    }

    public void setExportButton(RichCommandButton exportButton) {
        this.exportButton = exportButton;
    }

    public RichCommandButton getExportButton() {
        return exportButton;
    }

    private RichPoll exportProcessPoll;

    public void setExportProcessPoll(RichPoll exportProcessPoll) {
        this.exportProcessPoll = exportProcessPoll;
    }

    public RichPoll getExportProcessPoll() {
        return exportProcessPoll;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isIsEnd() {
        return isEnd;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public TemplateEO getCurTempalte() {
        return curTempalte;
    }
    
    //点击表中的行则显示下拉框小图标,由js调用
    public void tableClickEvent(ClientEvent clientEvent) {
        showCurrentList();
    }

    public void setBackStepPop(RichPopup backStepPop) {
        this.backStepPop = backStepPop;
    }

    public RichPopup getBackStepPop() {
        return backStepPop;
    }

    public void setBackReason(String backReason) {
        this.backReason = backReason;
    }

    public String getBackReason() {
        return backReason;
    }

    private Map<String,List<String>> backMap = new HashMap<String,List<String>>();
    private List<SelectItem> backList = new ArrayList<SelectItem>();
    public void showBackPop(ActionEvent actionEvent) {
        backMap.clear();
        backList.clear();
        DBTransaction trans =
            (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //查询当前审批步骤之前的填写表单步骤 
        String backSql = "SELECT DISTINCT T.STEP_NO,D.NAME FROM WORKFLOW_TEMPLATE_STATUS T,DCM_TEMPLATE D "
            + "WHERE T.TEMPLATE_ID = D.ID AND D.LOCALE = 'zh_CN' " + "AND T.RUN_ID = '"
            + this.curRunId + "' AND T.STEP_NO < " + this.approveStepNo + " AND T.ENTITY_CODE IN "
            + "(SELECT P.ENTITY FROM DCM_ENTITY_PARENT P WHERE P.PARENT = " 
            + "(SELECT DISTINCT P1.PARENT FROM WORKFLOW_TEMPLATE_STATUS W,DCM_ENTITY_PARENT P1 "
            + "WHERE W.ENTITY_CODE = P1.ENTITY AND W.COM_ID = '" + this.curCombiantionRecord + "')) "
            + "ORDER BY T.STEP_NO DESC";

        try {
            ResultSet bRs = stat.executeQuery(backSql);
            while(bRs.next()){
                String sNo = bRs.getString("STEP_NO");
                String tName = bRs.getString("NAME");
                if(backMap.get(sNo) == null){
                    List<String> tList = new ArrayList<String>();
                    tList.add(tName);
                    backMap.put(sNo, tList);
                    SelectItem si = new SelectItem();
                    si.setLabel("填写表单第"+sNo+"步");
                    si.setValue(sNo);
                    backList.add(si);
                }else{
                    backMap.get(sNo).add(tName);
                }
            }
            bRs.close();
            stat.close();
            this.backSoc.setValue(backList.get(0).getValue());
            ValueChangeEvent vcEvent = new ValueChangeEvent(this.backSoc,"",backList.get(0).getValue());
            this.backSocChangeValue(vcEvent);
            RichPopup.PopupHints hint = new RichPopup.PopupHints();
            this.backStepPop.show(hint);
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    
    public void backSocChangeValue(ValueChangeEvent valueChangeEvent) {
        String backNo = valueChangeEvent.getNewValue().toString();
        this.backReason = "回退涉及到的表单 ：";
        for(String name : backMap.get(backNo)){
            this.backReason += "\n\t";
            this.backReason += name;
            this.backReason += ",";
        }
    }

    public void setBackList(List<SelectItem> backList) {
        this.backList = backList;
    }

    public List<SelectItem> getBackList() {
        return backList;
    }

    public void setBackSoc(RichSelectOneChoice backSoc) {
        this.backSoc = backSoc;
    }

    public RichSelectOneChoice getBackSoc() {
        return backSoc;
    }
}

