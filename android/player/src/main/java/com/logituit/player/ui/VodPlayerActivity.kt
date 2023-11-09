package com.logituit.player.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import coil.load
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
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
import com.logituit.logixsdk.logixplayer.player.LogixMediaItem
import com.logituit.logixsdk.logixplayer.player.LogixPlayer
import com.logituit.logixsdk.logixplayer.player.LogixPlayerEventListener
import com.logituit.player.BuildConfig
import com.logituit.player.R
import com.logituit.player.common.CustomDialog
import com.logituit.player.common.isNetworkAvailable
import com.logituit.player.data.PlayerQuality
import com.logituit.player.data.PlayerSubtitle
import com.logituit.player.databinding.ActivityVodPlayerBinding
import com.logituit.player.databinding.ItemRailBinding
import com.logituit.player.databinding.ItemSubTitleBinding
import com.logituit.player.databinding.LayoutMyListHomeRailItemBinding
import com.logituit.player.log.LogituitLog
import com.logituit.player.model.CatchUpData
import com.logituit.player.model.CatchUpRailModel
import com.logituit.player.ui.adapter.SimpleAdapter
import com.logituit.player.ui.util.PlayerConstants.DEFAULT_SEEK_TO
import com.logituit.player.ui.util.PlayerConstants.SCRUBBING_LEFT
import com.logituit.player.ui.util.PlayerConstants.SCRUBBING_RIGHT
import org.json.JSONArray
import java.util.concurrent.TimeUnit


private var catchUpEndCallback: VodPlayerActivity.CatchUpEndCallback? = null


class VodPlayerActivity : BaseActivity(), LogixPlayerEventListener {

    private var logixPlayer: LogixPlayer? = null
    private lateinit var binding: ActivityVodPlayerBinding
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private val logixMediaItems: ArrayList<LogixMediaItem> = ArrayList()
    private var subTitleList: ArrayList<PlayerSubtitle> = ArrayList()
    private var languageList: List<AudioTrack>? = ArrayList()
    private var qualityList: ArrayList<PlayerQuality> = ArrayList()
    private val updateDurationHandler = Handler()
    private  var catchUpList = ArrayList<CatchUpData>()
    private  var catchUpRalList = ArrayList<CatchUpRailModel>()
    private lateinit var seekBar: AppCompatSeekBar
    private var isPlayingStarted = false
    private var isSideMenuShowing = false
    private lateinit var recomendedAdapter: SimpleAdapter<CatchUpData>
    private lateinit var episodesAdapter: SimpleAdapter<CatchUpData>
    private lateinit var adapterSubtitle: SimpleAdapter<PlayerSubtitle>
    private lateinit var adapterLanguage: SimpleAdapter<AudioTrack>
    private lateinit var adapterQuality: SimpleAdapter<PlayerQuality>
    private var selectedQltPos = 0
    private var selectedAudioPos = 0
    private var selectedSubtitlePos = 0
    private var rateUsCheck = false


    // Handler for hiding controls
    private val controlHideHandler = Handler()
    private val controlHideRunnable = Runnable {
        // Hide the player controls
        hideController()
    }


    interface CatchUpEndCallback {
        fun allCatchUpPlayed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVodPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadCatchUpData()
        initController(getCustomData())
        //setCatchUpList()
        createMediaList()
        /* binding.layoutActions.playPause.setOnKeyListener { _, _, event ->
             if (event.action == KeyEvent.ACTION_DOWN) {
                 when (event.keyCode) {
                     KeyEvent.KEYCODE_DPAD_DOWN -> {
                         hideController()
                         recommendedData()
                         binding.rvRecommended.visibility = View.VISIBLE
                     }
                 }
                 return@setOnKeyListener true
             }else{
                 return@setOnKeyListener true
             }

         }*/

