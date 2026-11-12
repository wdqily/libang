package plugins.work;

import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;

/**
 * 基础资料插件
 */
public class CourseBorrowBook extends AbstractListPlugin implements Plugin {
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        if ("donothing".equals(args.getOperateKey())) {
            IListView listview = (IListView)this.getView();
            Object pkObj = listview.getFocusRowPkId();
            if (pkObj == null) {
                this.getView().showMessage("请先选中一行书籍再点击借阅");
                return;
            }
            long rowPkId = Long.parseLong(pkObj.toString());
            //设置BillShowParameter
            DynamicObject bookSingle = BusinessDataServiceHelper.loadSingle("fxyz_manybooks", new QFilter[]{new QFilter("id", QCP.equals, rowPkId)});
            if (bookSingle == null) {
                this.getView().showMessage("未找到该书籍数据");
                return;
            }
            if (!bookSingle.getString("fxyz_combo").equalsIgnoreCase("0")) {
                this.getView().showMessage("该书籍已被借阅");
                return;
            }
            BillShowParameter billShowParameter = new BillShowParameter();
            billShowParameter.setFormId("fxyz_borrow_book");
            //设置宽高
            StyleCss styleCss = new StyleCss();
            styleCss.setHeight("600");
            styleCss.setWidth("1200");
            billShowParameter.getOpenStyle().setInlineStyleCss(styleCss);
            billShowParameter.getOpenStyle().setShowType(ShowType.Modal);
            //传递参数
            billShowParameter.setCustomParam("bookBaseInfo", rowPkId);
            //show出新表单
            this.getView().showForm(billShowParameter);
        }
    }
}