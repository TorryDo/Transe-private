package com.torrydo.transe.di

import android.content.Context
import com.torrydo.transe.dataSource.database.LocalDatabaseRepository
import com.torrydo.transe.dataSource.database.LocalDatabaseRepositoryImpl
import com.torrydo.transe.dataSource.database.RemoteDatabaseRepository
import com.torrydo.transe.dataSource.database.RemoteDatabaseRepositoryImpl
import com.torrydo.transe.dataSource.database.local.MyRoomDatabase
import com.torrydo.transe.dataSource.database.remote.FirebaseDaoImpl
import com.torrydo.transe.dataSource.signin.FirebaseAuthenticationMethod
import com.torrydo.transe.dataSource.signin.AuthenticationMethod
import com.torrydo.transe.dataSource.translation.SearchRepository
import com.torrydo.transe.dataSource.translation.SearchRepositoryImpl
import com.torrydo.transe.dataSource.translation.eng.EngSearchImpl
import com.torrydo.transe.utils.CONSTANT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
@Named(CONSTANT.viewModelModule)
object ViewModelModule {

    @ViewModelScoped
    @Provides
    @Named(CONSTANT.viewModelLocalDB)
    fun provideDatabaseRepository(
        @ApplicationContext context: Context
    ): LocalDatabaseRepository = LocalDatabaseRepositoryImpl(
        MyRoomDatabase.getMyRoomDatabase(context).vocabDao()
    )

    @ViewModelScoped
    @Provides
    @Named(CONSTANT.viewModelSearchRepo)
    fun provideSearchRepository(): SearchRepository = SearchRepositoryImpl(EngSearchImpl())

    @ViewModelScoped
    @Provides
    @Named(CONSTANT.viewModelAuth)
    fun provideSignIn(
        @ApplicationContext context: Context
    ): AuthenticationMethod = FirebaseAuthenticationMethod(context)

    @ViewModelScoped
    @Provides
    @Named(CONSTANT.viewModelRemoteDB)
    fun provideRemoteDatabase(): RemoteDatabaseRepository =
        RemoteDatabaseRepositoryImpl(FirebaseDaoImpl())

}