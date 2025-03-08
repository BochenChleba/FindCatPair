package bochenchleba.com.findcatpair.di

import bochenchleba.com.findcatpair.game.GameViewModel
import bochenchleba.com.findcatpair.repository.CatImageRepository
import bochenchleba.com.findcatpair.repository.CatImageRepositoryImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Network
    single { NetworkModule.provideOkHttpClient() }
    single { NetworkModule.provideRetrofit(get()) }
    single { NetworkModule.provideCatApiService(get()) }

    // Repository
    single<CatImageRepository> { CatImageRepositoryImpl(get()) }

    // ViewModel
    viewModel { GameViewModel(androidApplication(), get()) }
}