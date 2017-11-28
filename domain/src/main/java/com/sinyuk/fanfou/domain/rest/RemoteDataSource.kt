/*
 *
 *  * Apache License
 *  *
 *  * Copyright [2017] Sinyuk
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.sinyuk.fanfou.domain.rest

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sinyuk.fanfou.domain.AUTHOR_FAILED_MSG
import com.sinyuk.fanfou.domain.BuildConfig
import com.sinyuk.fanfou.domain.entities.User
import com.sinyuk.fanfou.domain.utils.XauthUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor


/**
 * Created by sinyuk on 2017/11/28.
 */
class RemoteDataSource constructor(application: Application, endpoint: Endpoint, interceptor: Oauth1SigningInterceptor) : RemoteTasks {
    override fun updateProfile(params: SortedMap<String, Any>): Single<User> = restAPI.update_profile(params).map(ErrorCheckFunction(gson))

    @Throws(IOException::class)
    override fun showUser(params: SortedMap<String, Any>): Single<User> = restAPI.user_show(params).map(ErrorCheckFunction(gson))


    override fun requestToken(account: String, password: String): Single<Authorization?> = Single.fromCallable({
        val url: HttpUrl = XauthUtils.getInstance(account, password).url()
        val request = Request.Builder().url(url).build()

        val response: Response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw VisibleThrowable(AUTHOR_FAILED_MSG)
        }
        val text = response.body()!!.string()
        val querySpilt = text.split("&")
        try {
            val tokenAttr = querySpilt[0].split("=".toRegex())
            val secretAttr = querySpilt[1].split("=".toRegex())
            val token = tokenAttr[1]
            val secret = secretAttr[1]
            Authorization(token, secret)

        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            throw VisibleThrowable(AUTHOR_FAILED_MSG)
        }
    }).subscribeOn(Schedulers.io())


    private val MAX_HTTP_CACHE = (1024 * 1024 * 100).toLong()
    private val TIMEOUT: Long = 10
    private var okHttpClient: OkHttpClient
    private val gson: Gson = GsonBuilder()
            // Blank fields are included as null instead of being omitted.
            .serializeNulls()
            // convert timestamp to date
            .setDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy")
            .create()
    private val adapter: Retrofit
    private val restAPI: RestAPI


    init {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient = OkHttpClient.Builder()
                .cache(Cache(application.getExternalFilesDir("http_cache"), MAX_HTTP_CACHE))
                .addInterceptor(interceptor)
                .addNetworkInterceptor(interceptor)
                .addInterceptor(logging)
//                .addNetworkInterceptor { it -> it.proceed(it.request().newBuilder().header("User-Agent", generateUA()).build()) }
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()


        adapter = Retrofit.Builder()
                .baseUrl(endpoint.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttpClient)
                .build()

        restAPI = adapter.create(RestAPI::class.java)
    }


    private fun generateUA(): String = "Fanfou_Android_" + BuildConfig.BUILD_TYPE + "_" + BuildConfig.VERSION_NAME + "_" + BuildConfig.FLAVOR
}