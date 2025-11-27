package ru.netology.statsview.ui

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    private var animType = 0
    private val animators = mutableListOf<ValueAnimator>()
    private var animatorSet: AnimatorSet? = null
    private val animationTime: Long = 5000L


    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            animType = getInteger(R.styleable.StatsView_animType, 0)
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            when (animType) {
                0, 2 -> update()
                1 -> {
                    createAnimatorsSet()
                    progressList = MutableList(data.size) { 0F }
                }
            }
        }

    private var progressList = MutableList(data.size) { 0F }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        // Заполняем список цветами, чтобы во время выполнения цикла цвета не рандомились заного (мерцание цветов)
        while (true) {
            if (colors.size < data.size) {
                colors = colors + listOf(randomColor())
            } else break
        }

        var startFrom = -90F

        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            paint.color = colors.getOrNull(index) ?: randomColor()
            when (animType) {
                0 -> {
                    canvas.drawArc(
                        oval,
                        startFrom + (360F * progress),
                        angle * progress,
                        false,
                        paint
                    )
                }

                1 -> {
                    canvas.drawArc(
                        oval,
                        startFrom,
                        progressList[index],
                        false,
                        paint
                    )
                }

                2 -> {
                    canvas.drawArc(
                        oval,
                        startFrom + (angle / 2),
                        (angle / 2) * progress,
                        false,
                        paint
                    )
                    canvas.drawArc(
                        oval,
                        startFrom + (angle / 2),
                        -(angle / 2) * progress,
                        false,
                        paint
                    )
                }
            }
            startFrom += angle
        }
        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = animationTime
            interpolator = DecelerateInterpolator()
            startDelay = 3000
        }.also {
            it.start()
        }
    }

    private fun createAnimatorsSet() {
        animators.clear()
        animatorSet?.cancel()
        val sum = data.sum()

        for ((index, datum) in data.withIndex()) {
            val animator =
                ValueAnimator.ofFloat(0f, (360F * (datum / sum))).apply {
                    duration = (animationTime * (datum / sum)).toLong()
                    interpolator = LinearInterpolator()
                    addUpdateListener { anim ->
                        progressList[index] = anim.animatedValue as Float
                        invalidate()
                    }
                }
            animators.add(animator)
        }
        animatorSet = AnimatorSet().apply {
            playSequentially(animators.toList())
        }
        animatorSet?.apply {
            startDelay = 3000
        }?.start()
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}