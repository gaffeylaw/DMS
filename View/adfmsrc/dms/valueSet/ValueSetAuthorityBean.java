package dms.valueSet;

import common.ADFUtils;

import common.DmsUtils;

import common.lov.DmsComBoxLov;

import common.lov.ValueSetRow;

import dcm.DcmQueryDescriptor;

import dms.login.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adf.view.rich.event.QueryEvent;

import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.QueryDescriptor;

import oracle.jbo.Key;
import oracle.jbo.Row;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import oracle.adf.view.rich.model.AttributeDescriptor.Operator;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import org.apache.commons.lang.ObjectUtils;

public class ValueSetAuthorityBean {
    private RowKeySet selectedRows=new RowKeySetImpl(); 
    private Map valueMap;
    private static ADFLogger logger=ADFLogger.createADFLogger(ValueSetAuthorityBean.class);
    private Person person = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
    private RichTable assignedValueTable;
    private RichPopup popup;
    private RichTable unassignedValueTable;
    private RichInputText filterValueInput;
    //搜索
    private FilterableQueryDescriptor queryDescriptor=new DcmQueryDescriptor();

    private DmsComBoxLov valueLov;
    private DmsComBoxLov groupLov;
    private String groupName;
    private String valueSetName;
    
    public ValueSetAuthorityBean() {
            super();
            this.initGroupLov();
            this.initValueLov();
        }

