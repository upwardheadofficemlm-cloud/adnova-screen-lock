package com.adnova.screenlock.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LockConfiguration(
    val lockType: LockType = LockType.FULL_SCREEN,
    val edgeType: EdgeType = EdgeType.BOTH_EDGES,
    val customArea: CustomArea? = null,
    val floatingButtonEnabled: Boolean = true,
    val doubleTapUnlock: Boolean = true,
    val tapsToReveal: Int = 3,
    val pinUnlockEnabled: Boolean = false,
    val pinHash: String? = null,
    val blockIconEnabled: Boolean = true,
    val animationEnabled: Boolean = true,
    val volumeButtonLock: Boolean = false,
    val autoLockOnBoot: Boolean = false,
    val kioskMode: Boolean = false,
    val whitelistApps: List<String> = emptyList()
) : Parcelable

@Parcelize
enum class LockType : Parcelable {
    FULL_SCREEN,
    EDGE_LOCK,
    CUSTOM_AREA
}

@Parcelize
enum class EdgeType : Parcelable {
    LEFT_EDGE,
    RIGHT_EDGE,
    BOTH_EDGES
}

@Parcelize
data class CustomArea(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) : Parcelable

@Parcelize
data class LockStatus(
    val isLocked: Boolean = false,
    val lockType: LockType = LockType.FULL_SCREEN,
    val startTime: Long = 0L,
    val unlockAttempts: Int = 0
) : Parcelable
