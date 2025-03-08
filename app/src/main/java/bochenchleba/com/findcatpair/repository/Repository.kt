package bochenchleba.com.findcatpair.repository

import bochenchleba.com.findcatpair.model.CatImage
import bochenchleba.com.findcatpair.network.CatApiService

interface CatImageRepository {
    suspend fun getCatImages(limit: Int): List<CatImage>
}

class CatImageRepositoryImpl(private val catApiService: CatApiService) : CatImageRepository {
    override suspend fun getCatImages(limit: Int): List<CatImage> {
        return catApiService.getCatImages(limit)
    }
}