        public void initValueLov() {
            List<SelectItem> values = new ArrayList<SelectItem>();
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql =
                "SELECT T.ID,T.NAME FROM DMS_VALUE_SET T WHERE T.LOCALE='" +
                this.person.getLocale() + "' ORDER BY T.NAME";
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                List<ValueSetRow> list = new ArrayList<ValueSetRow>();
                while (rs.next()) {
                    SelectItem item = new SelectItem();
                    item.setLabel(rs.getString("ID"));
                    item.setValue(rs.getString("NAME"));
                    values.add(item);
                    //模糊搜索框
                    ValueSetRow vsr =
                        new ValueSetRow(rs.getString("ID"), rs.getString("NAME"),
                                        rs.getString("ID"));
                    list.add(vsr);
                    this.valueSetName = rs.getString("NAME");
                }
                this.valueLov = new DmsComBoxLov(list);
                ViewObject vo =
                    ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
                String wc = " NAME = '"+this.valueSetName+"'";
                vo.setWhereClause(wc);
                vo.executeQuery();
                if (vo.hasNext()) {
                    Row row = vo.first();
                    vo.setCurrentRow(row);
                    this.refreshValueMap(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    //        this.valueSetName = "年1";
        }

        public void initGroupLov() {
            List<SelectItem> values = new ArrayList<SelectItem>();
            DBTransaction trans =
                (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql =
                "SELECT T.ID,T.NAME FROM DMS_GROUP T WHERE T.LOCALE='" + this.person.getLocale() +
                "' AND T.ENABLE_FLAG = 'Y' ORDER BY T.NAME";
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                List<ValueSetRow> list = new ArrayList<ValueSetRow>();
                while (rs.next()) {
                    SelectItem item = new SelectItem();
                    item.setLabel(rs.getString("ID"));
                    item.setValue(rs.getString("NAME"));
                    values.add(item);
                    //模糊搜索框
                    ValueSetRow vsr =
                        new ValueSetRow(rs.getString("ID"), rs.getString("NAME"),
                                        rs.getString("ID"));
                    list.add(vsr);
                    this.groupName = rs.getString("NAME");
                }
                this.groupLov = new DmsComBoxLov(list);
                ViewObject vo =
                    ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject();
                String wc = " NAME = '"+this.groupName+"'";
                vo.setWhereClause(wc);
                vo.executeQuery();
                if (vo.hasNext()) {
                    Row row = vo.first();
                    vo.setCurrentRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            //get vo
            //curRow.getAtt();
            //
        }
        
    public void setAssignedValueTable(RichTable assignedValueTable) {
        this.assignedValueTable = assignedValueTable;
    }

    public RichTable getAssignedValueTable() {
        return assignedValueTable;
    }

    public void groupChangeListener(ValueChangeEvent valueChangeEvent) {
        ViewObject vo =
                    ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject();
                String wc = " NAME = '" + valueChangeEvent.getNewValue() + "'";
                vo.setWhereClause(wc);
                vo.executeQuery();
                if (vo.hasNext()) {
                    Row row = vo.first();
                    vo.setCurrentRow(row);
                }
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
    }

    public void valuesetChangeListener(ValueChangeEvent valueChangeEvent) {
//        Row row=ADFUtils.findIterator("DmsValueSetViewIterator").getRowSetIterator().getRowAtRangeIndex((Integer)valueChangeEvent.getNewValue());
//        this.refreshValueMap(row);
        ViewObject vo =
            ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
        String wc = " NAME = '" + valueChangeEvent.getNewValue() + "'";
        vo.setWhereClause(wc);
        vo.executeQuery();
        if (vo.hasNext()) {
            Row row = vo.first();
            vo.setCurrentRow(row);
            this.refreshValueMap(row);
        }
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
    }

    public void setValueMap(Map valueMap) {
        this.valueMap = valueMap;
    }

    public Map getValueMap() {
        if(this.valueMap==null){
            this.refreshValueMap(null);
        }
        return valueMap;
    }
    private void refreshValueMap(Row row){
        this.valueMap=new HashMap();
        ViewObject valueSetVo=ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
        if(row==null){
            row=valueSetVo.getCurrentRow();
        }
        if(row!=null){
            String valueSetSrc=row.getAttribute("Source").toString().toUpperCase();
            String sql="select t.code,t.meaning from \""+valueSetSrc+"\" t where t.locale='"+person.getLocale()+"'";
            DBTransaction trans = (DBTransaction)valueSetVo.getApplicationModule().getTransaction();
            Statement stmt=trans.createStatement(DBTransaction.DEFAULT);
            try {
                ResultSet rs=stmt.executeQuery(sql);
                while(rs.next()){
                    String code=rs.getString("code");
                    String meaning=rs.getString("meaning");
                    this.valueMap.put(code,meaning);
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                this.logger.severe(e);
            }
        }
    }

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    public void add_value(ActionEvent actionEvent) {
        ADFUtils.findIterator("getDmsValueViewIterator").getViewObject().executeQuery();
        RichPopup.PopupHints hint=new RichPopup.PopupHints();
        this.popup.show(hint);
    }

    public void remove_value(ActionEvent actionEvent) {
        if (this.assignedValueTable.getSelectedRowKeys() != null) {
            Iterator itr = this.assignedValueTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =ADFUtils.findIterator("DmsGroupValueViewIterator").getRowSetIterator();
            while(itr.hasNext()){
                List key = (List)itr.next();
                Row row = rowSetIterator.getRow((Key)key.get(0));
                if(row!=null){
                    row.remove();
                }
            }
            ADFUtils.findIterator("getDmsValueViewIterator").getViewObject()
                .getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("getDmsValueViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
        }
    }

    public void add_value_authority(ActionEvent actionEvent) {
        if (this.unassignedValueTable.getSelectedRowKeys() != null) {
            ViewObject groupValueVo =ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject();
            String groupId =(String)ADFUtils.findIterator("DmsEnabledGroupViewIterator").getViewObject().getCurrentRow().getAttribute("Id");
            String valueSetId=(String)ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject().getCurrentRow().getAttribute("Id");            
            Iterator itr =
                this.unassignedValueTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("getDmsValueViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row valueRow = rowSetIterator.getRow((Key)key.get(0));
                if(valueRow == null){
                    continue;    
                }
                Row row = groupValueVo.createRow();
                row.setAttribute("GroupId", groupId);
                row.setAttribute("ValueSetId", valueSetId);
                row.setAttribute("ValueId", valueRow.getAttribute("CODE"));
                groupValueVo.insertRow(row);
            }
            groupValueVo.getApplicationModule().getTransaction().commit();
            ADFUtils.findIterator("getDmsValueViewIterator").getViewObject().executeQuery();
            ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject().executeQuery();
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
            AdfFacesContext.getCurrentInstance().addPartialTarget(this.unassignedValueTable);
        }
    }
    
    public void unassignedValueChangeListener(ValueChangeEvent valueChangeEvent) {
        String filter = (String)valueChangeEvent.getNewValue();

        ViewObject vo =
        ADFUtils.findIterator("getDmsValueViewIterator").getViewObject();

        vo.setWhereClause("MEANING like '%" + filter + "%'");
        vo.executeQuery();

        AdfFacesContext.getCurrentInstance().addPartialTarget(unassignedValueTable);
    }

    public void setUnassignedValueTable(RichTable unassignedValueTable) {
        this.unassignedValueTable = unassignedValueTable;
    }

    public RichTable getUnassignedValueTable() {
        return unassignedValueTable;
    }

    public void setSelectedRows(RowKeySet selectedRows) {
        this.selectedRows = selectedRows;
    }

    public RowKeySet getSelectedRows() {
        return selectedRows;
    }

    public void setFilterValueInput(RichInputText filterValueInput) {
        this.filterValueInput = filterValueInput;
    }

    public RichInputText getFilterValueInput() {
        return filterValueInput;
    }

    public void groupValueQuery(QueryEvent queryEvent) {
        DcmQueryDescriptor descriptor =(DcmQueryDescriptor)queryEvent.getDescriptor();
        if(descriptor.getFilterCriteria()!=null){
            for(Object key:descriptor.getFilterCriteria().keySet()){
                String filter = ObjectUtils.toString(descriptor.getFilterCriteria().get(key)).trim();
                ViewObject vo =
                    ADFUtils.findIterator("DmsGroupValueViewIterator").getViewObject();
                    vo.executeQuery();
                    Row row = vo.first();
                    while(row!=null){
                        if (!valueMap.get(row.getAttribute("ValueId")).toString().contains(filter)){
                            vo.removeCurrentRowAndRetain();    
                        } else{
                            vo.next();
                        }
                        row = vo.getCurrentRow();
                    }
                    AdfFacesContext.getCurrentInstance().addPartialTarget(this.assignedValueTable);
            }    
        }
    }

    public void setQueryDescriptor(FilterableQueryDescriptor queryDescriptor) {
        this.queryDescriptor = queryDescriptor;
    }

    public FilterableQueryDescriptor getQueryDescriptor() {
        return queryDescriptor;
    }

    public void setValueLov(DmsComBoxLov valueLov) {
        this.valueLov = valueLov;
    }

    public DmsComBoxLov getValueLov() {
        return valueLov;
    }

    public void setGroupLov(DmsComBoxLov groupLov) {
        this.groupLov = groupLov;
    }

    public DmsComBoxLov getGroupLov() {
        return groupLov;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setValueSetName(String valueSetName) {
        this.valueSetName = valueSetName;
    }

    public String getValueSetName() {
        return valueSetName;
    }
}
