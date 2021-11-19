package com.amaizzzing.sobes3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.atan2
import kotlin.math.min

class CarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var lineColor = DEFAULT_LINE_COLOR
    private var needDashEffect = DEFAULT_NEED_DASH_EFFECT
    private var countEndPoints = DEFAULT_COUNT_END_POINTS
    private var showCarPath = DEFAULT_SHOW_CAR_PATH
    private var defaultStartX = DEFAULT_START_X
    private var defaultStartY = DEFAULT_START_Y
    private var dureationAnimation: Long = DEFAULT_DURATION

    private var mBitmap: Bitmap? = null

    private var minWidthView = MIN_WIDTH
    private var minHeightView = MIN_HEIGHT
    private var xPropertyList = mutableListOf<Float>()
    private var yPropertyList = mutableListOf<Float>()

    private val matrixCar = Matrix()

    private val paintLine =  Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePath = Path()

    private var pMeasure: PathMeasure? = null
    private var length: Float = 0f
    private var distance: Float
    private var pos: FloatArray
    private var tan: FloatArray
    private var mOffsetY = 20
    private var mOffsetX = 20

    private var animator: ValueAnimator? = null

    private var clickListener: OnClickListener? = null

    init {
        setupAttributes(attrs)

        paintLine.style = Paint.Style.STROKE
        if (needDashEffect) {
            paintLine.pathEffect = DashPathEffect(floatArrayOf(25f, 10f), 0f)
        }

        paintLine.color = lineColor
        paintLine.strokeWidth = 8f

        distance = 0f

        pos = FloatArray(2)
        tan = FloatArray(2)

        mBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.car
        )
        mBitmap?.let {
            mOffsetX = it.width / 2
            mOffsetY = it.height / 2
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            startAnimation()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        clickListener = l
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desireWidth = minWidthView + paddingLeft + paddingRight
        val desireHeight = minWidthView + paddingTop + paddingBottom

        minWidthView = measureDimensions(desireWidth, widthMeasureSpec)
        minHeightView = measureDimensions(desireHeight, heightMeasureSpec)

        setMeasuredDimension(
            minWidthView,
            minHeightView
        )

        configLinePath()
    }

    private fun configLinePath() {
        linePath.reset()
        populatePoints()
        createLines()
        invalidate()
    }

    private fun createLines() {
        linePath.moveTo(xPropertyList.first(), yPropertyList.first())
        xPropertyList.forEachIndexed { index, fl ->
            if (index != 0) {
                linePath.lineTo(fl, yPropertyList[index])
            }
        }
        pMeasure = PathMeasure(linePath, false)
        length = pMeasure?.length ?: 0f
    }

    private fun populatePoints() {
        xPropertyList.clear()
        yPropertyList.clear()
        xPropertyList.add(defaultStartX)
        yPropertyList.add(defaultStartY)
        (0..countEndPoints).forEach { _ ->
            xPropertyList.add((0..minWidthView).random().toFloat())
            yPropertyList.add((0..minHeightView).random().toFloat())
        }
        xPropertyList.add(minWidthView.toFloat() - minWidthView*0.1f)
        yPropertyList.add(minHeightView.toFloat() - minHeightView*0.1f)
    }

    private fun measureDimensions(desiredSize: Int, measureSpec: Int): Int {
        var result = 0
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        result = when (mode) {
            MeasureSpec.EXACTLY -> {
                size
            }
            MeasureSpec.AT_MOST -> {
                result = desiredSize
                min(result, size)
            }
            else -> {
                result
            }
        }
        if (result < desiredSize) {
            Log.d("CarView", "Too small size...")
        }

        return result
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawLines(canvas)
        configMatrixForCar()
        drawCar(canvas)
    }

    private fun configMatrixForCar() {
        pMeasure?.getPosTan(distance, pos, tan)
        matrixCar.reset()
        matrixCar.postRotate(
            (atan2(tan[1], tan[0]) * 135.0 / Math.PI).toFloat(),
            mOffsetX.toFloat(),
            mOffsetY.toFloat()
        )
        matrixCar.postTranslate(pos[0] - mOffsetX.toFloat(), pos[1] - mOffsetY.toFloat())
    }

    private fun drawCar(canvas: Canvas?) {
        mBitmap?.let {
            canvas?.drawBitmap(it, matrixCar,null)
        }
 }

    private fun drawLines(canvas: Canvas?) {
        if (showCarPath) {
            canvas?.drawPath(linePath, paintLine)
        }
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CarView,
            0, 0)

        lineColor = typedArray.getColor(R.styleable.CarView_lineColor, DEFAULT_LINE_COLOR)
        needDashEffect = typedArray.getBoolean(R.styleable.CarView_needDashEffect, DEFAULT_NEED_DASH_EFFECT)
        showCarPath = typedArray.getBoolean(R.styleable.CarView_showCarPath, DEFAULT_SHOW_CAR_PATH)
        countEndPoints = typedArray.getInt(R.styleable.CarView_countEndPoints, DEFAULT_COUNT_END_POINTS)
        dureationAnimation = typedArray.getInt(R.styleable.CarView_dureationAnimation, DEFAULT_DURATION.toInt()).toLong()
        defaultStartX = typedArray.getFloat(R.styleable.CarView_defaultStartX, DEFAULT_START_X)
        defaultStartY = typedArray.getFloat(R.styleable.CarView_defaultStartY, DEFAULT_START_Y)

        typedArray.recycle()
    }

    fun startAnimation() {
        if (animator == null || animator?.isRunning == false) {
            animator = ValueAnimator.ofFloat(0f, length).apply {
                duration = dureationAnimation
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    distance = valueAnimator.animatedValue as Float
                    invalidate()
                }
            }
            animator?.start()
        }
    }

    fun stopAnimation() {
        animator?.removeAllUpdateListeners()
        animator?.cancel()
    }

    fun recreatePath() {
        configLinePath()
    }

    companion object {
        private const val DEFAULT_LINE_COLOR = Color.BLUE
        private const val DEFAULT_COUNT_END_POINTS = 3
        private const val DEFAULT_DURATION = 15000L
        private const val DEFAULT_START_X = 40f
        private const val DEFAULT_START_Y = 40f
        private const val DEFAULT_NEED_DASH_EFFECT = true
        private const val DEFAULT_SHOW_CAR_PATH = false
        private const val MIN_WIDTH = 150
        private const val MIN_HEIGHT = 150
    }
}