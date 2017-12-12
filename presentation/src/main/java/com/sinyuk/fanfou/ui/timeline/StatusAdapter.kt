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

package com.sinyuk.fanfou.ui.timeline

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import com.daimajia.swipe.util.Attributes
import com.sinyuk.fanfou.R
import com.sinyuk.fanfou.domain.DO.Status
import com.sinyuk.fanfou.ui.player.PlayerView
import com.sinyuk.fanfou.util.QuickSwipeAdapter
import com.sinyuk.fanfou.util.addFragmentInActivity
import kotlinx.android.synthetic.main.timeline_view_list_item.view.*
import kotlinx.android.synthetic.main.timeline_view_list_item_underlayer.view.*

/**
 * Created by sinyuk on 2017/12/1.
 */
class StatusAdapter : QuickSwipeAdapter<Status, BaseViewHolder>(null) {

    var uniqueId: String? = null

    var placeholder = RecyclerView.NO_POSITION

    private val ITEM_PLACEHOLDER = Int.MAX_VALUE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = if (viewType == ITEM_PLACEHOLDER) {
        Placeholder(parent)
    } else {
        StatusViewHolder(parent)
    }

    override fun convert(holder: BaseViewHolder, status: Status?) {
        if (holder.itemViewType == ITEM_PLACEHOLDER) {
            holder as Placeholder
            holder.addOnClickListener(R.id.placeholder)
        } else {
            holder as StatusViewHolder
            if (status == null) {
                holder.clear()
            } else {
                holder.bindTo(status, uniqueId)
            }
        }
    }


    fun insertPlaceholder() {
        if (placeholder == RecyclerView.NO_POSITION) {
            placeholder = mData.size
            notifyItemInserted(mData.size)
        } else {
            if (mData.size != placeholder) {
                val temp = placeholder
                placeholder = mData.size
                notifyItemRemoved(temp)
                notifyItemInserted(placeholder)
            }
        }
    }

    fun removePlaceholder() {
        if (placeholder != RecyclerView.NO_POSITION) {
            notifyItemRemoved(placeholder)
            placeholder = RecyclerView.NO_POSITION
        }
    }

    override fun getItemCount() = if (placeholder != RecyclerView.NO_POSITION) {
        super.getItemCount() + 1
    } else {
        super.getItemCount()
    }

    override fun getItemViewType(position: Int) = if (position == placeholder) {
        ITEM_PLACEHOLDER
    } else {
        super.getItemViewType(position)
    }

    private var mItemManger = SwipeItemRecyclerMangerImpl(this)

    override fun openItem(position: Int) {
        mItemManger.openItem(position)
    }

    override fun closeItem(position: Int) {
        mItemManger.closeItem(position)
    }

    override fun closeAllExcept(layout: SwipeLayout) {
        mItemManger.closeAllExcept(layout)
    }

    override fun closeAllItems() {
        mItemManger.closeAllItems()
    }

    override fun getOpenItems(): List<Int> {
        return mItemManger.openItems
    }

    override fun getOpenLayouts(): List<SwipeLayout> {
        return mItemManger.openLayouts
    }

    override fun removeShownLayouts(layout: SwipeLayout) {
        mItemManger.removeShownLayouts(layout)
    }

    override fun isOpen(position: Int): Boolean {
        return mItemManger.isOpen(position)
    }

    override fun getMode(): Attributes.Mode {
        return mItemManger.mode
    }

    override fun setMode(mode: Attributes.Mode) {
        mItemManger.mode = mode
    }

    override fun getSwipeLayoutResourceId(position: Int): Int = R.id.swipeLayout


    class Placeholder(parent: ViewGroup) : BaseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.timeline_view_list_placeholder, parent, false)) {

    }

    class StatusViewHolder(parent: ViewGroup) : BaseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.timeline_view_list_item, parent, false)) {
        fun clear() {
            itemView.avatar.setImageDrawable(null)
            itemView.screenName.text = null
            itemView.content.text = null
            itemView.createdAt.text = null
            itemView.image.setImageDrawable(null)
            itemView.swipeLayout.close(false)
            itemView.surfaceView.setBackgroundColor(Color.WHITE)
            itemView.likeButton.setImageDrawable(null)
            itemView.deleteButton.visibility = View.GONE
        }

        fun bindTo(status: Status, uniqueId: String?) {
            itemView.avatar.setImageResource(R.mipmap.ic_launcher_round)
            itemView.screenName.text = status.playerExtracts?.screenName
            itemView.content.text = status.text
            itemView.createdAt.text = status.createdAt?.toString()
            itemView.image.setImageResource(R.mipmap.ic_launcher_round)
            itemView.swipeLayout.isClickToClose = true


            if (status.playerExtracts?.uniqueId == uniqueId || status.repostUserId == uniqueId || status.inReplyToUserId == uniqueId) {
                itemView.surfaceView.setBackgroundColor(Color.GRAY)
            } else {
                itemView.surfaceView.setBackgroundColor(Color.WHITE)
            }


            if (status.playerExtracts?.uniqueId == uniqueId) {
                itemView.deleteButton.visibility = View.VISIBLE
            } else {
                itemView.deleteButton.visibility = View.GONE
            }


            if (status.favorited) {
                itemView.likeButton.setImageResource(R.mipmap.ic_launcher_round)
            } else {
                itemView.likeButton.setImageDrawable(null)
            }

            itemView.avatar.setOnClickListener {
                @Suppress("CAST_NEVER_SUCCEEDS")
                (it.context as AppCompatActivity).addFragmentInActivity(PlayerView.newInstance(status.playerExtracts?.uniqueId), R.id.fragment_container, true)
            }

            itemView.likeButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
            itemView.repostButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
            itemView.overflowButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
            itemView.deleteButton.setOnClickListener {
                itemView.swipeLayout.close()
            }
        }
    }
}