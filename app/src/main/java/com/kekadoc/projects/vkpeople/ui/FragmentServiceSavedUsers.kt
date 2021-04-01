package com.kekadoc.projects.vkpeople.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.kekadoc.projects.vkpeople.ActivityViewModel
import com.kekadoc.projects.vkpeople.MainActivity
import com.kekadoc.projects.vkpeople.R
import com.kekadoc.projects.vkpeople.vkapi.data.VKUserPreview
import com.kekadoc.projects.vkpeople.util.RequestCallback
import com.kekadoc.projects.vkpeople.vkapi.startVKUserProfile
import com.kekadoc.tools.android.fragment.dpToPx
import kotlin.math.abs
import kotlin.math.min

class FragmentServiceSavedUsers : Fragment(R.layout.fragment_service_saved_users) {

    private val viewModel by activityViewModels<ActivityViewModel>()

    private val userViewCallback = UserViewCallback()
    private val adapter = Adapter(userViewCallback)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = this@FragmentServiceSavedUsers.adapter
            addItemDecoration(ItemsDecorator())
            ItemTouchHelper(SwipeToDeleteCallback(userViewCallback)).attachToRecyclerView(this)
        }
        val message = view.findViewById<LinearLayout>(R.id.linearLayout_message)

        viewModel.savedUsers.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                recyclerView.visibility = View.INVISIBLE
                message.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                message.visibility = View.INVISIBLE
            }
            adapter.submitList(it)
        }

    }

    private inner class ItemsDecorator : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val size = dpToPx(4f).toInt()
            outRect.set(size, size, size, size)
        }
    }

    private inner class UserViewCallback : UserViewActionCallback, UserViewSwipeCallback {
        override fun onAction(user: VKUserPreview) {
            viewModel.requestUser(user.id, object : RequestCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    (requireActivity() as MainActivity).navController.navigate(R.id.action_fragmentSavedUsers_to_destination_service_randomUsers)
                }
            })
        }
        override fun onSwipeLeft(viewHolder: VH) {
            val holderPosition = viewHolder.adapterPosition
            val id = viewHolder.user!!.id
            viewModel.deleteSavedUser(id, object : RequestCallback<Unit> {
                override fun onFail(error: Throwable) {
                    adapter.notifyItemChanged(holderPosition)
                    viewHolder.indicatorRight.hide()
                    Snackbar.make(
                        requireView(),
                        R.string.message_user_delete_fail,
                        Snackbar.LENGTH_SHORT
                    ).setAction(R.string.message_user_delete_fail_action) {
                        viewModel.deleteSavedUser(id, this)
                    }.show()
                }
                override fun onStart() {
                    viewHolder.indicatorRight.show()
                }
                override fun onSuccess(result: Unit) {
                    viewHolder.indicatorRight.hide()
                    Snackbar.make(requireView(), R.string.message_user_delete_success, Snackbar.LENGTH_LONG)
                            .setAction(R.string.message_user_delete_success_action) {
                                viewModel.saveUser(id)
                            }.show()
                }
            })
        }
        override fun onSwipeRight(viewHolder: VH) {
            val holderPosition = viewHolder.adapterPosition
            val id = viewHolder.user!!.id
            viewHolder.indicatorLeft.show()
            this@FragmentServiceSavedUsers.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_STOP) {
                        adapter.notifyItemChanged(holderPosition)
                        this@FragmentServiceSavedUsers.lifecycle.removeObserver(this)
                        viewHolder.indicatorLeft.hide()
                    }
                }
            })
            requireActivity().startVKUserProfile(id)
        }
    }

    private interface UserViewActionCallback {
        fun onAction(user: VKUserPreview)
    }
    private interface UserViewSwipeCallback {
        fun onSwipeLeft(viewHolder: VH)
        fun onSwipeRight(viewHolder: VH)
    }

    private class VH(itemView: View, private val callback: UserViewActionCallback) : RecyclerView.ViewHolder(itemView) {

        val indicatorLeft: CircularProgressIndicator = itemView.findViewById(R.id.circularProgressIndicator_left)
        val indicatorRight: CircularProgressIndicator = itemView.findViewById(R.id.circularProgressIndicator_right)
        val cardView: MaterialCardView = itemView.findViewById<MaterialCardView>(R.id.materialCardView).apply {
            setOnClickListener { user?.let { callback.onAction(it) } }
        }
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)

        var user: VKUserPreview? = null
            set(value) {
                field = value
                imageView.load(user?.photo_200)
                textView.text = user?.getFullName()
            }

    }

    private class Adapter(val callback: UserViewActionCallback) : ListAdapter<VKUserPreview, VH>(DiffUtilCallback()) {

        private var inflater: LayoutInflater? = null

        private fun getLayoutInflater(context: Context): LayoutInflater {
            if (inflater == null) inflater = LayoutInflater.from(context)
            return inflater!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(getLayoutInflater(parent.context).inflate(R.layout.saved_user_view, parent, false), callback)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.user = getItem(position)
        }

    }

    private class DiffUtilCallback : DiffUtil.ItemCallback<VKUserPreview>() {
        override fun areItemsTheSame(oldItem: VKUserPreview, newItem: VKUserPreview): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: VKUserPreview, newItem: VKUserPreview): Boolean {
            return oldItem == newItem
        }
    }

    private class SwipeToDeleteCallback(private val callback: UserViewSwipeCallback)
        : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return true
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (viewHolder != null)
                getDefaultUIUtil().onSelected(getForegroundView(viewHolder))
        }
        override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView,
                                     viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int,
                                     isCurrentlyActive: Boolean) {
            getDefaultUIUtil().onDrawOver(c, recyclerView, getForegroundView(viewHolder), dX, dY, actionState, isCurrentlyActive)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            getDefaultUIUtil().clearView(getForegroundView(viewHolder))
        }
        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                                 viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int,
                                 isCurrentlyActive: Boolean) {
            val dx: Float = if (dX >= 0) min(dX, viewHolder.itemView.width.toFloat() * 0.45f)
            else -min(abs(dX), viewHolder.itemView.width.toFloat() * 0.45f)
            getDefaultUIUtil().onDraw(c, recyclerView, getForegroundView(viewHolder), dx, dY, actionState, isCurrentlyActive)
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            getViewHolder(viewHolder).user ?: return
            if (direction == ItemTouchHelper.LEFT) callback.onSwipeLeft(getViewHolder(viewHolder))
            else if (direction == ItemTouchHelper.RIGHT) callback.onSwipeRight(getViewHolder(viewHolder))
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return 0.45f
        }

        private fun getForegroundView(holder: RecyclerView.ViewHolder): View {
            return (holder as VH).cardView
        }
        private fun getViewHolder(viewHolder: RecyclerView.ViewHolder): VH = viewHolder as VH

    }

}