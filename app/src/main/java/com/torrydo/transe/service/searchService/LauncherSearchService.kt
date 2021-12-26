package com.torrydo.transe.service.searchService

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import com.torrydo.transe.dataSource.translation.SearchRepositoryImpl
import com.torrydo.transe.dataSource.database.LocalDatabaseRepositoryImpl
import com.torrydo.transe.utils.CONSTANT
import com.torrydo.transe.utils.Converter
import com.torrydo.transe.views.launcherScreen.IconViewListener
import com.torrydo.transe.views.launcherScreen.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
@Named(CONSTANT.serviceModule)
class LauncherSearchService : Service() {

    private val TAG = "_TAG_SearchService"

    @Inject
    @Named(CONSTANT.serviceWindowManager)
    lateinit var windowManager: WindowManager

    @Inject
    @Named(CONSTANT.serviceSearchRepo)
    lateinit var searchRepoImpl: SearchRepositoryImpl

    @Inject
    @Named(CONSTANT.serviceLocalDB)
    lateinit var localDatabaseRepositoryImpl: LocalDatabaseRepositoryImpl

    private var transView: TransView? = null
    private var iconView: HomeLauncherIconView? = null
    private var binIconView: HomeLauncherBinIconView? = null

    /** deviceScreen info */
    private var deviceWidth = 0
    private var deviceHalfWidth = 0
    private var deviceHeight = 0
    private var deviceHalfHeight = 0

    companion object {
        var STATE = LAUNCHER_STATE.NULL
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            deviceWidth = it.getIntExtra(CONSTANT.DEVICE_WIDTH, 1080)
            deviceHeight = it.getIntExtra(CONSTANT.DEVICE_HEIGHT, 2340)

            deviceHalfWidth = deviceWidth / 2
            deviceHalfHeight = deviceHeight / 2
        }

        STATE = LAUNCHER_STATE.NULL

        iconView = HomeLauncherIconView(this, windowManager).apply {
            customTouch(deviceHalfWidth, deviceHalfHeight, MyIconViewCallback())
        }
        binIconView = HomeLauncherBinIconView(this, windowManager)
        transView = TransView(
            context = this,
            windowManager = windowManager,
            searchRepository = searchRepoImpl,
            localDatabaseRepository = localDatabaseRepositoryImpl
        ) {
            displayNormalView()
        }

        showIconView()

        return START_STICKY
    }

    private inner class MyIconViewCallback : IconViewListener {
        override fun actionUp() { stopServiceIfSuitableCondition() }
        override fun showBin() { showBinView() }
        override fun removeBin() { removeBinView() }
        override fun showTrans() { showTransView() }
    }

    private fun stopServiceIfSuitableCondition() {
        // get X and Y of binIcon
        val arrBin = binIconView!!.getViewXY()

        val binXmin = arrBin[0] - 150
        val binXmax = arrBin[0] + 150

        val binYmin = arrBin[1] - 150
        val binYmax = arrBin[1] + 150

        // get X and Y of Main Icon
        val iconArr = iconView!!.getViewXY()

        val currentIconX = iconArr[0]
        val currentIconY = iconArr[1]


        if (
            binXmin < currentIconX && currentIconX < binXmax
            &&
            binYmin < currentIconY && currentIconY < binYmax
        ) {
            stopThisService()
        } else {

            iconView!!.animateIconToEdge(
                currentIconX,
                Converter.toPx(27),
                deviceHalfWidth
            ) {  // onFinished
//                Log.d(TAG, "animated icon to edge")
            }
        }
    }

    private fun showIconView() {
        if (STATE == LAUNCHER_STATE.NULL || STATE == LAUNCHER_STATE.TRANS) {
            iconView?.addView()
            STATE = LAUNCHER_STATE.ICON
        }
    }

    private fun removeIconView() {
        iconView?.removeView()
    }

    private fun showBinView() {
        if (STATE == LAUNCHER_STATE.ICON) {
            binIconView?.addView()
            STATE = LAUNCHER_STATE.BIN
        }
    }

    private fun removeBinView() {
        binIconView!!.removeView()
    }

    private fun showTransView() {
        iconView?.removeView()
        transView!!.addView()
        STATE = LAUNCHER_STATE.TRANS
    }

    private fun displayNormalView() {
        if (STATE == LAUNCHER_STATE.TRANS) {
            transView?.removeView()
            showIconView()
        }
    }

    private fun stopThisService() {
        try {
            removeIconView()
            removeBinView()
//            transView?.removeView()
        } catch (e: Exception) {
            Log.e(TAG, "unable to removeView in service")
        }
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? = null
    override fun onDestroy() {
        Log.e(TAG, "service destroyed")
        super.onDestroy()
    }

}
