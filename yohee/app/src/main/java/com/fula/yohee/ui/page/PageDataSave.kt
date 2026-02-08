package com.fula.yohee.ui.page

import android.app.Activity
import android.os.Handler
import android.view.*
import android.webkit.ValueCallback
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.base.BaseInfoAdapter
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.base.iview.BasePage
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.extensions.shortToast
import com.fula.yohee.utils.DiskSave
import com.fula.yohee.utils.FileUtils
import com.fula.yohee.utils.Utils
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.item_listpage_check.view.*
import java.io.File
import java.util.*
import javax.inject.Inject


/**
 * @Desc: user data copy to disk
 * @Date: 2019.03.26
 * @author: delanding
 */
class PageDataSave : BasePage(), ListPageAdapter.ItemListener {

    private lateinit var adapter: DataAdapter
    @Inject
    internal lateinit var bookmarkModel: BookmarkDatabase

    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler

    @Inject
    @field:DiskScheduler
    internal lateinit var diskScheduler: Scheduler
    private val handler = Handler()
    private val choice: MutableList<Int> = mutableListOf()

    private val diskSave: DiskSave by lazy { DiskSave(mContext) }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mContext.menuInflater.inflate(R.menu.menu_android_edit, menu)
        menu!!.getItem(0).apply {
            setTitle(R.string.import_save2disk)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_menu0 -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_DATA_SAVE_IMPORT_CLICK)
                openFileForResult()

//                val settings = FileUtils.createYohheFile("", FileUtils.SAVE_SETTINGS)
//                if (settings.exists()) {
//                    DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.setting_import_tip,
//                            positiveButton = DialogItem(title = R.string.action_yes) {
//                                CLog.i("import settings...")
//                                DiskSave(mContext).importSave2App(settings, getUserPrefer())
//                            },
//                            negativeButton = DialogItem(title = R.string.action_no) {
//                                CLog.i("open settings...")
//                                openFileForResult()
//                            }
//                    )
//                } else {
//                    openFileForResult()
//                }
            }
        }
        return true
    }

    private fun openFileForResult() {
        mContext.openFileChooser(ValueCallback { value ->
            CLog.i("value = $value")
            if (null == value) return@ValueCallback
            FileUtils.getPath(mContext, value)?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    if (!file.name.endsWith(FileUtils.SAVE_SETTING_SUFFIX)) {
                        mContext.shortToast(R.string.file_format_error)
                        return@let
                    }
                    DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.restore_copyed_data,
                            positiveButton = DialogItem(title = R.string.action_yes) {
                                CLog.i("unzip settings...")
                                diskSave.importSave2App(file, getUserPrefer())
                            }
                    )
                }
            }
        })

    }

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, com.fula.yohee.R.layout.page_items)
        YoheeApp.injector.inject(this)
        initView()
    }

    private fun initView() {
        mContext.setSupportActionBar(findViewById(com.fula.yohee.R.id.toolSetingbar) as Toolbar)
        initToolBar()

        val listPageContent = findViewById(com.fula.yohee.R.id.list_page_content) as LinearLayout
        adapter = DataAdapter(mContext, listPageContent, this)
        adapter.initAdapter()
    }

    private fun initToolBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(com.fula.yohee.R.drawable.ic_array_left)
        actionBar.title = title
    }

    override fun onItemClick(v: View, item: ListPageGroup.ListPageItem, arg: Int) {
        CLog.i("onitem click...${item.index}, arg = $arg")
        val isTrue = arg == 1
        when (item.index) {
            DATA_BOOKMARK, DATA_USERPREFER -> if (isTrue) choice.add(item.index) else choice.remove(item.index)
            ONEKEY_COPY2DISK -> {
                if (choice.isEmpty()) {
                    mContext.shortToast(R.string.please_choice_first)
                    return
                }
                if (Utils.fastClick()) return

                save2Disk()
            }
        }
    }

    private fun save2Disk() = Single.fromCallable { choice }
            .doOnSuccess {
                val sb = StringBuffer()
                it.forEach { choiceItem ->
                    sb.append(choiceItem).append("-")
                    when (choiceItem) {
                        DATA_BOOKMARK -> diskSave.exportBookmark()
                        DATA_USERPREFER -> diskSave.exportSettins(getUserPrefer())
                    }
                }
                FlurryAgent.logEvent(FlurryConst.SETTING_DATA_SAVE_EXPORT_ + sb)
            }
            .observeOn(dbScheduler)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                handler.postDelayed({
                    diskSave.zipAndShareSave(mContext)
                }, 1000)
            }

    inner class DataAdapter(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener)
        : BaseInfoAdapter(mContext, pView, listener, null) {

        override fun getItemLayout(index: Int): Int {
            return when (index) {
                ONEKEY_COPY2DISK -> super.getItemLayout(index)
                else -> R.layout.item_listpage_check
            }
        }

        override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
            return ArrayList<ListPageGroup>().apply {
                add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                    val toDisk = mContext.getString(com.fula.yohee.R.string.copy2disk)
                    add(ListPageGroup.ListPageItem(DATA_BOOKMARK, "${mContext.getString(com.fula.yohee.R.string.bookmark)}$toDisk"))
                    //add(ListPageGroup.ListPageItem(DATA_HISTORY, "${mContext.getString(com.fula.yohee.R.string.action_history)}$toDisk"))
                    add(ListPageGroup.ListPageItem(DATA_USERPREFER, "${mContext.getString(com.fula.yohee.R.string.settings)}$toDisk"))
                }))
                add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                    add(ListPageGroup.ListPageItem(ONEKEY_COPY2DISK, com.fula.yohee.R.string.onekey_copy2disk, null))
                }))
            }
        }

        override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
            return when (item.index) {
                DATA_BOOKMARK, DATA_USERPREFER -> {
                    itemView.item_check.run {
                        setOnCheckedChangeListener { _, isChecked ->
                            listener.onItemClick(itemView, item, if (isChecked) 1 else 0)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private const val DATA_BOOKMARK = 0
        private const val DATA_USERPREFER = 2
        private const val ONEKEY_COPY2DISK = 5

        private const val RESULT_OPEN_FILE = 11
    }

}

