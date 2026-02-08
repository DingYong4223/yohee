package com.fula.yohee.ui.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.fula.permission.PermissionsManager
import com.fula.CLog
import org.greenrobot.eventbus.EventBus

abstract class BaseFragment : Fragment() {

    protected var visiable: Boolean = false
    val mContext: Context by lazy { requireNotNull(context) { "Context should never be null in onCreate" } }

    protected fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterEventBus()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visiable = isVisibleToUser
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        CLog.i("permission changed...")
        PermissionsManager.notifyPermissionsChange(permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
