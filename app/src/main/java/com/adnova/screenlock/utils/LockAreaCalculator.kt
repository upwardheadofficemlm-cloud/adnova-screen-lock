package com.adnova.screenlock.utils

import android.content.Context
import android.graphics.Rect
import android.view.WindowManager
import com.adnova.screenlock.data.CustomArea
import com.adnova.screenlock.data.EdgeType
import com.adnova.screenlock.data.LockType

class LockAreaCalculator(private val context: Context) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    fun calculateLockAreas(lockType: LockType, edgeType: EdgeType? = null, customArea: CustomArea? = null): List<Rect> {
        val screenSize = getScreenSize()
        val areas = mutableListOf<Rect>()
        
        when (lockType) {
            LockType.FULL_SCREEN -> {
                areas.add(Rect(0, 0, screenSize.width, screenSize.height))
            }
            LockType.EDGE_LOCK -> {
                areas.addAll(calculateEdgeAreas(edgeType ?: EdgeType.BOTH_EDGES, screenSize))
            }
            LockType.CUSTOM_AREA -> {
                customArea?.let { area ->
                    areas.add(Rect(area.x, area.y, area.x + area.width, area.y + area.height))
                }
            }
        }
        
        return areas
    }
    
    private fun calculateEdgeAreas(edgeType: EdgeType, screenSize: ScreenSize): List<Rect> {
        val areas = mutableListOf<Rect>()
        val edgeWidth = (screenSize.width * 0.1f).toInt() // 10% of screen width
        
        when (edgeType) {
            EdgeType.LEFT_EDGE -> {
                areas.add(Rect(0, 0, edgeWidth, screenSize.height))
            }
            EdgeType.RIGHT_EDGE -> {
                areas.add(Rect(screenSize.width - edgeWidth, 0, screenSize.width, screenSize.height))
            }
            EdgeType.BOTH_EDGES -> {
                areas.add(Rect(0, 0, edgeWidth, screenSize.height))
                areas.add(Rect(screenSize.width - edgeWidth, 0, screenSize.width, screenSize.height))
            }
        }
        
        return areas
    }
    
    private fun getScreenSize(): ScreenSize {
        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getSize(size)
        return ScreenSize(size.x, size.y)
    }
    
    fun isPointInLockArea(x: Float, y: Float, lockAreas: List<Rect>): Boolean {
        return lockAreas.any { area ->
            area.contains(x.toInt(), y.toInt())
        }
    }
    
    fun getLockAreaAtPoint(x: Float, y: Float, lockAreas: List<Rect>): Rect? {
        return lockAreas.find { area ->
            area.contains(x.toInt(), y.toInt())
        }
    }
    
    data class ScreenSize(val width: Int, val height: Int)
}
