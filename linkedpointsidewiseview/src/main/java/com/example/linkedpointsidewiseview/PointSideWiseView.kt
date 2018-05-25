package com.example.linkedpointsidewiseview

/**
 * Created by anweshmishra on 25/05/18.
 */

import android.app.Activity
import android.view.View
import android.content.Context
import android.view.MotionEvent
import android.graphics.*

val PSW_NODES : Int = 6

class PointSideWiseView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(scale)

            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class PSWNode (var i : Int, val state : State = State()) {

        var next : PSWNode? = null

        var prev : PSWNode? = null

        fun addNeighbor() {
            if (i < PSW_NODES - 1) {
                next = PSWNode(i + 1)
                next?.prev = this
                next?.addNeighbor()
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            paint.color = Color.parseColor("#e67e22")
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = h / (2 * PSW_NODES + 1)
            val size : Float = w/10
            val x : Float = ((1 - i%2) * ((-size) * (1 - state.scale))) + ((w + ((size) * (1 - state.scale))) * (i%2))
            prev?.draw(canvas, paint)
            canvas.save()
            canvas.translate(x, 3 * gap/2 + i * 2 * gap)
            val path = Path()
            path.moveTo(0f, -gap/2)
            path.lineTo(0f, gap/2)
            path.lineTo(size * (1 - 2 * (i%2)), 0f)
            path.lineTo(0f, -gap/2)
            canvas.drawPath(path, paint)
            canvas.restore()
            val deg_gap : Float = 360f / PSW_NODES
            val r : Float = Math.min(w, h) / 12
            if (state.scale > 0f) {
                for (i in 0..1) {
                    canvas.save()
                    canvas.translate(w / 2, h / 10 + 0.7f * h * i)
                    canvas.drawArc(RectF(-r, -r, r, r), this.i * deg_gap, deg_gap * state.scale, true, paint)
                    canvas.restore()
                }
            }
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PSWNode {
            var curr : PSWNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedPointSideWise (var i : Int) {

        var curr : PSWNode = PSWNode(0)

        var dir : Int = 1
        init {
            curr.addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update { scale ->
                curr = curr.getNext(dir) {
                    this.dir *= -1
                }
                stopcb(scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer (var view : PointSideWiseView) {

        private val animator : Animator = Animator(view)

        private val pointedSideWise : LinkedPointSideWise = LinkedPointSideWise(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            pointedSideWise.draw(canvas, paint)
            animator.animate {
                pointedSideWise.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            pointedSideWise.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : PointSideWiseView {
            val view : PointSideWiseView = PointSideWiseView(activity)
            activity.setContentView(view)
            return view
        }
    }
}