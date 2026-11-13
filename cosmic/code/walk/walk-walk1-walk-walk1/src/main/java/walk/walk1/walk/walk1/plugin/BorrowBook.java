package walk.walk1.walk.walk1.plugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 单据界面插件：借阅界面自动填充字段 + 借阅提交
 */
public class BorrowBook extends AbstractBillPlugIn implements Plugin {

    /*
     * 关闭页面忽略参数变化
     **/
    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        e.setCheckDataChange(false);
    }

    @Override
    public void registerListener(EventObject e) {
        this.addItemClickListeners("tbmain");
    }

    @Override
    public void beforeBindData(EventObject event) {
        //接受传递参数，自动填充书籍字段
        Object pkObj = this.getView().getFormShowParameter().getCustomParam("bookBaseInfo");
        if (pkObj != null) {
            long pkValue = Long.parseLong(pkObj.toString());
            this.getModel().setValue("fxyz_booklist", pkValue);
            //加载书籍数据，带出名称、类别
            DynamicObject book = BusinessDataServiceHelper.loadSingle("fxyz_manybooks",
                    new QFilter[]{new QFilter("id", QCP.equals, pkValue)});
            if (book != null) {
                this.getModel().setValue("fxyz_name", book.getString("name"));
                this.getModel().setValue("fxyz_type", book.getString("fxyz_type"));
            }
        }
    }

    @Override
    public void afterCreateNewData(EventObject eventObject) {
        //随机生成单据编号，填给系统单据编号字段billno
        //注意：该事件在列表/基础资料首次 loadData 时也会触发，且部分实体（如基础资料）没有 billno 字段，
        //因此必须先判断属性是否存在，否则 setValue 会抛 IllegalArgumentException: 属性:billno不存在
        if (!this.getModel().getDataEntityType().getProperties().containsKey("billno")) {
            return;
        }
        long billNo = (long) (Math.random()*Math.pow(10,15));
        this.getModel().setValue("billno", billNo);
    }

    @Override
    public void itemClick(ItemClickEvent ee) {
        if ("fxyz_borrow_btn".equalsIgnoreCase(ee.getItemKey())) {
            Object pkObj = this.getView().getFormShowParameter().getCustomParam("bookBaseInfo");
            if (pkObj == null) {
                this.getView().showMessage("未获取到书籍信息，请从书籍列表进入借阅");
                return;
            }
            long pkValue = Long.parseLong(pkObj.toString());
            int day = (int) this.getModel().getValue("fxyz_day");
            if (day <= 0) {
                this.getView().showMessage("请正确填写借阅天数");
                return;
            }
            //执行提交操作（实体编码必须与CourseBorrowBook中setFormId的一致）
            OperationResult submit = OperationServiceHelper.executeOperate("submit", "fxyz_borrow_book",
                    new DynamicObject[]{this.getModel().getDataEntity()}, OperateOption.create());
            if (!submit.isSuccess()) {
                this.getView().showMessage("借阅提交失败：" + submit.getMessage());
                return;
            }
            //获取书籍基础资料，更新借阅状态（load与save必须使用同一个实体）
            DynamicObject bookSingle = BusinessDataServiceHelper.loadSingle("fxyz_manybooks",
                    new QFilter[]{new QFilter("id", QCP.equals, pkValue)});
            if (bookSingle != null) {
                bookSingle.set("fxyz_combo", "1");
                SaveServiceHelper.saveOperate("fxyz_manybooks", new DynamicObject[] {bookSingle}, null);
            }
            this.getView().showMessage("借阅成功");
            this.getView().close();
        }
    }
}