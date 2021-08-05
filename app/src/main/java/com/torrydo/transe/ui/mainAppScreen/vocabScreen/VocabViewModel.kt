package com.torrydo.transe.ui.mainAppScreen.vocabScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torrydo.transe.dataSource.database.LocalDatabaseRepository
import com.torrydo.transe.dataSource.database.RemoteDatabaseRepository
import com.torrydo.transe.dataSource.database.local.models.Vocab
import com.torrydo.transe.dataSource.database.remote.BaseVocab
import com.torrydo.transe.dataSource.signin.AuthenticationMethod
import com.torrydo.transe.dataSource.translation.SearchRepository
import com.torrydo.transe.dataSource.translation.eng.models.EngResult
import com.torrydo.transe.interfaces.ListResultListener
import com.torrydo.transe.interfaces.ResultListener
import com.torrydo.transe.utils.CONSTANT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Date
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
@Named(CONSTANT.viewModelModule)
class VocabViewModel @Inject constructor(
    @Named(CONSTANT.viewModelSearchRepo) val searchRepository: SearchRepository,
    @Named(CONSTANT.viewModelLocalDB) val localDatabaseRepository: LocalDatabaseRepository,
    @Named(CONSTANT.viewModelRemoteDB) val remoteDatabaseRepository: RemoteDatabaseRepository,
    @Named(CONSTANT.viewModelAuth) val authentionMethod: AuthenticationMethod
) : ViewModel() {

    private val TAG = "_TAG_FirebaseDaoImpl"

    var resultList: LiveData<List<Vocab>> = MutableLiveData()

    init {
        viewModelScope.launch {
            resultList = localDatabaseRepository.getAll()
        }
    }

    fun deleteVocab(vocab: Vocab) {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.delete(vocab)
        }
    }

    fun insertAllToRemoteDatabase(vocabList: List<Vocab>) {
        viewModelScope.launch(Dispatchers.IO) {
            getUserID()?.let { uid ->
                vocabList.forEach { vocab ->

                    remoteDatabaseRepository.setUserID(uid)
                    remoteDatabaseRepository.insert(
                        convertToRemoteVocab(vocab),
                        object : ResultListener {
                            override fun <T> onSuccess(data: T?) {
                                Log.i(TAG, "insertion succeed")
                            }
                        })

                }
            }
        }
    }

    fun syncAllVocabFromRemoteDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            getUserID()?.let { uid ->

                remoteDatabaseRepository.setUserID(uid)
                remoteDatabaseRepository.getAll(object : ListResultListener {
                    override fun <T> onSuccess(dataList: List<T>) {
                        if (dataList.isNotEmpty() && dataList[0] is BaseVocab) {
                            viewModelScope.launch(Dispatchers.IO) {

                                val baseVocabList = dataList as List<BaseVocab>
                                val vocabList = resultList.value

                                val differenceVocabList = getDifferenceElement(vocabList, baseVocabList)

                                differenceVocabList.forEach { baseVocab ->
                                    convertToVocab(baseVocab) { vocab ->
                                        viewModelScope.launch(Dispatchers.IO) {
                                            localDatabaseRepository.insert(vocab)
                                        }
                                    }
                                }

                            }
                        }
                    }

                    override fun onError(e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                })

            }
        }
    }

    // --------------------------- private func --------------------------

    private fun convertToRemoteVocab(vocab: Vocab): BaseVocab {
        val keyWord = vocab.vocab
        val time = vocab.time.time.toString()
        val isFinished = false.toString()
        return BaseVocab(keyWord, time.toLong(), isFinished.toBoolean())
    }

    private fun convertToVocab(
        baseVocab: BaseVocab,
        isReady: (vocab: Vocab) -> Unit
    ) {
        searchRepository.getEnglishSource(baseVocab.keyWord, object : ListResultListener {
            override fun <T> onSuccess(dataList: List<T>) {
                if (dataList[0] is EngResult) {
                    val engResultList = dataList as ArrayList<EngResult>
                    val vocab = Vocab(
                        uid = 0,
                        vocab = baseVocab.keyWord,
                        time = Date(baseVocab.time),
                        finished = baseVocab.finished,
                        contentEng = engResultList,
                        contentVi = emptyList()
                    )

                    isReady(vocab)

                }
            }

        })
    }


    private fun getUserID() = authentionMethod.getUserAccountInfo()?.uid
    private fun getDifferenceElement(
        vocabList: List<Vocab>?,
        baseVocabList: List<BaseVocab>
    ): List<BaseVocab> {

        if (vocabList.isNullOrEmpty()) return baseVocabList
        if (baseVocabList.isNullOrEmpty()) return emptyList()

        val strVocab = ArrayList<String>()
        val differenceRemoteVocab = ArrayList<BaseVocab>()

        vocabList.forEach { vocab ->
            strVocab.add(vocab.vocab)
        }
        baseVocabList.forEach { baseVocab ->
            val keyWord = baseVocab.keyWord

            val b = strVocab.contains(keyWord)
            if (!b) {
                Log.i(TAG, "difference is : $keyWord")
                differenceRemoteVocab.add(baseVocab)
            }
        }
        return differenceRemoteVocab

    }

}
