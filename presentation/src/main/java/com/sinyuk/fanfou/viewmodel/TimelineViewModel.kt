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

package com.sinyuk.fanfou.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.ViewModel
import com.sinyuk.fanfou.domain.PAGE_SIZE
import com.sinyuk.fanfou.domain.repo.inDb.TimelineRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/6.
 *
 */
class TimelineViewModel @Inject constructor(private val repo: TimelineRepository) : ViewModel() {

    data class PathAndPlayer(val path: String, val uniqueId: String?)

    private val paramLive = MutableLiveData<PathAndPlayer>()

    fun setParams(params: PathAndPlayer): Boolean {
        if (paramLive.value == params) {
            return false
        }
        paramLive.value = params
        return true
    }

    private val repoResult = map(paramLive, { repo.timeline(path = it.path, uniqueId = it.uniqueId, pageSize = PAGE_SIZE) })
    val statuses = Transformations.switchMap(repoResult, { it.pagedList })!!
    val networkState = Transformations.switchMap(repoResult, { it.networkState })!!

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun fetchBeforeTop(since: String) = repo.fetchBeforeTop(path = paramLive.value!!.path, uniqueId = paramLive.value!!.uniqueId, since = since, pageSize = PAGE_SIZE)
}