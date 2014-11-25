package team.epm.module;

import java.util.Map;

import oracle.adf.share.ADFContext;

import oracle.jbo.Session;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewObjectImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Fri Nov 14 17:11:23 CST 2014
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class DcmModuleImpl extends ApplicationModuleImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public DcmModuleImpl() {
    }
    @Override
    protected void prepareSession(Session session) {
        super.prepareSession(session);
        Map sessionScope = ADFContext.getCurrent().getSessionScope();
        Object userId=sessionScope.get("userId");
        if(userId!=null){
            this.getSession().getUserData().put("userId", userId);
        }
    }
    /**
     * Container's getter for DcmComVsView.
     * @return DcmComVsView
     */
    public ViewObjectImpl getDcmComVsView() {
        return (ViewObjectImpl)findViewObject("DcmComVsView");
    }

    /**
     * Container's getter for DcmCombinationView.
     * @return DcmCombinationView
     */
    public ViewObjectImpl getDcmCombinationView() {
        return (ViewObjectImpl)findViewObject("DcmCombinationView");
    }

    /**
     * Container's getter for DcmErrorView.
     * @return DcmErrorView
     */
    public ViewObjectImpl getDcmErrorView() {
        return (ViewObjectImpl)findViewObject("DcmErrorView");
    }

    /**
     * Container's getter for DcmJobView.
     * @return DcmJobView
     */
    public ViewObjectImpl getDcmJobView() {
        return (ViewObjectImpl)findViewObject("DcmJobView");
    }

    /**
     * Container's getter for DcmRoleTemplateView.
     * @return DcmRoleTemplateView
     */
    public ViewObjectImpl getDcmRoleTemplateView() {
        return (ViewObjectImpl)findViewObject("DcmRoleTemplateView");
    }

    /**
     * Container's getter for DcmTemplateColumnView.
     * @return DcmTemplateColumnView
     */
    public ViewObjectImpl getDcmTemplateColumnView() {
        return (ViewObjectImpl)findViewObject("DcmTemplateColumnView");
    }

    /**
     * Container's getter for DcmTemplateCombinationView.
     * @return DcmTemplateCombinationView
     */
    public ViewObjectImpl getDcmTemplateCombinationView() {
        return (ViewObjectImpl)findViewObject("DcmTemplateCombinationView");
    }

    /**
     * Container's getter for DcmTemplateValidationView.
     * @return DcmTemplateValidationView
     */
    public ViewObjectImpl getDcmTemplateValidationView() {
        return (ViewObjectImpl)findViewObject("DcmTemplateValidationView");
    }

    /**
     * Container's getter for DcmTemplateView.
     * @return DcmTemplateView
     */
    public ViewObjectImpl getDcmTemplateView() {
        return (ViewObjectImpl)findViewObject("DcmTemplateView");
    }

    /**
     * Container's getter for DcmValidationView.
     * @return DcmValidationView
     */
    public ViewObjectImpl getDcmValidationView() {
        return (ViewObjectImpl)findViewObject("DcmValidationView");
    }

    /**
     * Container's getter for DcmTemplateCatView.
     * @return DcmTemplateCatView1
     */
    public ViewObjectImpl getDcmTemplateCatView() {
        return (ViewObjectImpl)findViewObject("DcmTemplateCatView");
    }

    /**
     * Container's getter for DcmComVsQueryView.
     * @return DcmComVsQueryView
     */
    public ViewObjectImpl getDcmComVsQueryView() {
        return (ViewObjectImpl)findViewObject("DcmComVsQueryView");
    }
}
