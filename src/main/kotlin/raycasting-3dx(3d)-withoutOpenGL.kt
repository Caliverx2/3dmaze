package org.lewapnoob
/*
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.swing.JFrame
import kotlin.math.*

const val SCREEN_WIDTH = 1280
const val SCREEN_HEIGHT = 720
const val MAP_SIZE_X = 16
const val MAP_SIZE_Y = 16
const val MAP_SIZE_Z = 3
const val VOXEL_SIZE = 64.0
const val PLAYER_SPEED = 15.0
const val ROT_SPEED = 0.05
const val MAX_DEPTH = 1000.0

// FOV w stopniach
var FOV_HORIZONTAL = 90.0 // Domyślnie 90°
var FOV_VERTICAL = 90.0   // Domyślnie 60°
var rayStepX = 1 // Krok promieni w osi X
var rayStepY = 1 // Krok promieni w osi Y

// Mapa 3D: 0 = pustka, 1 = ściana, 2 = podłoga, 3 = sufit
val map = Array(MAP_SIZE_Z) { z ->
    Array(MAP_SIZE_Y) { y ->
        IntArray(MAP_SIZE_X) { x ->
            when {
                z == 0 -> 2 // Podłoga
                z == MAP_SIZE_Z - 1 -> 3 // Sufit
                z > 0 && z < MAP_SIZE_Z - 1 && (x == 0 || x == MAP_SIZE_X - 1 || y == 0 || y == MAP_SIZE_Y - 1) -> 1 // Ściany
                z == 1 && x == 4 && y == 4 -> 1
                z == 1 && x == 8 && y == 4 -> 1// Wewnętrzna ściana
                else -> 0
            }
        }
    }
}

var playerX = 2.5 * VOXEL_SIZE
var playerY = 2.5 * VOXEL_SIZE
var playerZ = 1.5 * VOXEL_SIZE
var playerAngle = 0.0 // Kąt w poziomie (radiany)
var playerPitch = 0.0 // Kąt w pionie (radiany)

fun main() {
    val frame = JFrame("raycasting-3dx(3d)-withoutOpenGL.kt").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT)
        isResizable = false
        setLocationRelativeTo(null)
    }

    val canvas = RaycastingCanvas()
    frame.add(canvas)
    frame.isVisible = true

    Thread {
        while (true) {
            canvas.handleInput()
            canvas.repaint()
            Thread.sleep(16) // ~60 FPS
        }
    }.start()
}

class RaycastingCanvas : Canvas() {
    private val image = BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB)
    private val keysPressed = mutableSetOf<Int>()

    init {
        isFocusable = true
        requestFocus()
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                keysPressed.add(e.keyCode)
            }

            override fun keyReleased(e: KeyEvent) {
                keysPressed.remove(e.keyCode)
            }
        })
    }

    fun handleInput() {
        val dx = cos(playerAngle) * PLAYER_SPEED
        val dy = sin(playerAngle) * PLAYER_SPEED
        val strafeDx = cos(playerAngle + Math.PI / 2) * PLAYER_SPEED
        val strafeDy = sin(playerAngle + Math.PI / 2) * PLAYER_SPEED

        if (KeyEvent.VK_W in keysPressed) movePlayer(dx, dy)
        if (KeyEvent.VK_S in keysPressed) movePlayer(-dx, -dy)
        if (KeyEvent.VK_A in keysPressed) movePlayer(-strafeDx, -strafeDy)
        if (KeyEvent.VK_D in keysPressed) movePlayer(strafeDx, strafeDy)

        if (KeyEvent.VK_LEFT in keysPressed) playerAngle -= ROT_SPEED
        if (KeyEvent.VK_RIGHT in keysPressed) playerAngle += ROT_SPEED
        if (KeyEvent.VK_DOWN in keysPressed) playerPitch = min(Math.PI / 4, playerPitch + ROT_SPEED)
        if (KeyEvent.VK_UP in keysPressed) playerPitch = max(-Math.PI / 4, playerPitch - ROT_SPEED)

        // Zmiana FOV w stopniach
        if (KeyEvent.VK_P in keysPressed) FOV_HORIZONTAL = min(180.0, FOV_HORIZONTAL + 1.0)
        if (KeyEvent.VK_K in keysPressed) FOV_HORIZONTAL = max(10.0, FOV_HORIZONTAL - 1.0)
        if (KeyEvent.VK_O in keysPressed) FOV_HORIZONTAL = min(90.0, FOV_VERTICAL + 1.0)
        if (KeyEvent.VK_L in keysPressed) FOV_VERTICAL = max(10.0, FOV_VERTICAL - 1.0)

        // Zmiana liczby promieni
        if (KeyEvent.VK_I in keysPressed) {
            rayStepX = min(10, rayStepX + 1)
            rayStepY = min(10, rayStepY + 1)
        }
        if (KeyEvent.VK_J in keysPressed) {
            rayStepX = max(1, rayStepX - 1)
            rayStepY = max(1, rayStepY - 1)
        }

        if (playerAngle > 2 * Math.PI) playerAngle -= 2 * Math.PI
        if (playerAngle < 0) playerAngle += 2 * Math.PI
    }

    override fun paint(g: Graphics) {
        render3D()
        g.drawImage(image, 0, 0, this)
    }

    override fun update(g: Graphics) {
        paint(g)
    }

    private fun render3D() {
        val pixelBuffer = Array(SCREEN_WIDTH) { Array(SCREEN_HEIGHT) { Color.BLACK } }

        // Konwersja FOV na radiany
        val fovHorizontalRad = Math.toRadians(FOV_HORIZONTAL)
        val fovVerticalRad = Math.toRadians(FOV_VERTICAL)

        for (screenX in 0 until SCREEN_WIDTH step rayStepX) {
            val angleX = playerAngle - fovHorizontalRad / 2 + fovHorizontalRad * screenX / SCREEN_WIDTH
            val (distX, hitX, hitY, hitZ, voxelTypeX) = castRayHorizontal(angleX)

            for (screenY in 0 until SCREEN_HEIGHT step rayStepY) {
                val angleY = playerPitch - fovVerticalRad / 2 + fovVerticalRad * screenY / SCREEN_HEIGHT
                val (dist, voxelType) = castRayVertical(hitX, hitY, hitZ, angleY, distX, voxelTypeX)

                val brightness = (1.0 - dist / MAX_DEPTH).coerceIn(0.2, 1.0)
                val color = when (voxelType) {
                    1 -> Color((0.7 * brightness).toFloat(), (0.7 * brightness).toFloat(), (0.7 * brightness).toFloat())
                    2 -> Color(0f, brightness.toFloat(), 0f)
                    3 -> Color(0f, 0f, brightness.toFloat())
                    else -> Color.BLACK
                }

                for (x in screenX until minOf(screenX + rayStepX, SCREEN_WIDTH)) {
                    for (y in screenY until minOf(screenY + rayStepY, SCREEN_HEIGHT)) {
                        pixelBuffer[x][y] = color
                    }
                }
            }
        }

        for (x in 0 until SCREEN_WIDTH) {
            for (y in 0 until SCREEN_HEIGHT) {
                image.setRGB(x, y, pixelBuffer[x][y].rgb)
            }
        }
    }

    private fun castRayHorizontal(angle: Double): RayHit {
        var dist = 0.0
        val stepX = cos(angle)
        val stepY = sin(angle)

        while (dist < MAX_DEPTH) {
            val testX = playerX + stepX * dist
            val testY = playerY + stepY * dist
            val mx = (testX / VOXEL_SIZE).toInt()
            val my = (testY / VOXEL_SIZE).toInt()
            val mz = (playerZ / VOXEL_SIZE).toInt()

            if (mx !in 0 until MAP_SIZE_X || my !in 0 until MAP_SIZE_Y || mz !in 0 until MAP_SIZE_Z) {
                return RayHit(MAX_DEPTH, testX, testY, playerZ, 0)
            }

            val voxel = map[mz][my][mx]
            if (voxel != 0) {
                return RayHit(dist, testX, testY, playerZ, voxel)
            }

            dist += 1.0
        }
        return RayHit(MAX_DEPTH, playerX + stepX * MAX_DEPTH, playerY + stepY * MAX_DEPTH, playerZ, 0)
    }

    private fun castRayVertical(hitX: Double, hitY: Double, hitZ: Double, angleY: Double, distX: Double, voxelTypeX: Int): Pair<Double, Int> {
        if (voxelTypeX != 0 && abs(angleY) < 0.01) {
            return Pair(distX, voxelTypeX)
        }

        var dist = distX / cos(angleY).coerceAtLeast(0.01)
        val stepZ = tan(angleY)

        while (dist < MAX_DEPTH) {
            val testZ = hitZ + stepZ * (dist - distX)
            val mx = (hitX / VOXEL_SIZE).toInt()
            val my = (hitY / VOXEL_SIZE).toInt()
            val mz = (testZ / VOXEL_SIZE).toInt()

            if (mx !in 0 until MAP_SIZE_X || my !in 0 until MAP_SIZE_Y || mz !in 0 until MAP_SIZE_Z) {
                return Pair(MAX_DEPTH, 0)
            }

            val voxel = map[mz][my][mx]
            if (voxel != 0) {
                return Pair(dist, voxel)
            }

            dist += 1.0
        }
        return Pair(MAX_DEPTH, 0)
    }
}

data class RayHit(val dist: Double, val hitX: Double, val hitY: Double, val hitZ: Double, val voxelType: Int)

fun movePlayer(dx: Double, dy: Double) {
    val nextX = playerX + dx
    val nextY = playerY + dy
    if (isWalkable(nextX, playerY)) playerX = nextX
    if (isWalkable(playerX, nextY)) playerY = nextY
}

fun isWalkable(x: Double, y: Double): Boolean {
    val mx = (x / VOXEL_SIZE).toInt()
    val my = (y / VOXEL_SIZE).toInt()
    val mz = (playerZ / VOXEL_SIZE).toInt()
    return mx in 0 until MAP_SIZE_X &&
            my in 0 until MAP_SIZE_Y &&
            mz in 0 until MAP_SIZE_Z &&
            map[mz][my][mx] == 0
}
*/