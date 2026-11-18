package walk.walk1.walk.walk1.plugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.Date;
import java.util.EventObject;
import java.util.UUID;

/**
 * 单据界面插件
 */
public class WriteReturnBookDate extends AbstractBillPlugIn implements Plugin {
    @Override
    public void afterCreateNewData(EventObject eventObject) {
        super.afterCreateNewData(eventObject);
        this.getModel().setValue("fxyz_returndate", new Date());
    }

    @Override
    public void afterBindData(EventObject eventObject) {
        super.afterBindData(eventObject);
        // 在平台编码规则处理后补齐空编号，已有编号不覆盖。
        if (this.getModel().getDataEntityType().getProperties().containsKey("billno")) {
            Object billNo = this.getModel().getValue("billno");
            if (billNo == null || billNo.toString().trim().isEmpty()) {
                //去掉-，截取30位
                this.getModel().setValue("billno",
                        UUID.randomUUID().toString().replace("-", "").substring(0, 30));
                this.getView().updateView("billno");
            }
        }
    }
    @Override
    public void propertyChanged(PropertyChangedArgs e){
        super.propertyChanged(e);
        if (!"fxyz_borrowbill".equals(e.getProperty().getName())) {
            return;
        }
        DynamicObject refBill = (DynamicObject) e.getChangeSet()[0].getNewValue();
        // 清空借书单时顺手清借阅人
        if (refBill == null) {
            this.getModel().setValue("fxyz_borrower", null);
            return;
        }
        //按 id 重新加载完整借阅单 getPkValue()拿到id
        DynamicObject borrowBill = BusinessDataServiceHelper.loadSingle(
                refBill.getPkValue(), "fxyz_borrow_book");
        // 完整实体上取创建人（系统标准属性名就是 creator）
        DynamicObject creator = (DynamicObject) borrowBill.get("creator");
        // 创建人是用户对象，借阅人文本框要的是名字字符串
        this.getModel().setValue("fxyz_borrower",
                creator != null ? creator.get("name") : null);
    }


}
