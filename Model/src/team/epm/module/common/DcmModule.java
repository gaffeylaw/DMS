package team.epm.module.common;

import oracle.jbo.ApplicationModule;
import oracle.jbo.ViewObject;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Sun Nov 30 12:52:10 CST 2014
// ---------------------------------------------------------------------
public interface DcmModule extends ApplicationModule {
    ViewObject getCombinationRecordView(String templateId,
                                        String combinationId);

    ViewObject getSubmitStatusVO(String sql, String templateId, String userId);
}
