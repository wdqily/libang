package plugins.work;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;


public class ReturnBook1 extends AbstractListPlugin {
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        if ("donothing".equals(args.getOperateKey())) {
            IListView listview = (IListView)this.getView();
            long rowPkId = (long) listview.getFocusRowPkId();
            DynamicObject bookBorrowObject = BusinessDataServiceHelper.loadSingle("fxyz_borrow", new QFilter[]{new QFilter("id", QCP.equals, rowPkId)});
            if (bookBorrowObject.getString("fxyz_combo_return").equalsIgnoreCase("1")) {
                this.getView().showMessage("该书籍已归还");
                return;
            }
            //更改借阅状态
            bookBorrowObject.set("fxyz_combo_return", "1");
            SaveServiceHelper.saveOperate("fxyz_borrow", new DynamicObject[] {bookBorrowObject}, null);
            //书籍大全更改书籍状态
            DynamicObject bookListObject = bookBorrowObject.getDynamicObject("fxyz_booklist");
            bookListObject.set("fxyz_combo", "0");
            SaveServiceHelper.saveOperate("fxyz_manybooks", new DynamicObject[] {bookListObject}, null);
            this.getView().showMessage("归还成功");
        }
    }
}