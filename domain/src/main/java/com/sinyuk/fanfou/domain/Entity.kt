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

package com.sinyuk.fanfou.domain

/**
 * Created by sinyuk on 2017/12/23.
 */

const val STATUS_PUBLIC_FLAG = 0x00000001
const val STATUS_FAVORITED_FLAG = 0x00000010
const val STATUS_POST_FLAG = 0x00000100

fun convertPathToFlag(path: String): Int = when (path) {
    TIMELINE_HOME -> STATUS_PUBLIC_FLAG
    TIMELINE_FAVORITES -> STATUS_FAVORITED_FLAG
    TIMELINE_USER -> STATUS_POST_FLAG
    else -> TODO()
}