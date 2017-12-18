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

package com.sinyuk.fanfou.util

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.interfaces.SwipeAdapterInterface
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface

/**
 * @author sinyuk
 * @date 2017/10/25
 */

abstract class QuickSwipeAdapter<T, K : BaseViewHolder> : BaseQuickAdapter<T, K>, SwipeItemMangerInterface, SwipeAdapterInterface {

    constructor(layoutResId: Int, data: List<T>?) : super(layoutResId, data)

    constructor(data: List<T>?) : super(data)

    constructor(layoutResId: Int) : super(layoutResId)


    override fun onBindViewHolder(holder: K, position: Int, payloads: List<Any>?) {
        if (payloads == null || payloads.isEmpty()) {
            super.onBindViewHolder(holder, position)
        } else {
            throw TODO("onBindViewHolder( payloads ) Method not implement")
        }
    }

    fun swapData(data: List<T>) {
        getData().clear()
        getData().addAll(data)
    }
}
