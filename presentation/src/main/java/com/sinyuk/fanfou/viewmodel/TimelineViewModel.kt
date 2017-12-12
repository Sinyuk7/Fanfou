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

import android.arch.lifecycle.ViewModel
import com.sinyuk.fanfou.domain.repo.StatusRepository
import javax.inject.Inject

/**
 * Created by sinyuk on 2017/12/6.
 */
class TimelineViewModel @Inject constructor(private val repo: StatusRepository) : ViewModel() {

    fun loadTimeline(path: String, max: String?, uniqueId: String? = null) = repo.timeline(path, max)

    fun fetchNewTimeline(path: String, since: String?, uniqueId: String? = null) = repo.fetchTimeline(path, since, uniqueId)
}