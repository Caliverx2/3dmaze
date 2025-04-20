package org.lewapnoob
/*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.math.*

const val SCREEN_WIDTH = 1280
const val SCREEN_HEIGHT = 720
const val MAP_SIZE_X = 8
const val MAP_SIZE_Y = 8
const val MAP_SIZE_Z = 4
const val VOXEL_SIZE = 64.0
const val PLAYER_SPEED = 20.0
const val ROT_SPEED = 0.05
const val MAX_DEPTH = 1000.0

// Początkowe wartości FOV i kroku promieni
var FOV_HORIZONTAL = Math.PI / 3*1.76
var FOV_VERTICAL = Math.PI / 4*1.76
var rayStepX = 1 // Krok promieni w osi X (1 = promień na każdy piksel)
var rayStepY = 1 // Krok promieni w osi Y

// Mapa 3D: 0 = pustka, 1 = ściana, 2 = podłoga, 3 = sufit
val map = Array(MAP_SIZE_Z) { z ->
    Array(MAP_SIZE_Y) { y ->
        IntArray(MAP_SIZE_X) { x ->
            when {
                // Podłoga na z=0
                z == 0 -> 2
                // Sufit na z=3
                z == MAP_SIZE_Z - 1 -> 3
                // Ściany na krawędziach
                z > 0 && z < MAP_SIZE_Z - 1 && (x == 0 || x == MAP_SIZE_X - 1 || y == 0 || y == MAP_SIZE_Y - 1) -> 1
                // Wewnętrzna ściana (przykładowa)
                z == 1 && x == 4 && y == 4 -> 1
                else -> 0
            }
        }
    }
}

var playerX = 2.5 * VOXEL_SIZE
var playerY = 2.5 * VOXEL_SIZE
var playerZ = 1.5 * VOXEL_SIZE // Gracz na wysokości 1.5 voksela
var playerAngle = 0.0 // Kąt w poziomie (radiany)
var playerPitch = 0.0 // Kąt w pionie (radiany, patrzenie góra/dół)

fun main() {
    if (!glfwInit()) throw RuntimeException("GLFW init failed")
    val window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "raycasting-3dx(3d)-OpenGL.kt", NULL, NULL)
    glfwMakeContextCurrent(window)
    glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE)
    GL.createCapabilities()

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glOrtho(0.0, SCREEN_WIDTH.toDouble(), SCREEN_HEIGHT.toDouble(), 0.0, -1.0, 1.0)
    glMatrixMode(GL_MODELVIEW)

    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT)

        handleInput(window)
        render3D()

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}

fun handleInput(window: Long) {
    // Ruch gracza
    val dx = cos(playerAngle) * PLAYER_SPEED
    val dy = sin(playerAngle) * PLAYER_SPEED
    val strafeDx = cos(playerAngle + Math.PI / 2) * PLAYER_SPEED
    val strafeDy = sin(playerAngle + Math.PI / 2) * PLAYER_SPEED

    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) movePlayer(dx, dy)
    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) movePlayer(-dx, -dy)
    if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) movePlayer(-strafeDx, -strafeDy)
    if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) movePlayer(strafeDx, strafeDy)

    // Obrót w poziomie
    if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) playerAngle -= ROT_SPEED
    if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) playerAngle += ROT_SPEED

    // Patrzenie w górę/w dół
    if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) playerPitch = min(Math.PI / 4, playerPitch + ROT_SPEED)
    if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) playerPitch = max(-Math.PI / 4, playerPitch - ROT_SPEED)

    // Zmiana FOV poziomego
    if (glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) FOV_HORIZONTAL = min(Math.PI, FOV_HORIZONTAL + 0.01)
    if (glfwGetKey(window, GLFW_KEY_K) == GLFW_PRESS) FOV_HORIZONTAL = max(0.1, FOV_HORIZONTAL - 0.01)

    // Zmiana FOV pionowego
    if (glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS) FOV_VERTICAL = min(Math.PI / 2, FOV_VERTICAL + 0.01)
    if (glfwGetKey(window, GLFW_KEY_L) == GLFW_PRESS) FOV_VERTICAL = max(0.1, FOV_VERTICAL - 0.01)

    // Zmiana liczby promieni (krok promieni)
    if (glfwGetKey(window, GLFW_KEY_I) == GLFW_PRESS) {
        rayStepX = min(10, rayStepX + 1)
        rayStepY = min(10, rayStepY + 1)
    }
    if (glfwGetKey(window, GLFW_KEY_J) == GLFW_PRESS) {
        rayStepX = max(1, rayStepX - 1)
        rayStepY = max(1, rayStepY - 1)
    }

    // Normalizacja kąta
    if (playerAngle > 2 * Math.PI) playerAngle -= 2 * Math.PI
    if (playerAngle < 0) playerAngle += 2 * Math.PI
}

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

fun render3D() {
    // Bufor do przechowywania wyników promieni dla interpolacji
    val pixelBuffer = Array(SCREEN_WIDTH) { Array(SCREEN_HEIGHT) { floatArrayOf(0f, 0f, 0f) } }

    // Wysyłanie promieni z krokiem rayStepX, rayStepY
    for (screenX in 0 until SCREEN_WIDTH step rayStepX) {
        val angleX = playerAngle - FOV_HORIZONTAL / 2 + FOV_HORIZONTAL * screenX / SCREEN_WIDTH
        val (distX, hitX, hitY, hitZ, voxelTypeX) = castRayHorizontal(angleX)

        for (screenY in 0 until SCREEN_HEIGHT step rayStepY) {
            val angleY = playerPitch - FOV_VERTICAL / 2 + FOV_VERTICAL * screenY / SCREEN_HEIGHT
            val (dist, voxelType) = castRayVertical(hitX, hitY, hitZ, angleY, distX, voxelTypeX)

            // Kolor na podstawie typu voksela i odległości
            val brightness = (1.0 - dist / MAX_DEPTH).coerceIn(0.2, 1.0).toFloat()
            val color = when (voxelType) {
                1 -> floatArrayOf(0.7f * brightness, 0.7f * brightness, 0.7f * brightness) // Ściana (szary)
                2 -> floatArrayOf(0.0f, brightness, 0.0f) // Podłoga (zielony)
                3 -> floatArrayOf(0.0f, 0.0f, brightness) // Sufit (niebieski)
                else -> floatArrayOf(0.0f, 0.0f, 0.0f) // Pustka (czarny)
            }

            // Wypełnij piksele w obszarze rayStepX x rayStepY
            for (x in screenX until minOf(screenX + rayStepX, SCREEN_WIDTH)) {
                for (y in screenY until minOf(screenY + rayStepY, SCREEN_HEIGHT)) {
                    pixelBuffer[x][y] = color
                }
            }
        }
    }

    // Rysowanie bufora pikseli
    glBegin(GL_POINTS)
    for (x in 0 until SCREEN_WIDTH) {
        for (y in 0 until SCREEN_HEIGHT) {
            glColor3fv(pixelBuffer[x][y])
            glVertex2i(x, y)
        }
    }
    glEnd()
}

data class RayHit(val dist: Double, val hitX: Double, val hitY: Double, val hitZ: Double, val voxelType: Int)

fun castRayHorizontal(angle: Double): RayHit {
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

fun castRayVertical(hitX: Double, hitY: Double, hitZ: Double, angleY: Double, distX: Double, voxelTypeX: Int): Pair<Double, Int> {
    if (voxelTypeX != 0 && abs(angleY) < 0.01) {
        // Jeśli trafiliśmy ścianę w poziomie i patrzymy prawie prosto, zwróć ścianę
        return Pair(distX, voxelTypeX)
    }

    var dist = distX / cos(angleY).coerceAtLeast(0.01) // Korygujemy odległość dla perspektywy
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
*/