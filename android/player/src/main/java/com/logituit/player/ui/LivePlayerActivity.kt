package com.logituit.player.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import coil.load
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.logituit.player.BuildConfig
import com.logituit.player.R

import com.logituit.player.common.isNetworkAvailable
import com.logituit.player.databinding.ActivityLivePlayerBinding
import com.logituit.player.databinding.ItemRailBinding
import com.logituit.player.databinding.ItemSubTitleBinding
import com.logituit.player.log.LogituitLog
import com.logituit.player.model.CatchUpData
import com.logituit.player.model.LiveNewsData
import com.logituit.player.ui.BaseActivity
import com.logituit.logixsdk.logixplayer.LogixPlayerSDK
import com.logituit.logixsdk.logixplayer.ads.Properties
import com.logituit.logixsdk.logixplayer.error.LogixPlayerError
import com.logituit.logixsdk.logixplayer.model.AdObstructionPurpose
import com.logituit.logixsdk.logixplayer.model.AdObstructionView
import com.logituit.logixsdk.logixplayer.model.AudioTrack
import com.logituit.logixsdk.logixplayer.model.LoadControlBuffers
import com.logituit.logixsdk.logixplayer.model.LogixTracks
import com.logituit.logixsdk.logixplayer.model.RequestParams
import com.logituit.logixsdk.logixplayer.model.ThumbnailDescription
import com.logituit.logixsdk.logixplayer.model.VideoTrack
import com.logituit.logixsdk.logixplayer.player.LogixMediaItem
import com.logituit.logixsdk.logixplayer.player.LogixPlayer
import com.logituit.logixsdk.logixplayer.player.LogixPlayerEventListener
import com.logituit.player.common.CustomDialog
import com.logituit.player.data.PlayerQuality
import com.logituit.player.ui.adapter.SimpleAdapter
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class LivePlayerActivity : BaseActivity(), LogixPlayerEventListener {

    private var logixPlayer: LogixPlayer? = null
    private lateinit var binding: ActivityLivePlayerBinding
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private val logixMediaItems: ArrayList<LogixMediaItem> = ArrayList()
    private lateinit var adapter: SimpleAdapter<LiveNewsData>
    private val liveNewsDataList = ArrayList<LiveNewsData>()
    private var languageList: List<AudioTrack>? = ArrayList()
    private var qualityList: ArrayList<PlayerQuality> = ArrayList()
    private lateinit var adapterLanguage: SimpleAdapter<AudioTrack>
    private lateinit var adapterQuality: SimpleAdapter<PlayerQuality>
    private var isSideMenuShowing = false
    private var selectedQltPos = 0
    private var selectedAudioPos = 0

    // Handler for hiding controls
    private val controlHideHandler = Handler()
    private val controlHideRunnable = Runnable {
        // Hide the player controls
        binding.layoutActions.liveController.visibility = View.GONE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLiveNewsDataFromUrl()
        createMediaList()
        val friendlyViews = arrayListOf<AdObstructionView>()
        binding.layoutActions.let {
            friendlyViews.add(
                AdObstructionView(
                    it.liveController,
                    AdObstructionPurpose.VIDEO_CONTROLS,
                    false
                )
            )
        }
        LogixPlayerSDK.clearFriendlyObstructionViews()
        LogixPlayerSDK.addFriendlyObstructionViews(friendlyViews)
        initPlayer()
        init()

        initClickListener()
        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }

    }

    private fun initPlayer() {
        logixPlayer = LogixPlayerSDK.createLogixPlayerBuilder(this).build()
        logixPlayer?.setLoadControlBuffers(
            LoadControlBuffers()
                .setAllowedVideoJoiningTimeMs(6000)
                .setBackBufferDurationMs(0)
                .setMaxPlayerBufferMs(6000)
                .setMinBufferAfterInteractionMs(2500)
                .setMinBufferAfterReBufferMs(6000)
                .setMinPlayerBufferMs(6000)
                .setRetainBackBufferFromKeyframe(false)
        )
        logixPlayer?.setPlayerListener(this)
        logixPlayer?.setAutoPlayNextItem(false)
        logixPlayer?.usePlayerController(false)
    }

    private fun prepareAndPlay(logixMediaItem: LogixMediaItem) {
        val params = HashMap<String, String>()
      //  params["applicationId"] = BuildConfig.APPLICATION_ID
      //  params["versionName"] = BuildConfig.VERSION_NAME
        val contentParams = RequestParams(logixMediaItem.sourceUrl!!, params)
        LogixPlayerSDK.userAgent = "LogixPlayerSDK"
        logixPlayer?.setContentRequestParams(contentParams)
        logixMediaItem.contentProperties = HashMap<Properties, String>().apply {
            put(Properties.CONTENT_ID, logixMediaItem.id)
            put(Properties.CONTENT_TITLE, logixMediaItem.title)
            put(Properties.CONTENT_PAGE_TYPE, "Player")
            put(Properties.CONTENT_PLAYER_INIT_TIME, "1")
        }

        binding.playerView.let {
            logixPlayer?.setLogixPlayerView(it)
        }
        logixPlayer?.prepare(logixMediaItem)
        logixPlayer?.play()
    }

    private fun releasePlayer() {
        isPlayerPlaying = logixPlayer?.isPlaying() == true
        playbackPosition = logixPlayer?.getCurrentPosition() ?: 0
        currentWindow = logixPlayer?.getCurrentMediaItemIndex() ?: 0
        logixPlayer?.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        logixPlayer?.let { outState.putInt(STATE_RESUME_WINDOW, it.getCurrentMediaItemIndex()) }
        logixPlayer?.let { outState.putLong(STATE_RESUME_POSITION, it.getCurrentPosition()) }
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            showProgressDialog()
            prepareAndPlay(logixMediaItems[0])
        }
    }

    override fun onResume() {
        super.onResume()
        val url = liveNewsDataList[0].poster1920x1080Url.toString()
        Glide.with(this).load(url).into(binding.ivBackground)
        resumeVideo()
        logixPlayer?.setAppInBackground(false)
    }

    override fun onPause() {
        super.onPause()
        controlHideHandler.removeCallbacks(controlHideRunnable)
        if (Util.SDK_INT <= 23) {
            pauseVideo()
            logixPlayer?.setAppInBackground(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            hideProgressDialog()
            pauseVideo()
        }
    }

    override fun onDestroy() {
        logixPlayer?.onDestroy()
        logixPlayer = null
        LogixPlayerSDK.release()
        super.onDestroy()
    }

    companion object {
        const val STATE_RESUME_WINDOW = "resumeWindow"
        const val STATE_RESUME_POSITION = "resumePosition"
        const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
        const val STATE_PLAYER_PLAYING = "playerOnPlay"
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            LogituitLog.errorLog(
                "VodPlayerActivity",
                "dispatchKeyEvent ::event.keyCode :" + event.keyCode
            )
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BUTTON_R2, MotionEvent.AXIS_RTRIGGER, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
//                    if (!isControllerVisible())
//                        forwardBackwardPlayer(PlayerConstants.SCRUBBING_RIGHT)
                }

                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_L2, MotionEvent.AXIS_LTRIGGER, KeyEvent.KEYCODE_MEDIA_REWIND -> {
//                    if (!isControllerVisible())
//                        forwardBackwardPlayer(PlayerConstants.SCRUBBING_LEFT)
                }

                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (!isSideMenuShowing && !isControllerVisible())
                        showController()
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (!isSideMenuShowing && !isControllerVisible())
//                        hideController()
//                    else
                        showController()
                }

                else -> {}
            }
        }
        if (event.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BUTTON_R2, MotionEvent.AXIS_RTRIGGER, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_L2, MotionEvent.AXIS_LTRIGGER, KeyEvent.KEYCODE_MEDIA_REWIND -> {
                }

                else -> {}
            }
        }

        return super.dispatchKeyEvent(event)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        LogituitLog.errorLog("VodPlayerActivity", "onKeyDown ::event.keyCode :" + event.keyCode)
        when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (!isSideMenuShowing) {
                    togglePlayPause()
                    showController()
                }
            }

            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                resumeVideo()
            }

            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                pauseVideo()
            }


            else -> {}
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onBackPressed() {
        if (isSideMenuShowing) {
            isSideMenuShowing = false
            closeSideMenu()
        } else {
            releasePlayer()
            finish()
            super.onBackPressed()
        }
    }

    private fun resumeVideo() {
        logixPlayer?.resume()
    }

    private fun pauseVideo() {
        logixPlayer?.pause()
        showController()
        startControlHideCountdown(5000)
    }

    private fun togglePlayPause() {
        if (logixPlayer?.isPlaying() == true) pauseVideo() else resumeVideo()

    }

    private fun initController(title: String) {
        title.let {
            binding.layoutActions.tvTitle.text = it
            binding.layoutActions.tvSubtitle.text = it
        }
    }

    private fun showErrorDialog() {
        CustomDialog(
            title = getString(R.string.error_message),
            message = getString(R.string.we_were_unable_to_process_your_request_please_try_again),
            positiveButtonText = getString(R.string.try_again),
            negativeButtonText = getString(R.string.exit_btn),
            cancellable = false,
            mListener = object : CustomDialog.ClickListener {
                override fun onSuccess() {
                    showProgressDialog()
                    retry()
                }

                override fun onCancel() {
                    finish()
                }

            }).show(supportFragmentManager, "tag")
        true

    }

    private fun showProgressDialog() {
        val url = liveNewsDataList[0].poster1920x1080Url.toString()
        Glide.with(this).load(url).into(binding.ivBackground)
        binding.progressBar.visibility = View.VISIBLE
        binding.ivBackground.visibility = View.VISIBLE
        binding.flBackground.visibility = View.VISIBLE

    }

    private fun hideProgressDialog() {
        binding.progressBar.visibility = View.GONE
        binding.ivBackground.visibility = View.GONE
        binding.flBackground.visibility = View.GONE
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        hideProgressDialog()
        if (!isConnected) {

            CustomDialog(
                title = getString(R.string.network),
                message = getString(R.string.network_description),
                positiveButtonText = getString(R.string.try_again),
                negativeButtonText = getString(R.string.exit_btn),
                cancellable = true,
                mListener = object : CustomDialog.ClickListener {
                    override fun onSuccess() {
                        showProgressDialog()
                        retry()
                    }

                    override fun onCancel() {
                        finishAffinity()
                    }

                }).show(supportFragmentManager, "tag")
            true
        } else {
            if (Util.SDK_INT <= 23) {
                prepareAndPlay(logixMediaItems[0])
                binding.playerView.onResume()
            }
        }
    }

    private fun retry() {
        binding.playerView.hideController()
        isPlayerPlaying = false
        releasePlayer()
        prepareAndPlay(logixMediaItems[0])
    }

    private fun isControllerVisible(): Boolean {
        return binding.layoutActions.liveController.visibility == View.VISIBLE
    }

    private fun showController() {
        binding.ivPlayerLogo.visibility = View.GONE
        binding.layoutActions.liveController.visibility = View.VISIBLE
        showPlayerSettings()
        Handler().postDelayed({ binding.layoutActions.playPause.requestFocus() }, 100)
        startControlHideCountdown(8000)
    }

    private fun showControllerTimeout(timeInMillis: Long) {
        binding.layoutActions.liveController.visibility = View.VISIBLE
        startControlHideCountdown(timeInMillis)
    }

    private fun hideController() {
        if(!isSideMenuShowing)
            binding.ivPlayerLogo.visibility = View.VISIBLE

        binding.layoutActions.liveController.visibility = View.GONE
    }

    private fun startControlHideCountdown(timeInMillis: Long) {
        // Cancel any previous runnables
        controlHideHandler.removeCallbacks(controlHideRunnable)

        // Schedule hiding of controls after the desired duration (3 seconds)
        // if (logixPlayer?.isPlaying() == false)
        controlHideHandler.postDelayed(controlHideRunnable, timeInMillis)
    }

    override fun onKeyEvents(event: KeyEvent) {
    }

    override fun onMediaItemChange(logixMediaItem: LogixMediaItem, position: Int) {
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            LogixPlayer.PlaybackState.STATE_IDLE -> {}
            LogixPlayer.PlaybackState.STATE_READY -> {
                hideProgressDialog()
            }

            LogixPlayer.PlaybackState.STATE_BUFFERING -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            LogixPlayer.PlaybackState.STATE_ENDED -> {}
        }
    }

    override fun onPlayerError(playerError: LogixPlayerError) {
        if (playerError.errorCode == LogixPlayerError.Code.BEHIND_LIVE_WINDOW) {
            logixPlayer?.seekToLiveEdge()
            prepareAndPlay(logixMediaItems[0])
        } else {
            if (playerError.errorCode == LogixPlayerError.Code.TIMEOUT_ERROR_CODE ||
                !isNetworkAvailable(this@LivePlayerActivity)
            ) {
                showNetworkMessage(false)
            } else {
                hideProgressDialog()
                showErrorDialog()
            }
        }

    }

    override fun onPlaylistAltered(action: LogixPlayer.PlaylistAction) {
    }

    override fun onProgressUpdate(progress: Long) {
    }

    override fun onRenderedFirstFrame() {
        binding.ivPlayerLogo.visibility = View.VISIBLE
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
    }

    override fun onTracksChanged(logixTracks: LogixTracks) {
    }

    override fun thumbnailInfoCompleted(map: HashMap<Uri, ThumbnailDescription>) {
    }

    override fun updateCurrentProgramTime(currentProgramTime: Long) {
    }

    private fun createMediaList() {
        liveNewsDataList[0]?.title?.let { initController(it) }
        liveNewsDataList[0]?.title?.let {
            LogixMediaItem(
                id = "1001",
                title = it,
                sourceUrl = "http://content.uplynk.com/channel/e4d136bb61b1453a8817dc39c6e90201.m3u8",
                drmScheme = LogixMediaItem.ContentType.CLEAR,
                streamType = LogixMediaItem.StreamType.LIVE
            )
        }?.let {
            logixMediaItems.add(
                it
            )
        }
    }

    private fun initClickListener() {
        binding.layoutActions.apply {
            setButtonClickListener(btnGoLive)
            setButtonClickListener(btnLanguage)
            setButtonClickListener(btnQuality)
            setButtonClickListener(playPause)
            setButtonClickListener(btnKeyMoment)
        }
    }

    private fun init() {
        binding.layoutActions.apply {
            setButtonFocusChangeListener(btnGoLive)
            setButtonFocusChangeListener(btnLanguage)
            setButtonFocusChangeListener(btnQuality)
            setButtonFocusChangeListener(btnKeyMoment)

            playPause.setOnFocusChangeListener { _, hasFocus ->
                val isPlaying = logixPlayer?.isPlaying() == true
                playPause.setBackgroundResource(
                    if (hasFocus) {
                        if (isPlaying) R.drawable.ic_controls_pause_focused else R.drawable.ic_controls_play_focused
                    } else {
                        if (isPlaying) R.drawable.ic_controls_pause_default else R.drawable.ic_controls_play_default
                    }
                )

            }
        }
    }

    private fun setButtonFocusChangeListener(button: Button) {
        button.setOnFocusChangeListener { view, hasFocus ->
            val context = view.context
            val textColorResId = if (hasFocus) R.color.blue_black else R.color.logituit_white_70
            val backgroundResId =
                if (hasFocus) R.drawable.bg_btn_selected else R.drawable.bg_edit_button
            var iconResId: Int = 0
            when (view.id) {
                R.id.btn_go_live -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_go_live_dark
                    else
                        R.drawable.ic_go_live
                }

                R.id.btn_language -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_language_icon_dark
                    else
                        R.drawable.ic_language_icon
                }

                R.id.btn_quality -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_settings_dark
                    else
                        R.drawable.ic_settings
                }

                R.id.btn_key_moment -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_key_moment_dark
                    else
                        R.drawable.ic_key_moment
                }
            }
            val iconDrawable = ContextCompat.getDrawable(context, iconResId)
            button.setTextColor(ContextCompat.getColor(context, textColorResId))
            button.setBackgroundResource(backgroundResId)
            button.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null)
        }
    }

    private fun setButtonClickListener(button: Button) {
        button.setOnClickListener { view ->
            when (view.id) {
                R.id.btn_go_live -> {
                }

                R.id.btn_language -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    showAudioTrackPopUp()
                }

                R.id.btn_quality -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    showQualityChangePopUp()
                }

                R.id.btn_key_moment -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    var firstView:View? = null
                    adapter = SimpleAdapter.with<LiveNewsData, ItemRailBinding>(R.layout.item_rail) { pos, model, binding ->
                        if(pos == 0) {
                            firstView = binding.cardviewImage
                            binding.railDesc.setTextColor(
                                ContextCompat.getColor(
                                    view.context,
                                    R.color.logituit_white
                                )
                            )
                            binding.cardviewImage.marginBottom
                            binding.cardviewImage.elevation=4F
                            binding.cardviewImage.strokeWidth=4
                            binding.cardviewImage.outlineSpotShadowColor= ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_56
                            )
                            binding.cardviewImage.strokeColor=  ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white
                            )
                        }
                        binding.railSubtitle.visibility=View.GONE
                        binding.railDesc.text = model.title
                        //binding.railDuration.text = getFormattedDuration(model.du).trim()
                        binding.railImage.load(model.thumbnailUrls[0].img1)
                        firstView?.requestFocus()
                        binding.cardviewImage.setOnFocusChangeListener { view, hasFocus ->
                            if (hasFocus) {
                                binding.railDesc.setTextColor(
                                    ContextCompat.getColor(
                                        view.context,
                                        R.color.logituit_white
                                    )
                                )
                                binding.cardviewImage.marginBottom
                                // railCardOpacity.visibility=View.GONE
                                binding.cardviewImage.elevation=4F
                                binding.cardviewImage.strokeWidth=4
                                binding.cardviewImage.outlineSpotShadowColor= ContextCompat.getColor(
                                    view.context,
                                    R.color.logituit_white_56
                                )
                                binding.cardviewImage.strokeColor=  ContextCompat.getColor(
                                    view.context,
                                    R.color.logituit_white
                                )
                            } else {
                                binding.railDesc.setTextColor(
                                    ContextCompat.getColor(
                                        view.context,
                                        R.color.logituit_white_70
                                    )
                                )
                                //railCardOpacity.visibility=View.VISIBLE
                                binding.cardviewImage.elevation=0F
                                binding.cardviewImage.strokeWidth=1
                                binding.cardviewImage.strokeColor=  ContextCompat.getColor(
                                    view.context,
                                    R.color.logituit_white_20
                                )

                            }
                        }
                    }

                    adapter.addAll(liveNewsDataList)
                    binding.rvKeyMoment.adapter = adapter

                    val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
                    binding.blfKeyMoment.startAnimation(animSlide)
                    binding.blfKeyMoment.visibility = View.VISIBLE
                    binding.blfKeyMoment.requestLayout()
                    isSideMenuShowing  = true
                    hideController()
                }

                R.id.playPause -> {
                    binding.layoutActions.playPause.setBackgroundResource(if (logixPlayer?.isPlaying() == true) R.drawable.ic_controls_play_focused else R.drawable.ic_controls_pause_focused)
                    togglePlayPause()
                }
            }
        }
    }

    private fun showMsg(msg: String) {
        Toast.makeText(this@LivePlayerActivity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun getLiveNewsDataFromUrl(): ArrayList<LiveNewsData> {
        val objectArrayString: String = resources.openRawResource(R.raw.cards).bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(objectArrayString)
        liveNewsDataList.clear()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val liveNewsItem =
                Gson().fromJson(jsonObject.toString(), LiveNewsData::class.java)
            liveNewsDataList.add(liveNewsItem)
        }
        return liveNewsDataList
    }



    private fun showAudioTrackPopUp() {
        languageList = logixPlayer?.getAudioTracks()

        var selectedButton: TextView? = null
        adapterLanguage =
            SimpleAdapter.with<AudioTrack, ItemSubTitleBinding>(R.layout.item_sub_title) { pos, audioTrack, binding ->
                binding.btnSubtitle.text = audioTrack.label
                if (selectedAudioPos == pos)
                    selectedButton = binding.btnSubtitle
                binding.btnSubtitle.setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.blue_black
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_btn_selected)
                        if (selectedAudioPos == pos) {
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(
                                ContextCompat.getColor(
                                    this@LivePlayerActivity,
                                    R.color.blue_black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }else{
                            binding.ivCheck.visibility = View.INVISIBLE
                        }   } else {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_edit_button)
                        if (selectedAudioPos == pos){
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@LivePlayerActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                        }else {
                            binding.ivCheck.visibility = View.INVISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@LivePlayerActivity, R.color.blue_black), android.graphics.PorterDuff.Mode.SRC_IN)

                        }

                    }
                }
                selectedButton?.requestFocus()
            }
        languageList?.let { adapterLanguage.addAll(it) }
        binding.rvLanguage.adapter = adapterLanguage
        adapterLanguage.setClickableViews({ _, audioTrack, pos ->
            selectedAudioPos = pos
            logixPlayer?.setAudioTrack(audioTrack)
            isSideMenuShowing = false
            binding.layoutActions.btnLanguage.text="Language: ${audioTrack.label}"
            closeSideMenu()
        }, R.id.btn_subtitle)
        val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.blfLanguage.startAnimation(animSlide)
        binding.blfLanguage.visibility = View.VISIBLE
        binding.blfLanguage.requestLayout()
        isSideMenuShowing = true
        hideController()
    }

    private fun showQualityChangePopUp() {
        qualityList.clear()
        val playerQuality = PlayerQuality("Auto",null)
        qualityList.add(playerQuality)
        if (logixPlayer?.getVideoTracks() != null) {
            for (qualities in logixPlayer?.getVideoTracks()!!) {
                PlayerQuality(qualities.height.toString()+"p", qualities).let { qualityList.add(it) }
            }
        }

        var selectedButton: TextView? = null
        adapterQuality =
            SimpleAdapter.with<PlayerQuality, ItemSubTitleBinding>(R.layout.item_sub_title) { pos, videoTrack, binding ->
                binding.btnSubtitle.text = videoTrack.name
                if (selectedQltPos == pos)
                    selectedButton = binding.btnSubtitle
                binding.btnSubtitle.setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.blue_black
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_btn_selected)
                        if (selectedQltPos == pos) {
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(
                                ContextCompat.getColor(
                                    this@LivePlayerActivity,
                                    R.color.blue_black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }else{
                            binding.ivCheck.visibility = View.INVISIBLE
                        }   } else {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_edit_button)
                        if (selectedQltPos == pos){
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@LivePlayerActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                        }else {
                            binding.ivCheck.visibility = View.INVISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@LivePlayerActivity, R.color.blue_black), android.graphics.PorterDuff.Mode.SRC_IN)

                        }

                    }
                }
                selectedButton?.requestFocus()
            }

        qualityList.let { adapterQuality.addAll(it) }
        binding.rvQuality.adapter = adapterQuality
        adapterQuality.setClickableViews({ _, videoTrack, pos ->
            selectedQltPos = pos
            if (selectedQltPos == 0)
                logixPlayer?.setABRForVideo()
            else
                videoTrack.track?.let { logixPlayer?.setVideoTrack(it) }
            isSideMenuShowing = false
            binding.layoutActions.btnQuality.text="Quality: ${videoTrack.name}"
            closeSideMenu()
        }, R.id.btn_subtitle)

        val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.blfQuality.startAnimation(animSlide)
        binding.blfQuality.visibility = View.VISIBLE
        binding.blfQuality.requestLayout()
        isSideMenuShowing = true
        hideController()
    }

    private fun closeSideMenu() {
        binding.flBackgroundOverlay.visibility = View.GONE
        binding.blfLanguage.visibility = View.GONE
        binding.blfQuality.visibility = View.GONE
        binding.blfKeyMoment.visibility=View.GONE
        isSideMenuShowing = false
    }
    private fun showPlayerSettings() {
        if (logixPlayer?.getAudioTracks()?.size != null && logixPlayer?.getAudioTracks()?.size!! > 1)
            binding.layoutActions.btnLanguage.visibility = View.VISIBLE
        if (logixPlayer?.getVideoTracks()?.size != null && logixPlayer?.getVideoTracks()?.size!! > 1)
            binding.layoutActions.btnQuality.visibility = View.VISIBLE
    }
}

