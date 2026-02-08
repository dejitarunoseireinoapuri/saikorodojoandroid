package com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data

import android.content.Context
import android.media.SoundPool
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundPlayer
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundSettingsRepository

class AndroidSoundPlayer(
    context: Context,
    private val soundSettingsRepository: SoundSettingsRepository,
    maxStreams: Int = DEFAULT_MAX_STREAMS
) : SoundPlayer {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(maxStreams)
        .build()

    private val soundIds = mapOf(
        SoundEffect.USE to soundPool.load(context, R.raw.use, 1),
        SoundEffect.CARD_DRAW to soundPool.load(context, R.raw.card_draw, 1),
        SoundEffect.DICE_ROLL to soundPool.load(context, R.raw.dice_roll, 1),
        SoundEffect.SUCCESS to soundPool.load(context, R.raw.success, 1),
        SoundEffect.LOSS to soundPool.load(context, R.raw.loss, 1),
        SoundEffect.MOVE_DICE to soundPool.load(context, R.raw.move_dice, 1),
        SoundEffect.QUESTION to soundPool.load(context, R.raw.question, 1)
    )

    override fun play(effect: SoundEffect) {
        if (!soundSettingsRepository.isSoundEnabled()) return
        val soundId = soundIds[effect] ?: return
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }

    private companion object {
        private const val DEFAULT_MAX_STREAMS = 6
    }
}
