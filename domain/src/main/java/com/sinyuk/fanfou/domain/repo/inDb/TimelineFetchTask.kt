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

package com.sinyuk.fanfou.domain.repo.inDb

import android.arch.lifecycle.MutableLiveData
import android.support.annotation.WorkerThread
import com.sinyuk.fanfou.domain.*
import com.sinyuk.fanfou.domain.DO.PlayerExtracts
import com.sinyuk.fanfou.domain.DO.Resource
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.domain.api.ApiResponse
import com.sinyuk.fanfou.domain.api.RestAPI
import com.sinyuk.fanfou.domain.db.LocalDatabase
import java.io.IOException

/**
 * Created by sinyuk on 2017/12/13.
 *
 *
 */
class TimelineFetchTask(private val restAPI: RestAPI,
                        private val db: LocalDatabase,
                        private val path: String,
                        private val max: String,
                        private val pageSize: Int) : Runnable {

    val liveData = MutableLiveData<Resource<Boolean>>()
    var pathFlag: Int = 0

    init {
        liveData.value = Resource.loading(null)
        pathFlag = when (path) {
            TIMELINE_HOME -> STATUS_PUBLIC_FLAG
            TIMELINE_FAVORITES -> STATUS_FAVORTITED_FLAG
            TIMELINE_USER -> STATUS_POST_FLAG
            else -> TODO()
        }
    }

    override fun run() {
        try {
            val response = when (path) {
                TIMELINE_FAVORITES -> restAPI.fetch_favorites(count = pageSize, max = max)
                else -> restAPI.fetch_from_path(path = path, count = pageSize, max = max)
            }.execute()
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful()) {
                removeBreakChain()
                if (apiResponse.body == null || apiResponse.body.isEmpty()) {
                    liveData.postValue(null)
                } else {
                    val data = apiResponse.body
                    insertResultIntoDb(data)
                    if (data.size < pageSize) {
                        liveData.postValue(Resource.success(true))
                    } else {
                        val newResponse = when (path) {
                            TIMELINE_FAVORITES -> restAPI.fetch_favorites(count = 1, max = data.last().id)
                            else -> restAPI.fetch_from_path(path = path, count = 1, max = data.last().id)
                        }.execute()

                        if (newResponse.isSuccessful && newResponse.body()?.isNotEmpty() == true) {
                            val status = newResponse.body()!!.last()
                            try {
                                db.beginTransaction()
                                if (isBreakPoint(data.last().id, status)) {
                                    status.addBreakFlag(path)
                                    status.addPathFlag(path)
                                    status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                                    db.statusDao().insert(status)
                                }
                                db.setTransactionSuccessful()
                            } finally {
                                db.endTransaction()
                                liveData.postValue(Resource.success(true))
                            }
                        }
                    }
                }
            } else {
                liveData.postValue(Resource.error(null, false))
            }
        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, false))
        }
    }

    // 如果 item 之后有数据 但是 item 不在数据库里 so it's a break point
    private fun isBreakPoint(max: String, nextItem: Status) = if (db.statusDao().query(id = nextItem.id, path = pathFlag) != null) { // 前后数据是连贯的
        false
    } else { // 前后数据不连贯,但是也有可能后面没有数据了 比如第一次刷新
        db.statusDao().queryNext(id = max, path = pathFlag) != null
    }


    private fun removeBreakChain() {
        db.runInTransaction {
            db.statusDao().query(id = max, path = pathFlag)?.let {
                it.removeBreakFlag(path)
                db.statusDao().update(it)
            }
        }
    }

    @WorkerThread
    private fun insertResultIntoDb(body: MutableList<Status>?) {
        if (body?.isNotEmpty() == true) {
            for (status in body) {
                status.user?.let { status.playerExtracts = PlayerExtracts(it) }
                status.addPathFlag(path)
            }
            db.statusDao().inserts(body)
        }
    }
}