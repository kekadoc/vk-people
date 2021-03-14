package com.kekadoc.projects.vkpeople.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.kekadoc.projects.vkpeople.*
import com.kekadoc.projects.vkpeople.data.VKUser
import com.kekadoc.tools.android.AndroidUtils
import com.kekadoc.tools.android.fragment.string
import com.kekadoc.tools.android.view.dpToPx
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class FragmentUserPreview : Fragment() {

    companion object {
        private const val TAG: String = "FragmentUserPreview-TAG"
    }

    private val activityViewModel: ViewModelActivity by activityViewModels()

    private lateinit var userPreview: UserPreview
    
    private lateinit var userView: View
    private lateinit var shutterView: View
    private lateinit var indicator: LinearProgressIndicator

    private fun action() {
        showShutter(true)
        activityViewModel.requestRandomUser()
    }
    private fun longAction() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input_id, null, false)
        val editText = view.findViewById<EditText>(R.id.editText_inputId)
        MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setTitle("Введите ID пользователя:")
                .setIcon(R.drawable.ic_input_id)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val txt = editText.text.toString()
                    val id = txt.toIntOrNull()
                    if (id != null) {
                        showShutter(true)
                        activityViewModel.requestUser(id)
                    } else {
                        val view = requireView().findViewById<ExtendedFloatingActionButton>(R.id.button_action)
                        Snackbar.make(view, "Некорректный ID!", Snackbar.LENGTH_SHORT).setAnchorView(view).show()
                    }
                }
                .show()
    }

    private fun showShutter(indicator: Boolean) {
        userView.visibility = View.INVISIBLE
        shutterView.visibility = View.VISIBLE
        if (indicator) this.indicator.show()
        else this.indicator.hide()
    }
    private fun showContent() {
        userView.visibility = View.VISIBLE
        shutterView.visibility = View.INVISIBLE
        this.indicator.hide()
    }
    
    private fun updateUI(user: VKUser?) {
        userPreview.user = user
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_service_random_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userView = view.findViewById(R.id.userView)
        shutterView = view.findViewById(R.id.linearLayout_shutter)
        indicator = view.findViewById(R.id.linearProgressIndicator)

        userPreview = UserPreview(view)

        view.findViewById<MaterialButton>(R.id.button_action).apply {
            setOnClickListener {
                action()
            }
            setOnLongClickListener {
                longAction()
                return@setOnLongClickListener true
            }
            activityViewModel.currentUser.observe(viewLifecycleOwner) {
                isEnabled = it != null
            }
        }

        activityViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) showShutter(false)
        }
        activityViewModel.showingUser.observe(viewLifecycleOwner) {
            updateUI(it)
        }

    }
    
    private inner class UserPreview(view: View) {

        private val cardView: CardView = view.findViewById<MaterialCardView>(R.id.cardView).apply {
            setOnClickListener {
                activityViewModel.showingUser.value?.let {
                    requireActivity().startVKUserProfile(it.id)
                }
            }
        }
        private val textViewLastSeen: TextView = view.findViewById<TextView>(R.id.textView_lastSeen).apply {
            compoundDrawablePadding = dpToPx(4f).toInt()
        }
        private val imageViewPhoto: ImageView = view.findViewById(R.id.imageView_photo)

        private val textViewDomain: TextView = view.findViewById(R.id.textView_domain)
        private val textViewName: TextView = view.findViewById(R.id.textView_name)
        private val textViewStatus: TextView = view.findViewById(R.id.textView_status)
        private val textViewBDate: TextView = view.findViewById<TextView>(R.id.textView_bdate).apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_birth_day, 0, 0, 0)
            compoundDrawablePadding = dpToPx(8f).toInt()
        }
        private val textViewCity: TextView = view.findViewById(R.id.textView_city)
        private val textViewFamily: TextView = view.findViewById<TextView>(R.id.textView_family).apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_relation, 0, 0, 0)
            compoundDrawablePadding = dpToPx(8f).toInt()
        }

        private val chipVideos: Chip = view.findViewById(R.id.chip_video)
        private val chipPhotos: Chip = view.findViewById(R.id.chip_photos)
        private val chipAudios: Chip = view.findViewById(R.id.chip_audios)
        private val chipGifts: Chip = view.findViewById(R.id.chip_gifts)
        private val chipGroups: Chip = view.findViewById(R.id.chip_groups)
        private val chipFriends: Chip = view.findViewById(R.id.chip_friends)
        private val chipFollowers: Chip = view.findViewById(R.id.chip_followers)

        private val imageViewStateTrending: ImageView = view.findViewById(R.id.imageView_stateTrending)
        private val imageViewStateFriend: ImageView = view.findViewById(R.id.imageView_stateFriend)
        private val imageViewStateFavorite: ImageView = view.findViewById(R.id.imageView_stateFavorite)
        private val imageViewStateClosed: ImageView = view.findViewById(R.id.imageView_stateClosed)
        private val imageViewStateHidden: ImageView = view.findViewById(R.id.imageView_stateHidden)
        private val imageViewStateBlacklisted: ImageView = view.findViewById(R.id.imageView_stateBlacklisted)
        private val imageViewStateBlacklistedByMe: ImageView = view.findViewById(R.id.imageView_stateBlacklistedByMe)


        var user: VKUser? = null
            set(value) {
                field = value
                if (value == null) onUserNull()
                else onUserAttach(value)
            }

        var userLoadingProcess: Job? = null

        init {
            user = null
        }

        @WorkerThread
        private suspend fun handleUserOnline(textView: TextView, user: VKUser?) {
            if (user == null) textView.text = null
            else {
                val online = user.online
                if (online) {
                    val icon = if (user.online_mobile) R.drawable.ic_online_phone else R.drawable.ic_online_other
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                    textView.setText(R.string.online)
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val text = getLastSeen(user)
                        withContext(Dispatchers.Main) {
                            textView.text = text
                            val iconRes = when (user.last_seen.platform) {
                                1, 2, 3, 4, 5 -> R.drawable.ic_online_phone
                                else -> 0
                            }
                            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconRes, 0)
                        }
                    }
                }
            }
        }
        @WorkerThread
        private fun getLastSeen(user: VKUser): String? {
            val parsed = VKUsersParser.parseLastSeen(user)
            if (parsed == null || parsed.isEmpty()) {
                val lastSeen = user.last_seen
                lastSeen.let {
                    val time = it.time
                    val date = Date(time * 1000L)
                    val genderPrefixId = when (user.sex) {
                        1 -> R.string.last_seen_female
                        2 -> R.string.last_seen_male
                        else -> R.string.last_seen_other
                    }
                    val format = SimpleDateFormat("${string(genderPrefixId)} d MMMM yyyy в HH:mm", AndroidUtils.getLocale())
                    return format.format(date)
                }
            }
            return parsed
        }

        @MainThread
        private fun onUserNull() {
            showShutter(false)

            imageViewPhoto.setImageDrawable(null)
            /*imageViewIcFriend.visibility = View.INVISIBLE
            imageViewIcFavorite.visibility = View.INVISIBLE
            imageViewIcBlacklisted.visibility = View.INVISIBLE
            imageViewIcBlacklistedByMe.visibility = View.INVISIBLE
            imageViewIcHided.visibility = View.INVISIBLE*/
            textViewDomain.text = null
            textViewStatus.text = null
            textViewBDate.text = null
            textViewCity.text = null
            textViewFamily.text = null

        }
        @MainThread
        private fun onUserAttach(user: VKUser) {

            if (userLoadingProcess != null) userLoadingProcess!!.cancel()

            userLoadingProcess = lifecycleScope.launch(Dispatchers.IO) {
                val image = if (user.photo_400_orig == "") user.photo_max_orig else user.photo_400_orig

                val imageLoading = async(Dispatchers.IO) {
                    val request = ImageRequest.Builder(requireContext())
                            .data(image)
                            .target(imageViewPhoto)
                            .build()
                    requireContext().imageLoader.execute(request)
                }
                val lastSeenLoading = async(Dispatchers.IO) {
                    handleUserOnline(textViewLastSeen, user)
                }

                awaitAll(imageLoading, lastSeenLoading)
                withContext(Dispatchers.Main) {

                    textViewDomain.text = user.getScreenName()
                    textViewName.apply {
                        text = user.getFullName()
                        val genderRes = when (user.sex) {
                            1 -> R.drawable.ic_user_sex_female
                            2 -> R.drawable.ic_user_sex_male
                            else -> R.drawable.ic_user_sex_transgender
                        }
                        val verified = user.verified
                        compoundDrawablePadding = dpToPx(8f).toInt()
                        setCompoundDrawablesWithIntrinsicBounds(genderRes, 0, if (verified) R.drawable.ic_user_state_verified else 0, 0)
                    }
                    textViewStatus.apply {
                        text = user.status
                        visibility = if (text == null || text.isEmpty()) View.GONE
                        else View.VISIBLE
                    }
                    textViewBDate.apply {
                        text = user.bdate
                        visibility = if (text == null || text.isEmpty()) View.GONE
                        else View.VISIBLE
                    }
                    textViewCity.apply {
                        val country = user.country?.title
                        val city = user.city?.title
                        val txt = StringBuilder()
                        if (country != null && country.isNotEmpty()) txt.append(country)
                        if (city != null && city.isNotEmpty()) {
                            if (txt.isNotEmpty()) txt.append(", ")
                            txt.append(city)
                        }
                        text = txt
                        visibility = if (text == null || text.isEmpty()) View.GONE
                        else View.VISIBLE
                    }
                    textViewFamily.apply {
                        val relation = VKUser.getRelation(user)
                        text = relation
                        visibility = if (relation == null) View.GONE
                        else View.VISIBLE
                    }

                    chipVideos.text = user.counters.videos.toString()
                    chipPhotos.text = user.counters.photos.toString()
                    chipAudios.text = user.counters.audios.toString()
                    chipGifts.apply {
                        text = user.giftsCount.toString()
                        visibility = if (text == null || text.isEmpty() || text == "null") View.GONE else View.VISIBLE
                    }
                    chipGroups.text = user.counters.groups.toString()
                    chipFriends.text = user.counters.friends.toString()
                    chipFollowers.text = user.counters.followers.toString()

                    imageViewStateTrending.visibility = if (user.trending) View.VISIBLE else View.GONE
                    imageViewStateFriend.visibility = if (user.is_friend) View.VISIBLE else View.GONE
                    imageViewStateFavorite.visibility = if (user.is_favorite) View.VISIBLE else View.GONE
                    imageViewStateClosed.visibility = if (user.is_closed && !user.can_access_closed) View.VISIBLE else View.GONE
                    imageViewStateHidden.visibility = if (user.is_hidden_from_feed) View.VISIBLE else View.GONE
                    imageViewStateBlacklisted.visibility = if (user.blacklisted) View.VISIBLE else View.GONE
                    imageViewStateBlacklistedByMe.visibility = if (user.blacklisted_by_me) View.VISIBLE else View.GONE

                    showContent()
                    userLoadingProcess = null
                }
            }

        }

    }

}