        val friendlyViews = arrayListOf<AdObstructionView>()
        binding.layoutActions.let {
            friendlyViews.add(
                AdObstructionView(
                    it.vodPlayerController,
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
        recommendedData()
        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
    }


    private fun episodeData() {
        var firstView:View? = null
        episodesAdapter =
            SimpleAdapter.with<CatchUpData, ItemRailBinding>(R.layout.item_rail) { pos, model, binding ->
                if(pos == 0) {
                    firstView = binding.cardviewImage
                    binding.railDesc.setTextColor(
                        ContextCompat.getColor(
                            this@VodPlayerActivity,
                            R.color.logituit_white
                        )
                    )
                    binding.cardviewImage.marginBottom
                    // railCardOpacity.visibility=View.GONE
                    binding.cardviewImage.elevation=4F
                    binding.cardviewImage.strokeWidth=4
                    binding.cardviewImage.outlineSpotShadowColor= ContextCompat.getColor(
                        this@VodPlayerActivity,
                        R.color.logituit_white_56
                    )
                    binding.cardviewImage.strokeColor=  ContextCompat.getColor(
                        this@VodPlayerActivity,
                        R.color.logituit_white
                    )
                }
                binding.railDesc.text = model.name
                binding.railSubtitle.text = model.name
                binding.railDuration.text = getFormattedDuration(model.duration).trim()
                binding.railImage.load(model.thumbnail_url)
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
        episodesAdapter.setClickableViews({ _, subtitleTrack, pos ->

            val position = catchUpList.indexOf(getCustomData())
            if (position < catchUpList.size - 1) {
                val updateCustomData = catchUpList[pos]
                startActivity(
                    Intent(
                        this@VodPlayerActivity,
                        VodPlayerActivity::class.java
                    ).apply {
                        putExtra("custom_data", updateCustomData)
                        putParcelableArrayListExtra("list_data", catchUpList)
                    })
            } else {
                allCatchUpPlayed()
            }
            finish()

            isSideMenuShowing = false
            closeSideMenu()
        }, R.id.cardview_image)
        episodesAdapter.addAll(catchUpList)
        binding.rvEpisode.adapter = episodesAdapter
        isSideMenuShowing = true
        hideController()
    }
    private fun recommendedData() {
        var firstView:View? = null
        recomendedAdapter =
            SimpleAdapter.with<CatchUpData, LayoutMyListHomeRailItemBinding>(R.layout.layout_my_list_home_rail_item) { pos, model, binding ->
                if(pos == 0)
                    firstView = binding.cardviewImage
                binding.railDesc.text = model.name
                binding.railDuration.text = getFormattedDuration(model.duration).trim()
                binding.railImage.load(model.thumbnail_url)
                firstView?.requestFocus()

                binding.cardviewImage.setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        binding.railDesc.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
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
                                R.color.logituit_white_40
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

        recomendedAdapter.addAll(catchUpList)
        binding.rvRecommended.adapter = recomendedAdapter
    }
    private fun showSubtitlePopUp() {
        subTitleList.clear()
        val playerSubtitle = PlayerSubtitle("Off",null)
        subTitleList.add(playerSubtitle)
        if (logixPlayer?.getSubtitles() != null) {
            for (subtitles in logixPlayer?.getSubtitles()!!) {
                subtitles.label?.let { PlayerSubtitle(it, subtitles) }?.let { subTitleList.add(it) }
            }
        }
        hideController()
        var selectedButton: TextView? = null
        adapterSubtitle =
            SimpleAdapter.with<PlayerSubtitle, ItemSubTitleBinding>(R.layout.item_sub_title) { pos, subtitleTrack, binding ->
                binding.btnSubtitle.text = subtitleTrack.name
                if (selectedSubtitlePos == pos)
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
                        if (selectedSubtitlePos == pos) {
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(
                                ContextCompat.getColor(
                                    this@VodPlayerActivity,
                                    R.color.blue_black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }else{
                            binding.ivCheck.visibility = View.INVISIBLE
                        }
                    } else {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_edit_button)
                        if (selectedSubtitlePos == pos){
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                        }else {
                            binding.ivCheck.visibility = View.INVISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.blue_black), android.graphics.PorterDuff.Mode.SRC_IN)
                        }

                    }
                }
                selectedButton?.requestFocus()
            }

        subTitleList.let { adapterSubtitle.addAll(it) }
        binding.rvSubTitle.adapter = adapterSubtitle
        adapterSubtitle.setClickableViews({ _, subtitleTrack, pos ->
            selectedSubtitlePos = pos
            if (selectedSubtitlePos == 0)
                logixPlayer?.disableSubtitles()
            else
                subtitleTrack.track?.let { logixPlayer?.setSubtitleTrack(it) }
            isSideMenuShowing = false
            closeSideMenu()
            binding.layoutActions.btnSubtitle.text="Subtitle: ${subtitleTrack.name}"
        }, R.id.btn_subtitle)

        val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.blfSubTitle.startAnimation(animSlide)
        binding.blfSubTitle.visibility = View.VISIBLE
        binding.blfSubTitle.requestLayout()
        isSideMenuShowing = true
    }

    private fun showAudioTrackPopUp() {
        languageList = logixPlayer?.getAudioTracks()
        hideController()
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
                                    this@VodPlayerActivity,
                                    R.color.blue_black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }else{
                            binding.ivCheck.visibility = View.INVISIBLE
                        }
                    } else {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_edit_button)
                        if (selectedAudioPos == pos){
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                        }else {
                            binding.ivCheck.visibility = View.INVISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.blue_black), android.graphics.PorterDuff.Mode.SRC_IN)

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
    }
    private fun showAudioTrackPopUpBottom() {
        languageList = logixPlayer?.getAudioTracks()
        //hideController()
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
                                    this@VodPlayerActivity,
                                    R.color.blue_black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }else{
                            binding.ivCheck.visibility = View.INVISIBLE
                        }
                    } else {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_edit_button)
                        if (selectedAudioPos == pos){
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                        }else {
                            binding.ivCheck.visibility = View.INVISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.blue_black), android.graphics.PorterDuff.Mode.SRC_IN)

                        }

                    }
                }
                selectedButton?.requestFocus()
            }
        languageList?.let { adapterLanguage.addAll(it) }
        binding.inRateUs.rvLanguage.adapter = adapterLanguage
        adapterLanguage.setClickableViews({ _, audioTrack, pos ->
            selectedAudioPos = pos
            logixPlayer?.setAudioTrack(audioTrack)
            // isSideMenuShowing = false
            // closeSideMenu()
        }, R.id.btn_subtitle)
        // val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        //binding.inRateUs.blfLanguage.startAnimation(animSlide)
        binding.inRateUs.blfLanguage.visibility = View.VISIBLE
        binding.inRateUs.blfLanguage.requestLayout()
        // isSideMenuShowing = true
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
        hideController()
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
                                    this@VodPlayerActivity,
                                    R.color.blue_black
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }else{
                            binding.ivCheck.visibility = View.INVISIBLE
                        } } else {
                        binding.btnSubtitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.logituit_white_70
                            )
                        )
                        binding.clTitle.setBackgroundResource(R.drawable.bg_edit_button)
                        if (selectedQltPos == pos){
                            binding.ivCheck.visibility = View.VISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                        }else {
                            binding.ivCheck.visibility = View.INVISIBLE
                            binding.ivCheck.setColorFilter(ContextCompat.getColor(this@VodPlayerActivity, R.color.blue_black), android.graphics.PorterDuff.Mode.SRC_IN)

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
            closeSideMenu()
            binding.layoutActions.btnQuality.text="Quality: ${videoTrack.name}"
        }, R.id.btn_subtitle)

        val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.blfQuality.startAnimation(animSlide)
        binding.blfQuality.visibility = View.VISIBLE
        binding.blfQuality.requestLayout()
        isSideMenuShowing = true
    }


    private fun getCustomData(): CatchUpData? {
        val customData = catchUpList[0]
        return customData
    }


    private fun setCatchUpList() {
        catchUpList = intent.getParcelableArrayListExtra("list_data")!!
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
       // params["versionName"] = BuildConfig.VERSION_NAME
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
        logixPlayer?.seekTo(playbackPosition)
        logixPlayer?.play()

    }


    private fun upNext() {
        val position = catchUpList.indexOf(getCustomData())
        if (position < catchUpList.size - 1) {
            val updateCustomData = catchUpList[position + 1]
            startActivity(
                Intent(
                    this@VodPlayerActivity,
                    VodPlayerActivity::class.java
                ).apply {
                    putExtra("custom_data", updateCustomData)
                    putParcelableArrayListExtra("list_data", catchUpList)
                })
        } else {
            allCatchUpPlayed()
        }
        finish()
    }

    private fun retry() {
        binding.playerView.hideController()
        playbackPosition = logixPlayer?.getCurrentPosition() ?: 0
        isPlayerPlaying = false
        updateDurationHandler.removeCallbacks(startRunnableForProgress)
        releasePlayer()
        createMediaList()
        initPlayer()
        prepareAndPlay(logixMediaItems[0])
    }

    private fun releasePlayer() {
        isPlayerPlaying = logixPlayer?.isPlaying() == true
        playbackPosition = logixPlayer?.getCurrentPosition() ?: 0
        currentWindow = logixPlayer?.getCurrentMediaItemIndex() ?: 0
        logixPlayer?.release()
        isPlayingStarted = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        logixPlayer?.getCurrentMediaItemIndex()?.let { outState.putInt(STATE_RESUME_WINDOW, it) }
        logixPlayer?.getCurrentPosition()?.let { outState.putLong(STATE_RESUME_POSITION, it) }
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            showProgressDialog()
            autoHideAgePopup()
            prepareAndPlay(logixMediaItems[0])
        }
    }

    override fun onResume() {
        super.onResume()
        val url = getCustomData()?.thumbnail_url
        Glide.with(this).load(url).into(binding.ivBackground)
        updateDurationHandler.postDelayed(startRunnableForProgress, 0)
        resumeVideo()
        logixPlayer?.setAppInBackground(false)
    }

    override fun onPause() {
        super.onPause()
        updateDurationHandler.removeCallbacks(startRunnableForProgress)
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
                    if (!isSideMenuShowing && !isControllerVisible())
                        forwardBackwardPlayer(SCRUBBING_RIGHT)
                }

                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_L2, MotionEvent.AXIS_LTRIGGER, KeyEvent.KEYCODE_MEDIA_REWIND -> {

                    if (!isSideMenuShowing && !isControllerVisible())
                        forwardBackwardPlayer(SCRUBBING_LEFT)
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
            finish()
            super.onBackPressed()
        }
    }

    private fun resumeVideo() {
        logixPlayer?.resume()
    }

    private fun pauseVideo() {
        logixPlayer?.pause()
        startControlHideCountdown()
    }

    private fun togglePlayPause() {
        if (logixPlayer?.isPlaying() == true) pauseVideo() else resumeVideo()

    }

    private fun initController(customData: CatchUpData?) {

        seekBar =binding.layoutActions.seekBar

        customData.let {

            if (it != null) {
                binding.layoutActions.vodTitle.text = it.name
                binding.layoutActions.tvSubtitle.text = it.name

                binding.tvDetailTitle.text = it.name
                binding.tvDetailSubtitle.text = it.name
                // binding.tvDetailDescription.text = it.name
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    logixPlayer?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


    private val startRunnableForProgress = object : Runnable {
        override fun run() {
            val currentPositionMillis = logixPlayer?.getCurrentPosition() ?: 0
            updateDurationViews(currentPositionMillis)

            updateDurationHandler.postDelayed(this, 1000)

        }
    }

    private fun updateDurationViews(currentPositionMillis: Long) {
        val currentPosition = currentPositionMillis.formatTime()
        binding.layoutActions.playingDuration.text = currentPosition

        try {
            seekBar.progress = currentPositionMillis.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateRemainingDuration(currentPositionMillis)
    }

    private fun updateRemainingDuration(currentPositionMillis: Long) {
        val totalDurationMillis = logixPlayer?.getDuration() ?: 0
        val remainingDurationMillis = totalDurationMillis - currentPositionMillis
        val formattedTotalDuration = remainingDurationMillis.formatTime()
        binding.layoutActions.totalDuration.text = formattedTotalDuration
        if (remainingDurationMillis in 1..10000){
            hideController()
            upNextPopup(formattedTotalDuration)
        }
    }
    private fun upNextPopup(formattedTotalDuration: String) {
        val position = catchUpList.indexOf(getCustomData())+1
        if (position < catchUpList.size - 1) {
            binding.upnext.clUpNextPopup.visibility = View.VISIBLE
            binding.upnext.railUpnextDur.text = "Up Next in $formattedTotalDuration"
            binding.upnext.railDesc.text = catchUpList[position].name
            binding.upnext.railDuration.text =
                catchUpList[position].duration.let { getFormattedDuration(it).trim() }
            binding.upnext.railImage.load(catchUpList[position].thumbnail_url)
            binding.upnext.nextCard.setOnClickListener { upNext() }
        }
    }

    private fun Long.formatTime(): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun showErrorDialog() {
        CustomDialog(
            title = getString(R.string.error_message),
            message = getString(R.string.we_were_unable_to_process_your_request_please_try_again),
            positiveButtonText = getString(R.string.try_again),
            negativeButtonText = getString(R.string.exit_btn),
            cancellable = true,
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
        val url = intent.getStringExtra("live_poster_url").toString()
        Glide.with(this).load(url).into(binding.ivBackground)
        binding.progressBar.visibility = View.VISIBLE
        binding.ivBackground.visibility = View.VISIBLE
        binding.flBackground.visibility = View.VISIBLE

    }

    private fun hideProgressDialog() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.ivBackground.visibility = View.GONE
            binding.flBackground.visibility = View.GONE
        }, 500)
    }
    private fun autoHideAgePopup() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.inRateUs.clRateUsPopup.visibility = View.GONE
        }, 3000)
    }

    private fun forwardBackwardPlayer(mType: Int) {
        val currentPositionInMs = logixPlayer?.getCurrentPosition() ?: 0
        val seekAmountInSec = DEFAULT_SEEK_TO
        if (!isControllerVisible())
            showController()
        val seekPositionInMs = when (mType) {
            SCRUBBING_LEFT -> calculateSeekPosition(currentPositionInMs, -seekAmountInSec)
            SCRUBBING_RIGHT -> calculateSeekPosition(currentPositionInMs, seekAmountInSec)
            else -> currentPositionInMs
        }
        Handler(Looper.getMainLooper()).postDelayed({
            logixPlayer?.seekTo(seekPositionInMs)
            updateRemainingDuration(seekPositionInMs)
        }, 50)

    }

    private fun calculateSeekPosition(currentPositionInMs: Long, seekAmountInSec: Int): Long {
        val seekPositionInMs = currentPositionInMs + seekAmountInSec * 1000
        return when {
            seekPositionInMs < 0 -> 0
            seekPositionInMs > (logixPlayer?.getDuration() ?: 0) -> logixPlayer?.getDuration() ?: 0
            else -> seekPositionInMs
        }
    }


    private fun showNetworkMessage() {
        hideProgressDialog()
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
    }

    private fun isControllerVisible(): Boolean {
        return binding.layoutActions.vodPlayerController.visibility == View.VISIBLE
    }

    fun setDataCallBack(callBack: CatchUpEndCallback) {
        catchUpEndCallback = callBack
    }

    private fun allCatchUpPlayed() {
        catchUpEndCallback?.allCatchUpPlayed()

    }

    private fun startControlHideCountdown() {
        // Cancel any previous runnables
        controlHideHandler.removeCallbacks(controlHideRunnable)

        // Schedule hiding of controls after the desired duration (3 seconds)
        if (logixPlayer?.isPlaying() == false)
            controlHideHandler.postDelayed(controlHideRunnable, 3000)
    }

    private fun showController() {
        binding.llRailTitle.visibility = View.VISIBLE
        binding.ivPlayerLogo.visibility = View.GONE
        binding.rvRecommended.visibility = View.GONE
        if (binding.inRateUs.clRateUsPopup.visibility == View.VISIBLE)
            autoHideAgePopup()
        binding.layoutActions.vodPlayerController.visibility = View.VISIBLE
        showPlayerSettings()
        Handler().postDelayed({ binding.layoutActions.playPause.requestFocus() }, 100)
    }

    private fun hideController() {
        if(!isSideMenuShowing)
            binding.ivPlayerLogo.visibility = View.VISIBLE

        binding.llRailTitle.visibility = View.GONE
        binding.layoutActions.vodPlayerController.visibility = View.GONE
    }

    override fun onKeyEvents(event: KeyEvent) {
    }

    override fun onMediaItemChange(logixMediaItem: LogixMediaItem, position: Int) {
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            LogixPlayer.PlaybackState.STATE_IDLE -> {

            }

            LogixPlayer.PlaybackState.STATE_READY -> {
                hideProgressDialog()
                binding.progressBar.visibility = View.GONE
                if (!isPlayingStarted) {
                    isPlayingStarted = true
                    seekBar.max = (logixPlayer?.getDuration() ?: 0).toInt()
                    updateDurationHandler.postDelayed(startRunnableForProgress, 0)
                }
            }

            LogixPlayer.PlaybackState.STATE_BUFFERING -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            LogixPlayer.PlaybackState.STATE_ENDED -> {
                upNext()
            }
        }
    }

    override fun onPlayerError(playerError: LogixPlayerError) {
        if (playerError.errorCode == LogixPlayerError.Code.BEHIND_LIVE_WINDOW) {
            logixPlayer?.seekTo(0)
            prepareAndPlay(logixMediaItems[0])
        } else {
            hideProgressDialog()
            if (playerError.errorCode == LogixPlayerError.Code.TIMEOUT_ERROR_CODE ||
                !isNetworkAvailable(this@VodPlayerActivity)
            ) {
                showNetworkMessage()
            } else {
                showErrorDialog()
            }
        }
    }

    override fun onPlaylistAltered(action: LogixPlayer.PlaylistAction) {
    }

    override fun onProgressUpdate(progress: Long) {
    }

    override fun onRenderedFirstFrame() {
        if (isControllerVisible())
            hideController()
        showAudioTrackPopUpBottom()
        binding.ivPlayerLogo.visibility = View.VISIBLE
        autoHideAgePopup()
        if(!rateUsCheck){
            binding.inRateUs.clRateUsPopup.visibility = View.VISIBLE
            rateUsCheck = true
        }
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
        getCustomData()?.name?.let {
            getCustomData()?.id?.let { it1 ->
                LogixMediaItem(
                    id = it1,
                    title = it,
                    sourceUrl = getCustomData()?.video_url,
                    drmScheme = LogixMediaItem.ContentType.CLEAR,
                    streamType = LogixMediaItem.StreamType.VOD
                )
            }
        }?.let {
            logixMediaItems.add(
                it
            )
        }
    }

    private fun initClickListener() {
        binding.layoutActions.apply {
            setButtonClickListener(btnSkipRecap)
            setButtonClickListener(btnWatchFromBegnining)
            setButtonClickListener(btnSubtitle)
            setButtonClickListener(btnLanguage)
            setButtonClickListener(btnQuality)
            setButtonClickListener(playPause)
            setButtonClickListener(btnEpisodes)
            setButtonClickListener(btnNextEpisode)
            setAppCompactButton(btnDetails)

        }
    }

    private fun init() {
        binding.layoutActions.apply {
            setButtonFocusChangeListener(btnSkipRecap)
            setButtonFocusChangeListener(btnWatchFromBegnining)
            setButtonFocusChangeListener(btnSubtitle)
            setButtonFocusChangeListener(btnLanguage)
            setButtonFocusChangeListener(btnQuality)
            setButtonFocusChangeListener(btnEpisodes)
            setButtonFocusChangeListener(btnNextEpisode)
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
                R.id.btn_skip_recap -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_skip_recape_dark
                    else
                        R.drawable.ic_skip_recape
                }

                R.id.btn_watch_from_begnining -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_go_live_dark
                    else
                        R.drawable.ic_go_live
                }

                R.id.btn_subtitle -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_subtitle_icon_dark
                    else
                        R.drawable.ic_subtitle_icon
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

                R.id.btn_episodes -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_key_moment_dark
                    else
                        R.drawable.ic_key_moment
                }

                R.id.btn_next_episode -> {
                    iconResId = if (hasFocus)
                        R.drawable.ic_next_episode_dark
                    else
                        R.drawable.ic_next_episode
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
                R.id.btn_skip_recap -> {
                }

                R.id.btn_watch_from_begnining -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        logixPlayer?.seekTo(0)
                        updateRemainingDuration(0)
                    }, 50)
                }

                R.id.btn_subtitle -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    showSubtitlePopUp()
                }

                R.id.btn_language -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    showAudioTrackPopUp()
                }

                R.id.btn_quality -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    showQualityChangePopUp()
                }

                R.id.btn_episodes -> {
                    if (!isSideMenuShowing) {
                        binding.flBackgroundOverlay.visibility = View.VISIBLE
                        openEpisode()
                        isSideMenuShowing = true
                    }
                }

                R.id.btn_next_episode -> {
                    upNext()
                }

                R.id.playPause -> {
                    binding.layoutActions.playPause.setBackgroundResource(if (logixPlayer?.isPlaying() == true) R.drawable.ic_controls_play_focused else R.drawable.ic_controls_pause_focused)
                    togglePlayPause()
                }
            }
        }
    }
    private fun setAppCompactButton(button: AppCompatImageButton) {
        button.setOnClickListener { view ->
            when (view.id) {
                R.id.btn_details -> {
                    binding.flBackgroundOverlay.visibility = View.VISIBLE
                    binding.blfDetails.visibility =View.VISIBLE
                    isSideMenuShowing = true
                    hideController()
                }
            }
        }
    }

    private fun showMsg(msg: String) {
        Toast.makeText(this@VodPlayerActivity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun getFormattedDuration(duration: Int): String {
        var hours = (duration / 60) / 60
        var minutes = (duration / 60) % 60
        val seconds = duration % 60
        if (seconds in 30..59 && minutes < 60 || duration < 30) {
            minutes++
        }
        if ((duration / 60) % 60 == 59 && seconds >= 45) {
            hours++
        }
        val durationString = if (hours == 0)
            String.format("%2d ${getMinString(minutes)}", minutes)
        else {
            if (minutes == 0 || minutes == 60)
                String.format("%2d ${getHrString(hours)} ", hours)
            else
                String.format("%2d hrs ", hours) + String.format(
                    "%2d ${getMinString(minutes)}",
                    minutes
                )
        }
        return durationString
    }

    private fun getMinString(minutes: Int): String {
        return if (minutes == 1) "min" else "mins"
    }

    private fun getHrString(hour: Int): String {
        return if (hour == 1) "hr" else "hrs"
    }
    private fun showPlayerSettings() {
        if (logixPlayer?.getSubtitles()?.size != null && logixPlayer?.getSubtitles()?.size!! > 0)
            binding.layoutActions.btnSubtitle.visibility = View.VISIBLE
        if (logixPlayer?.getAudioTracks()?.size != null && logixPlayer?.getAudioTracks()?.size!! > 1)
            binding.layoutActions.btnLanguage.visibility = View.VISIBLE
        if (logixPlayer?.getVideoTracks()?.size != null && logixPlayer?.getVideoTracks()?.size!! > 1)
            binding.layoutActions.btnQuality.visibility = View.VISIBLE
    }
    private fun openEpisode() {
        hideController()
        episodeData()
        val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.blfEpisode.startAnimation(animSlide)
        binding.blfEpisode.visibility = View.VISIBLE
        binding.blfEpisode.requestLayout()
    }

    private fun closeSideMenu() {
        binding.flBackgroundOverlay.visibility = View.GONE
        binding.ivPlayerLogo.visibility = View.VISIBLE
        binding.blfEpisode.visibility = View.GONE
        binding.blfSubTitle.visibility = View.GONE
        binding.blfLanguage.visibility = View.GONE
        binding.blfQuality.visibility = View.GONE
        binding.blfDetails.visibility = View.GONE
        isSideMenuShowing = false
    }

    private fun loadCatchUpData() {
        val objectArrayString: String =
            resources.openRawResource(R.raw.catch_up).bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(objectArrayString)
        catchUpList.clear()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val catchUpItem =
                Gson().fromJson(jsonObject.toString(), CatchUpRailModel::class.java)
            catchUpRalList.add(catchUpItem)
            catchUpList.add(
                CatchUpData(
                    catchUpRalList[i].video_id,
                    catchUpRalList[i].category,
                    catchUpRalList[i].duration_secs,
                    catchUpRalList[i].thumbnail_url,
                    catchUpRalList[i].video_url
                )
            )
        }

    }

}

