package team.epm.dcm.model;

import oracle.jbo.Key;
import oracle.jbo.domain.Date;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;
import oracle.jbo.server.TransactionEvent;

import org.apache.commons.lang.ObjectUtils;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Mon Jan 26 10:10:58 CST 2015
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DcmRoleTemplateImpl extends EntityImpl {
    private static EntityDefImpl mDefinitionObject;

    /**
     * AttributesEnum: generated enum for identifying attributes and accessors. DO NOT MODIFY.
     */
    public enum AttributesEnum {
        Id {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getId();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setId((String)value);
            }
        }
        ,
        RoleId {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getRoleId();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setRoleId((String)value);
            }
        }
        ,
        TemplateId {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getTemplateId();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setTemplateId((String)value);
            }
        }
        ,
        CreatedAt {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getCreatedAt();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setCreatedAt((Date)value);
            }
        }
        ,
        UpdatedAt {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getUpdatedAt();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setUpdatedAt((Date)value);
            }
        }
        ,
        UpdatedBy {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getUpdatedBy();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setUpdatedBy((String)value);
            }
        }
        ,
        CreatedBy {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getCreatedBy();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setCreatedBy((String)value);
            }
        }
        ,
        ReadOnly {
            public Object get(DcmRoleTemplateImpl obj) {
                return obj.getReadOnly();
            }

            public void put(DcmRoleTemplateImpl obj, Object value) {
                obj.setReadOnly((String)value);
            }
        }
        ;
        private static AttributesEnum[] vals = null;
        private static final int firstIndex = 0;

        public abstract Object get(DcmRoleTemplateImpl object);

        public abstract void put(DcmRoleTemplateImpl object, Object value);

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
    public static final int ID = AttributesEnum.Id.index();
    public static final int ROLEID = AttributesEnum.RoleId.index();
    public static final int TEMPLATEID = AttributesEnum.TemplateId.index();
    public static final int CREATEDAT = AttributesEnum.CreatedAt.index();
    public static final int UPDATEDAT = AttributesEnum.UpdatedAt.index();
    public static final int UPDATEDBY = AttributesEnum.UpdatedBy.index();
    public static final int CREATEDBY = AttributesEnum.CreatedBy.index();
    public static final int READONLY = AttributesEnum.ReadOnly.index();

    /**
     * This is the default constructor (do not remove).
     */
    public DcmRoleTemplateImpl() {
    }
    @Override
    protected void prepareForDML(int operation,
                                 TransactionEvent transactionEvent) {
        super.prepareForDML(operation, transactionEvent);
        if (operation == DML_UPDATE) {
            this.setUpdatedAt(new Date(new java.sql.Timestamp(System.currentTimeMillis())));
            this.setUpdatedBy(ObjectUtils.toString(this.getDBTransaction().getSession().getUserData().get("userId")));
        }
    }
    /**
     * Gets the attribute value for Id, using the alias name Id.
     * @return the Id
     */
    public String getId() {
        return (String)getAttributeInternal(ID);
    }

    /**
     * Sets <code>value</code> as the attribute value for Id.
     * @param value value to set the Id
     */
    public void setId(String value) {
        setAttributeInternal(ID, value);
    }

    /**
     * Gets the attribute value for RoleId, using the alias name RoleId.
     * @return the RoleId
     */
    public String getRoleId() {
        return (String)getAttributeInternal(ROLEID);
    }

    /**
     * Sets <code>value</code> as the attribute value for RoleId.
     * @param value value to set the RoleId
     */
    public void setRoleId(String value) {
        setAttributeInternal(ROLEID, value);
    }

    /**
     * Gets the attribute value for TemplateId, using the alias name TemplateId.
     * @return the TemplateId
     */
    public String getTemplateId() {
        return (String)getAttributeInternal(TEMPLATEID);
    }

    /**
     * Sets <code>value</code> as the attribute value for TemplateId.
     * @param value value to set the TemplateId
     */
    public void setTemplateId(String value) {
        setAttributeInternal(TEMPLATEID, value);
    }

    /**
     * Gets the attribute value for CreatedAt, using the alias name CreatedAt.
     * @return the CreatedAt
     */
    public Date getCreatedAt() {
        return (Date)getAttributeInternal(CREATEDAT);
    }

    /**
     * Sets <code>value</code> as the attribute value for CreatedAt.
     * @param value value to set the CreatedAt
     */
    public void setCreatedAt(Date value) {
        setAttributeInternal(CREATEDAT, value);
    }

    /**
     * Gets the attribute value for UpdatedAt, using the alias name UpdatedAt.
     * @return the UpdatedAt
     */
    public Date getUpdatedAt() {
        return (Date)getAttributeInternal(UPDATEDAT);
    }

    /**
     * Sets <code>value</code> as the attribute value for UpdatedAt.
     * @param value value to set the UpdatedAt
     */
    public void setUpdatedAt(Date value) {
        setAttributeInternal(UPDATEDAT, value);
    }

    /**
     * Gets the attribute value for UpdatedBy, using the alias name UpdatedBy.
     * @return the UpdatedBy
     */
    public String getUpdatedBy() {
        return (String)getAttributeInternal(UPDATEDBY);
    }

    /**
     * Sets <code>value</code> as the attribute value for UpdatedBy.
     * @param value value to set the UpdatedBy
     */
    public void setUpdatedBy(String value) {
        setAttributeInternal(UPDATEDBY, value);
    }

    /**
     * Gets the attribute value for CreatedBy, using the alias name CreatedBy.
     * @return the CreatedBy
     */
    public String getCreatedBy() {
        return (String)getAttributeInternal(CREATEDBY);
    }

    /**
     * Sets <code>value</code> as the attribute value for CreatedBy.
     * @param value value to set the CreatedBy
     */
    public void setCreatedBy(String value) {
        setAttributeInternal(CREATEDBY, value);
    }

    /**
     * Gets the attribute value for ReadOnly, using the alias name ReadOnly.
     * @return the ReadOnly
     */
    public String getReadOnly() {
        return (String)getAttributeInternal(READONLY);
    }

    /**
     * Sets <code>value</code> as the attribute value for ReadOnly.
     * @param value value to set the ReadOnly
     */
    public void setReadOnly(String value) {
        setAttributeInternal(READONLY, value);
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

    /**
     * @param id key constituent

     * @return a Key object based on given key constituents.
     */
    public static Key createPrimaryKey(String id) {
        return new Key(new Object[]{id});
    }

    /**
     * @return the definition object for this instance class.
     */
    public static synchronized EntityDefImpl getDefinitionObject() {
        if (mDefinitionObject == null) {
            mDefinitionObject = EntityDefImpl.findDefObject("team.epm.dcm.model.DcmRoleTemplate");
        }
        return mDefinitionObject;
    }
}
