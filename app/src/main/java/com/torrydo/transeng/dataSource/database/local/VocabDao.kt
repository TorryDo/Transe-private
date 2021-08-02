package com.torrydo.transeng.dataSource.database.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.torrydo.transeng.dataSource.database.local.MyRoomDatabase.Companion.VOCAB_TABLE_NAME
import com.torrydo.transeng.dataSource.database.local.models.Vocab

@Dao
interface VocabDao {

    @Query("SELECT * FROM $VOCAB_TABLE_NAME")
    fun getAll(): LiveData<List<Vocab>>

//    @Query("SELECT * FROM $VOCAB_TABLE_NAME WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): LiveData<List<Vocab>>

    @Query("SELECT * FROM $VOCAB_TABLE_NAME WHERE vocab LIKE :keyword limit 1")
    fun loadVocabByKeyword(keyword : String) : Vocab?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVocab(vocab: Vocab)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg vocab: Vocab)

    @Update
    fun updateVocab(vocab: Vocab)

    @Delete
    fun delete(vocab: Vocab)

}