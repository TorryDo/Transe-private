package com.torrydo.transeng.dataSource.data.eng.pronunciation

import android.content.Context
import android.util.Log
import com.torrydo.transeng.interfaces.RequestListener
import com.torrydo.transeng.utils.MyAudioManager
import com.torrydo.transeng.utils.MyDownloader
import com.torrydo.transeng.utils.MyFileManager
import java.io.File

class PronunciationAudioImpl(
    private val context: Context,
    private val url: String,
) : PronunciationAudio {

    private val TAG = "_TAG_PronunciationAudioImpl"

    companion object {
        const val PRONUNCIATION_FOLDER = "Pronunciation"
    }

    override fun downloadAndSavePronounce(
        file: File,
        requestListener: RequestListener
    ) {
        MyDownloader().download(url) { byteArray ->
            MyFileManager().saveFile(
                file,
                byteArray,
                requestListener
            )
        }
    }

    override fun play(keyWord: String) {

        val file = File(context.getExternalFilesDir(PRONUNCIATION_FOLDER), "/${keyWord}.mp3")

        if (!file.exists()) {
            downloadAndSavePronounce(file, object : RequestListener{

                override fun request() {
                    MyAudioManager.playAudioFromPath(file.absolutePath)
                    Log.i(TAG, "save pronunciation succeed")
                }

            })
        } else {
            MyAudioManager.playAudioFromPath(file.absolutePath)
        }
    }

}