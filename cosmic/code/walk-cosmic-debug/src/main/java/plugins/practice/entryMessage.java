package plugins.practice;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 基础资料插件
 */
public class entryMessage extends AbstractBasePlugIn implements Plugin {
    public void afterCreateNewData(EventObject e){
        super.afterCreateNewData(e);
        //获取座位名字
        RequestContext rc = RequestContext.get();
        String userName = rc.getUserName();
        this.getView().showMessage("I'm"+userName);

    }
}