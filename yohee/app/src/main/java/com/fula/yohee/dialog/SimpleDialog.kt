package com.fula.yohee.dialog

import android.app.Activity
import com.fula.yohee.R
import com.fula.yohee.database.HistoryDatabase
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.utils.WebUtils
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus

/**与自己业务逻辑相关的弹框展示，所有弹框附带业务逻辑操作*/
object SimpleDialog {

    @JvmStatic
    fun clearHistoryDialog(context: Activity, historyModel: HistoryDatabase, dbScheduler: Scheduler) {
        DialogHelper.showOkCancelDialog(context, R.string.alert_warm, R.string.alert_clear_all,
                positiveButton = DialogItem(title = R.string.action_yes) {
                    Completable.fromAction { WebUtils.clearHistory(context, historyModel, dbScheduler) }
                            .subscribeOn(dbScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                EventBus.getDefault().post(DrawerEvent(DrawerEvent.HISTORY_DATA_REMOVED, 0))
                            }
                }
        )
    }

}
