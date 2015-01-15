package team.epm.dms.view;

import oracle.jbo.RowIterator;
import oracle.jbo.domain.Date;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.ViewRowImpl;

import team.epm.dms.model.DmsRoleImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Wed Jan 07 16:16:55 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DmsEnabledRoleRowImpl extends ViewRowImpl {
    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        CreatedAt {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        CreatedBy {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ,
        EnableFlag {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getEnableFlag();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setEnableFlag((String)value);
            }
        }
        ,
        Id {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getId();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        Locale {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getLocale();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setLocale((String)value);
            }
        }
        ,
        RoleName {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getRoleName();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setRoleName((String)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        DmsRoleValueView {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getDmsRoleValueView();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        DmsRoleFunctionView {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getDmsRoleFunctionView();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ,
        DcmRoleTemplateView {
            public Object get(DmsEnabledRoleRowImpl obj) {
                return obj.getDcmRoleTemplateView();
            }

            public void put(DmsEnabledRoleRowImpl obj, Object value) {
                obj.setAttributeInternal(index(), value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(DmsEnabledRoleRowImpl object);

        public abstract void put(DmsEnabledRoleRowImpl object, Object value);

        public int index() {
            return AttributesEnum.firstIndex() + ordinal();
        }

        public static final int firstIndex() {
            return firstIndex;
        }

        public static int count() {
            return AttributesEnum.firstIndex() + AttributesEnum.staticValues().length;
        }

        public static final AttributesEnum[] staticValues() {
            if (vals == null) {
                vals = AttributesEnum.values();
            }
            return vals;
        }
    }


    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();
    public static final int ENABLEFLAG = AttributesEnum.EnableFlag.index();
    public static final int ID = AttributesEnum.Id.index();
    public static final int LOCALE = AttributesEnum.Locale.index();
    public static final int ROLENAME = AttributesEnum.RoleName.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int DMSROLEVALUEVIEW = AttributesEnum.DmsRoleValueView.index();
    public static final int DMSROLEFUNCTIONVIEW = AttributesEnum.DmsRoleFunctionView.index();
    public static final int DCMROLETEMPLATEVIEW = AttributesEnum.DcmRoleTemplateView.index();

    /**
     * This is the default constructor (do not remove).
     */
    public DmsEnabledRoleRowImpl() {
    }

    /**
     * Gets DmsRole entity object.
     * @return the DmsRole
     */
    public DmsRoleImpl getDmsRole() {
        return (DmsRoleImpl)getEntity(0);
    }

    /**
     * Gets the attribute value for CREATED_AT using the alias name CreatedAt.
     * @return the CREATED_AT
     */
    public Date getCreatedAt() {
        return (Date) getAttributeInternal(CREATEDAT);
    }

    /**
     * Sets <code>value</code> as attribute value for CREATED_AT using the alias name CreatedAt.
     * @param value value to set the CREATED_AT
     */
    public void setCreatedAt(Date value) {
        setAttributeInternal(CREATEDAT, value);
    }

    /**
     * Gets the attribute value for CREATED_BY using the alias name CreatedBy.
     * @return the CREATED_BY
     */
    public String getCreatedBy() {
        return (String) getAttributeInternal(CREATEDBY);
    }

    /**
     * Sets <code>value</code> as attribute value for CREATED_BY using the alias name CreatedBy.
     * @param value value to set the CREATED_BY
     */
    public void setCreatedBy(String value) {
        setAttributeInternal(CREATEDBY, value);
    }

    /**
     * Gets the attribute value for ENABLE_FLAG using the alias name EnableFlag.
     * @return the ENABLE_FLAG
     */
    public String getEnableFlag() {
        return (String) getAttributeInternal(ENABLEFLAG);
    }

    /**
     * Sets <code>value</code> as attribute value for ENABLE_FLAG using the alias name EnableFlag.
     * @param value value to set the ENABLE_FLAG
     */
    public void setEnableFlag(String value) {
        setAttributeInternal(ENABLEFLAG, value);
    }

    /**
     * Gets the attribute value for ID using the alias name Id.
     * @return the ID
     */
    public String getId() {
        return (String) getAttributeInternal(ID);
    }

    /**
     * Sets <code>value</code> as attribute value for ID using the alias name Id.
     * @param value value to set the ID
     */
    public void setId(String value) {
        setAttributeInternal(ID, value);
    }

    /**
     * Gets the attribute value for LOCALE using the alias name Locale.
     * @return the LOCALE
     */
    public String getLocale() {
        return (String) getAttributeInternal(LOCALE);
    }

    /**
     * Sets <code>value</code> as attribute value for LOCALE using the alias name Locale.
     * @param value value to set the LOCALE
     */
    public void setLocale(String value) {
        setAttributeInternal(LOCALE, value);
    }

    /**
     * Gets the attribute value for ROLE_NAME using the alias name RoleName.
     * @return the ROLE_NAME
     */
    public String getRoleName() {
        return (String) getAttributeInternal(ROLENAME);
    }

    /**
     * Sets <code>value</code> as attribute value for ROLE_NAME using the alias name RoleName.
     * @param value value to set the ROLE_NAME
     */
    public void setRoleName(String value) {
        setAttributeInternal(ROLENAME, value);
    }

    /**
     * Gets the attribute value for UPDATED_AT using the alias name UpdatedAt.
     * @return the UPDATED_AT
     */
    public Date getUpdatedAt() {
        return (Date) getAttributeInternal(UPDATEDAT);
    }

    /**
     * Sets <code>value</code> as attribute value for UPDATED_AT using the alias name UpdatedAt.
     * @param value value to set the UPDATED_AT
     */
    public void setUpdatedAt(Date value) {
        setAttributeInternal(UPDATEDAT, value);
    }

    /**
     * Gets the attribute value for UPDATED_BY using the alias name UpdatedBy.
     * @return the UPDATED_BY
     */
    public String getUpdatedBy() {
        return (String) getAttributeInternal(UPDATEDBY);
    }

    /**
     * Sets <code>value</code> as attribute value for UPDATED_BY using the alias name UpdatedBy.
     * @param value value to set the UPDATED_BY
     */
    public void setUpdatedBy(String value) {
        setAttributeInternal(UPDATEDBY, value);
    }

    /**
     * Gets the associated <code>RowIterator</code> using master-detail link DmsRoleValueView.
     */
    public RowIterator getDmsRoleValueView() {
        return (RowIterator)getAttributeInternal(DMSROLEVALUEVIEW);
    }

    /**
     * Gets the associated <code>RowIterator</code> using master-detail link DmsRoleFunctionView.
     */
    public RowIterator getDmsRoleFunctionView() {
        return (RowIterator)getAttributeInternal(DMSROLEFUNCTIONVIEW);
    }

    /**
     * Gets the associated <code>RowIterator</code> using master-detail link DcmRoleTemplateView.
     */
    public RowIterator getDcmRoleTemplateView() {
        return (RowIterator)getAttributeInternal(DCMROLETEMPLATEVIEW);
    }

    /**
     * getAttrInvokeAccessor: generated method. Do not modify.
     * @param index the index identifying the attribute
     * @param attrDef the attribute

     * @return the attribute value
     * @throws Exception
     */
    protected Object getAttrInvokeAccessor(int index,
                                           AttributeDefImpl attrDef) throws Exception {
        if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
            return AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].get(this);
        }
        return super.getAttrInvokeAccessor(index, attrDef);
    }

    /**
     * setAttrInvokeAccessor: generated method. Do not modify.
     * @param index the index identifying the attribute
     * @param value the value to assign to the attribute
     * @param attrDef the attribute

     * @throws Exception
     */
    protected void setAttrInvokeAccessor(int index, Object value,
                                         AttributeDefImpl attrDef) throws Exception {
        if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
            AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
            return;
        }
        super.setAttrInvokeAccessor(index, value, attrDef);
    }
}
