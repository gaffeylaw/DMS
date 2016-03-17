package team.epm.module;

import java.util.Map;

import oracle.adf.share.ADFContext;

import oracle.jbo.Session;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;
import oracle.jbo.server.ViewObjectImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Tue Mar 15 09:46:28 CST 2016
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class InfaModuleImpl extends ApplicationModuleImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public InfaModuleImpl() {
    }

    /**
     * Container's getter for InfaWorkflowVO.
     * @return InfaWorkflowVO
     */
    public ViewObjectImpl getInfaWorkflowVO() {
        return (ViewObjectImpl)findViewObject("InfaWorkflowVO");
    }

    @Override
    protected void prepareSession(Session session) {
        super.prepareSession(session);
        Map sessionScope = ADFContext.getCurrent().getSessionScope();
        Object userId = sessionScope.get("userId");
        if(userId != null){
            this.getSession().getUserData().put("userId", userId);    
        }
    }

    /**
     * Container's getter for InfaServerInfoVO.
     * @return InfaServerInfoVO
     */
    public ViewObjectImpl getInfaServerInfoVO() {
        return (ViewObjectImpl)findViewObject("InfaServerInfoVO");
    }

    /**
     * Container's getter for InfaRepInfoVO.
     * @return InfaRepInfoVO
     */
    public ViewObjectImpl getInfaRepInfoVO() {
        return (ViewObjectImpl)findViewObject("InfaRepInfoVO");
    }

    /**
     * Container's getter for InfaParameterVO.
     * @return InfaParameterVO
     */
    public ViewObjectImpl getInfaParameterVO() {
        return (ViewObjectImpl)findViewObject("InfaParameterVO");
    }

    /**
     * Container's getter for InfaWorkflowParamVO.
     * @return InfaWorkflowParamVO
     */
    public ViewObjectImpl getInfaWorkflowParamVO() {
        return (ViewObjectImpl)findViewObject("InfaWorkflowParamVO");
    }

    /**
     * Container's getter for WorkflowParamLink.
     * @return WorkflowParamLink
     */
    public ViewLinkImpl getWorkflowParamLink() {
        return (ViewLinkImpl)findViewLink("WorkflowParamLink");
    }

    /**
     * Container's getter for InfaUserWorkflowVO.
     * @return InfaUserWorkflowVO
     */
    public ViewObjectImpl getInfaUserWorkflowVO() {
        return (ViewObjectImpl)findViewObject("InfaUserWorkflowVO");
    }

    /**
     * Container's getter for InfaWorkflowCatVO.
     * @return InfaWorkflowCatVO
     */
    public ViewObjectImpl getInfaWorkflowCatVO() {
        return (ViewObjectImpl)findViewObject("InfaWorkflowCatVO");
    }

    /**
     * Container's getter for InfaWorkflowExecVO.
     * @return InfaWorkflowExecVO
     */
    public ViewObjectImpl getInfaWorkflowExecVO() {
        return (ViewObjectImpl)findViewObject("InfaWorkflowExecVO");
    }
}
