package bochenchleba.com.findcatpair.network

import bochenchleba.com.findcatpair.model.CatImage
import retrofit2.http.GET
import retrofit2.http.Query

interface CatApiService {
    @GET("images/search")
    suspend fun getCatImages(@Query("limit") limit: Int): List<CatImage>
}