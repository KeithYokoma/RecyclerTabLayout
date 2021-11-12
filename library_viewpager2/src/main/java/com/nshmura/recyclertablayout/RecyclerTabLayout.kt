/**
 * Copyright (C) 2015 nshmura
 * Copyright (C) 2015 The Android Open Source Project
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nshmura.recyclertablayout

import kotlin.jvm.JvmOverloads
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.animation.ValueAnimator
import android.content.Context
import androidx.core.view.ViewCompat
import android.view.ViewGroup
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextUtils.TruncateAt.END
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.TextViewCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlin.math.abs

class RecyclerTabLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {
  private var indicatorPaint: Paint
  private var tabBackgroundResId = 0
  private var tabOnScreenLimit = 0
  private var tabMinWidth = 0
  private var tabMaxWidth = 0
  private var tabTextAppearance = 0
  private var tabSelectedTextColor = 0
  private var tabSelectedTextColorSet = false
  private var tabPaddingStart = 0
  private var tabPaddingTop = 0
  private var tabPaddingEnd = 0
  private var tabPaddingBottom = 0
  private var indicatorHeight = 0
  private var linearLayoutManager: LinearLayoutManager
  private var recyclerOnScrollListener: RecyclerOnScrollListener? = null
  private var viewPager: ViewPager2? = null
  private var adapter: Adapter<*>? = null
  private var indicatorPosition = 0
  private var indicatorGap = 0
  private var indicatorScroll = 0
  private var oldPositionOffset = 0f
  private var positionThreshold: Float
  private var requestScrollToTab = false
  private var scrollEnabled = false

  private val isLayoutRtl: Boolean
    get() = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
  private val onPageChangeCallback: OnPageChangeCallback = ViewPagerOnPageChangeListener(this)

  init {
    setWillNotDraw(false)
    indicatorPaint = Paint()
    getAttributes(context, attrs, defStyle)
    linearLayoutManager = object : LinearLayoutManager(getContext()) {
      override fun canScrollHorizontally(): Boolean {
        return scrollEnabled
      }
    }
    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
    layoutManager = linearLayoutManager
    itemAnimator = null
    positionThreshold = DEFAULT_POSITION_THRESHOLD
  }

  private fun getAttributes(context: Context, attrs: AttributeSet?, defStyle: Int) {
    context.obtainStyledAttributes(attrs, R.styleable.RecyclerTabLayout, defStyle, R.style.rtl_RecyclerTabLayout).use { a ->
      setIndicatorColor(a.getColor(R.styleable.RecyclerTabLayout_rtl_tabIndicatorColor, 0))
      setIndicatorHeight(a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabIndicatorHeight, 0))
      tabTextAppearance = a.getResourceId(R.styleable.RecyclerTabLayout_rtl_tabTextAppearance, R.style.rtl_RecyclerTabLayout_Tab)
      tabPaddingBottom = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabPadding, 0)
      tabPaddingEnd = tabPaddingBottom
      tabPaddingTop = tabPaddingEnd
      tabPaddingStart = tabPaddingTop
      tabPaddingStart = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabPaddingStart, tabPaddingStart)
      tabPaddingTop = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabPaddingTop, tabPaddingTop)
      tabPaddingEnd = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabPaddingEnd, tabPaddingEnd)
      tabPaddingBottom = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabPaddingBottom, tabPaddingBottom)
      if (a.hasValue(R.styleable.RecyclerTabLayout_rtl_tabSelectedTextColor)) {
        tabSelectedTextColor = a.getColor(R.styleable.RecyclerTabLayout_rtl_tabSelectedTextColor, 0)
        tabSelectedTextColorSet = true
      }
      tabOnScreenLimit = a.getInteger(R.styleable.RecyclerTabLayout_rtl_tabOnScreenLimit, 0)
      if (tabOnScreenLimit == 0) {
        tabMinWidth = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabMinWidth, 0)
        tabMaxWidth = a.getDimensionPixelSize(R.styleable.RecyclerTabLayout_rtl_tabMaxWidth, 0)
      }
      tabBackgroundResId = a.getResourceId(R.styleable.RecyclerTabLayout_rtl_tabBackground, 0)
      scrollEnabled = a.getBoolean(R.styleable.RecyclerTabLayout_rtl_scrollEnabled, true)
    }
  }

  override fun onDetachedFromWindow() {
    recyclerOnScrollListener?.let { listener ->
      removeOnScrollListener(listener)
      recyclerOnScrollListener = null
    }
    viewPager?.unregisterOnPageChangeCallback(onPageChangeCallback)
    super.onDetachedFromWindow()
  }

  fun setIndicatorColor(color: Int) {
    indicatorPaint.color = color
  }

  fun setIndicatorHeight(indicatorHeight: Int) {
    this.indicatorHeight = indicatorHeight
  }

  fun setAutoSelectionMode(autoSelect: Boolean) {
    recyclerOnScrollListener?.let { listener ->
      removeOnScrollListener(listener)
      recyclerOnScrollListener = null
    }
    if (autoSelect) {
      recyclerOnScrollListener = RecyclerOnScrollListener(this, linearLayoutManager).also { listener ->
        addOnScrollListener(listener)
      }
    }
  }

  fun setPositionThreshold(positionThreshold: Float) {
    this.positionThreshold = positionThreshold
  }

  fun setUpWithViewPager(viewPager: ViewPager2) {
    val adapter = DefaultAdapter(viewPager)
    adapter.setTabPadding(tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom)
    adapter.setTabTextAppearance(tabTextAppearance)
    adapter.setTabSelectedTextColor(tabSelectedTextColorSet, tabSelectedTextColor)
    adapter.setTabMaxWidth(tabMaxWidth)
    adapter.setTabMinWidth(tabMinWidth)
    adapter.setTabBackgroundResId(tabBackgroundResId)
    adapter.setTabOnScreenLimit(tabOnScreenLimit)
    setUpWithAdapter(adapter)
  }

  fun setUpWithAdapter(adapter: Adapter<*>) {
    this.adapter = adapter
    this.viewPager = adapter.viewPager.also { pager ->
      pager.registerOnPageChangeCallback(onPageChangeCallback)
      setAdapter(adapter)
      scrollToTab(pager.currentItem)
    }
  }

  fun setCurrentItem(position: Int, smoothScroll: Boolean) {
    viewPager?.let { pager ->
      pager.setCurrentItem(position, smoothScroll)
      scrollToTab(pager.currentItem)
      return
    }
    if (smoothScroll && position != indicatorPosition) {
      startAnimation(position)
    } else {
      scrollToTab(position)
    }
  }

  private fun startAnimation(position: Int) {
    var distance = 1f
    val view = linearLayoutManager.findViewByPosition(position)
    if (view != null) {
      val currentX = view.x + view.measuredWidth / 2f
      val centerX = measuredWidth / 2f
      distance = abs(centerX - currentX) / view.measuredWidth
    }
    val animator: ValueAnimator = if (position < indicatorPosition) {
      ValueAnimator.ofFloat(distance, 0f)
    } else {
      ValueAnimator.ofFloat(-distance, 0f)
    }
    animator.duration = DEFAULT_SCROLL_DURATION
    animator.addUpdateListener { animation -> scrollToTab(position, animation.animatedValue as Float, true) }
    animator.start()
  }

  private fun scrollToTab(position: Int) {
    scrollToTab(position, 0f, false)
    adapter?.let { adapter ->
      adapter.currentIndicatorPosition = position
      adapter.notifyDataSetChanged()
    }
  }

  private fun scrollToTab(position: Int, positionOffset: Float, fitIndicator: Boolean) {
    var scrollOffset = 0
    val selectedView = linearLayoutManager.findViewByPosition(position)
    val nextView = linearLayoutManager.findViewByPosition(position + 1)
    if (selectedView != null) {
      val width = measuredWidth
      val sLeft: Float = if (position == 0) {
        0.0f
      } else {
        width / 2f - selectedView.measuredWidth / 2f // left edge of selected tab
      }
      val sRight = sLeft + selectedView.measuredWidth // right edge of selected tab
      if (nextView != null) {
        val nLeft = width / 2f - nextView.measuredWidth / 2f // left edge of next tab
        val distance = sRight - nLeft // total distance that is needed to distance to next tab
        val dx = distance * positionOffset
        scrollOffset = (sLeft - dx).toInt()
        if (position == 0) {
          val indicatorGap = ((nextView.measuredWidth - selectedView.measuredWidth) / 2).toFloat()
          this.indicatorGap = (indicatorGap * positionOffset).toInt()
          indicatorScroll = ((selectedView.measuredWidth + indicatorGap) * positionOffset).toInt()
        } else {
          val indicatorGap = ((nextView.measuredWidth - selectedView.measuredWidth) / 2).toFloat()
          this.indicatorGap = (indicatorGap * positionOffset).toInt()
          indicatorScroll = dx.toInt()
        }
      } else {
        scrollOffset = sLeft.toInt()
        indicatorScroll = 0
        indicatorGap = 0
      }
      if (fitIndicator) {
        indicatorScroll = 0
        indicatorGap = 0
      }
    } else {
      if (measuredWidth > 0 && tabMaxWidth > 0 && tabMinWidth == tabMaxWidth) { //fixed size
        val width = tabMinWidth
        val offset = (positionOffset * -width).toInt()
        val leftOffset = ((measuredWidth - width) / 2f).toInt()
        scrollOffset = offset + leftOffset
      }
      requestScrollToTab = true
    }
    updateCurrentIndicatorPosition(position, positionOffset - oldPositionOffset, positionOffset)
    indicatorPosition = position
    stopScroll()
    linearLayoutManager.scrollToPositionWithOffset(position, scrollOffset)
    if (indicatorHeight > 0) {
      invalidate()
    }
    oldPositionOffset = positionOffset
  }

  private fun updateCurrentIndicatorPosition(position: Int, dx: Float, positionOffset: Float) {
    adapter?.let { adapter ->
      var indicatorPosition = -1
      if (dx > 0 && positionOffset >= positionThreshold - POSITION_THRESHOLD_ALLOWABLE) {
        indicatorPosition = position + 1
      } else if (dx < 0 && positionOffset <= 1 - positionThreshold + POSITION_THRESHOLD_ALLOWABLE) {
        indicatorPosition = position
      }
      if (indicatorPosition >= 0 && indicatorPosition != adapter.currentIndicatorPosition) {
        adapter.currentIndicatorPosition = indicatorPosition
        adapter.notifyDataSetChanged()
      }
    }
  }

  override fun onDraw(canvas: Canvas) {
    val view = linearLayoutManager.findViewByPosition(indicatorPosition)
    if (view == null) {
      if (requestScrollToTab) {
        requestScrollToTab = false
        viewPager?.let { pager ->
          scrollToTab(pager.currentItem)
        }
      }
      return
    }
    requestScrollToTab = false
    val left: Int
    val right: Int
    if (isLayoutRtl) {
      left = view.left - indicatorScroll - indicatorGap
      right = view.right - indicatorScroll + indicatorGap
    } else {
      left = view.left + indicatorScroll - indicatorGap
      right = view.right + indicatorScroll + indicatorGap
    }
    val top = height - indicatorHeight
    val bottom = height
    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), indicatorPaint)
  }

  private class RecyclerOnScrollListener(
    private var recyclerTabLayout: RecyclerTabLayout,
    private var linearLayoutManager: LinearLayoutManager,
  ) : OnScrollListener() {
    private var dx = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      this.dx += dx
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      when (newState) {
        SCROLL_STATE_IDLE -> {
          if (dx > 0) {
            selectCenterTabForRightScroll()
          } else {
            selectCenterTabForLeftScroll()
          }
          dx = 0
        }
        SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING -> {
        }
      }
    }

    private fun selectCenterTabForRightScroll() {
      val first = linearLayoutManager.findFirstVisibleItemPosition()
      val last = linearLayoutManager.findLastVisibleItemPosition()
      val center = recyclerTabLayout.width / 2
      for (position in first..last) {
        val view = linearLayoutManager.findViewByPosition(position)
        if (view!!.left + view.width >= center) {
          recyclerTabLayout.setCurrentItem(position, false)
          break
        }
      }
    }

    private fun selectCenterTabForLeftScroll() {
      val first = linearLayoutManager.findFirstVisibleItemPosition()
      val last = linearLayoutManager.findLastVisibleItemPosition()
      val center = recyclerTabLayout.width / 2
      for (position in last downTo first) {
        val view = linearLayoutManager.findViewByPosition(position)
        if (view!!.left <= center) {
          recyclerTabLayout.setCurrentItem(position, false)
          break
        }
      }
    }
  }

  private class ViewPagerOnPageChangeListener(
    private val recyclerTabLayout: RecyclerTabLayout,
  ) : OnPageChangeCallback() {
    private var scrollState = 0

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      recyclerTabLayout.scrollToTab(position, positionOffset, false)
    }

    override fun onPageScrollStateChanged(state: Int) {
      scrollState = state
    }

    override fun onPageSelected(position: Int) {
      if (scrollState == ViewPager2.SCROLL_STATE_IDLE) {
        if (recyclerTabLayout.indicatorPosition != position) {
          recyclerTabLayout.scrollToTab(position)
        }
      }
    }
  }

  interface HasPageTitle {
    fun getPageTitle(position: Int): String = ""
  }

  abstract class Adapter<T : ViewHolder?>(val viewPager: ViewPager2) : RecyclerView.Adapter<T>(), HasPageTitle {
    var currentIndicatorPosition = 0
  }

  class DefaultAdapter(viewPager: ViewPager2) : Adapter<DefaultAdapter.ViewHolder>(viewPager) {
    private var tabPaddingStart = 0
    private var tabPaddingTop = 0
    private var tabPaddingEnd = 0
    private var tabPaddingBottom = 0
    private var tabTextAppearance = 0
    private var tabSelectedTextColorSet = false
    private var tabSelectedTextColor = 0
    private var tabMaxWidth = 0
    private var tabMinWidth = 0
    private var tabBackgroundResId = 0
    private var tabOnScreenLimit = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val tabTextView = TabTextView(parent.context)
      if (tabSelectedTextColorSet) {
        tabTextView.setTextColor(tabTextView.createColorStateList(tabTextView.currentTextColor, tabSelectedTextColor))
      }
      ViewCompat.setPaddingRelative(tabTextView, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom)
      TextViewCompat.setTextAppearance(tabTextView, tabTextAppearance)
      tabTextView.gravity = Gravity.CENTER
      tabTextView.maxLines = MAX_TAB_TEXT_LINES
      tabTextView.ellipsize = END
      if (tabOnScreenLimit > 0) {
        val width = parent.measuredWidth / tabOnScreenLimit
        tabTextView.maxWidth = width
        tabTextView.minWidth = width
      } else {
        if (tabMaxWidth > 0) {
          tabTextView.maxWidth = tabMaxWidth
        }
        tabTextView.minWidth = tabMinWidth
      }
      TextViewCompat.setTextAppearance(tabTextView, tabTextAppearance)
      if (tabSelectedTextColorSet) {
        tabTextView.setTextColor(tabTextView.createColorStateList(
          tabTextView.currentTextColor, tabSelectedTextColor))
      }
      if (tabBackgroundResId != 0) {
        tabTextView.setBackgroundDrawable(AppCompatResources.getDrawable(tabTextView.context, tabBackgroundResId))
      }
      tabTextView.layoutParams = createLayoutParamsForTabs()
      return ViewHolder(tabTextView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val title = (viewPager.adapter as? Adapter)?.getPageTitle(position) ?: return
      holder.title.text = title
      holder.title.isSelected = currentIndicatorPosition == position
    }

    override fun getItemCount(): Int = viewPager.adapter?.itemCount ?: 0

    fun setTabPadding(
      tabPaddingStart: Int,
      tabPaddingTop: Int,
      tabPaddingEnd: Int,
      tabPaddingBottom: Int,
    ) {
      this.tabPaddingStart = tabPaddingStart
      this.tabPaddingTop = tabPaddingTop
      this.tabPaddingEnd = tabPaddingEnd
      this.tabPaddingBottom = tabPaddingBottom
    }

    fun setTabTextAppearance(tabTextAppearance: Int) {
      this.tabTextAppearance = tabTextAppearance
    }

    fun setTabSelectedTextColor(
      tabSelectedTextColorSet: Boolean,
      tabSelectedTextColor: Int,
    ) {
      this.tabSelectedTextColorSet = tabSelectedTextColorSet
      this.tabSelectedTextColor = tabSelectedTextColor
    }

    fun setTabMaxWidth(tabMaxWidth: Int) {
      this.tabMaxWidth = tabMaxWidth
    }

    fun setTabMinWidth(tabMinWidth: Int) {
      this.tabMinWidth = tabMinWidth
    }

    fun setTabBackgroundResId(tabBackgroundResId: Int) {
      this.tabBackgroundResId = tabBackgroundResId
    }

    fun setTabOnScreenLimit(tabOnScreenLimit: Int) {
      this.tabOnScreenLimit = tabOnScreenLimit
    }

    private fun createLayoutParamsForTabs(): LayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      var title: TextView = itemView as TextView

      init {
        itemView.setOnClickListener {
          val pos = bindingAdapterPosition
          if (pos != NO_POSITION) {
            viewPager.setCurrentItem(pos, true)
          }
        }
      }
    }

    companion object {
      private const val MAX_TAB_TEXT_LINES = 2
    }
  }

  class TabTextView(context: Context) : AppCompatTextView(context) {
    fun createColorStateList(defaultColor: Int, selectedColor: Int): ColorStateList {
      val states = arrayOfNulls<IntArray>(2)
      val colors = IntArray(2)
      states[0] = SELECTED_STATE_SET
      colors[0] = selectedColor
      // Default enabled state
      states[1] = EMPTY_STATE_SET
      colors[1] = defaultColor
      return ColorStateList(states, colors)
    }
  }

  companion object {
    private const val DEFAULT_SCROLL_DURATION: Long = 200
    private const val DEFAULT_POSITION_THRESHOLD = 0.6f
    private const val POSITION_THRESHOLD_ALLOWABLE = 0.001f
  }
}