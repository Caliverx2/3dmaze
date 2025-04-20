package org.lewapnoob
/*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.math.*
import kotlin.properties.Delegates

const val SCREEN_WIDTH = 1280
const val SCREEN_HEIGHT = 720
const val TILE_SIZE = 64 // Rozmiar kostki w świecie gry (dla raycastingu i kolizji)
const val MINIMAP_TILE_SIZE = 10 // Rozmiar kostki na minimapie (w pikselach)
const val MAX_DEPTH = 800.0
const val ENEMY_SPEED = 1.2
const val SHOT_COOLDOWN = 500
var FOV = Math.PI / 3
var showMiniMap = true
var inMenu = true
var lastShotTime = System.currentTimeMillis()

val map = arrayOf(
    intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
    intArrayOf(0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1),
    intArrayOf(1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1),
    intArrayOf(1,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,1,0,1),
    intArrayOf(1,1,1,1,1,1,1,0,1,0,1,0,1,1,1,0,1,1,1,0,1),
    intArrayOf(1,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0,0,0,1),
    intArrayOf(1,0,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,0,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,0,1,0,1),
    intArrayOf(1,1,1,0,1,0,1,0,1,1,1,0,1,0,1,0,1,0,1,1,1),
    intArrayOf(1,0,0,0,1,0,1,0,0,0,1,0,0,0,1,0,1,0,0,0,1),
    intArrayOf(1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,0,1),
    intArrayOf(1,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,1,1,0,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,0,0,1,0,1,0,1,0,1,0,0,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,1,0,0,0,1,0,1,0,0,0,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,0,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,1,0,0,0,0,0,1,0,1,0,1,0,1),
    intArrayOf(1,0,1,0,1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,0,1),
    intArrayOf(1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0),
    intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1)
)

data class Enemy(var x: Double, var y: Double, var health: Int = 100)
val enemies = mutableListOf(
    Enemy(300.0, 350.0),
    Enemy(250.0, 300.0),
)

var playerX = 300.0
var playerY = 250.0
var playerAngle = 0.0
var playerHealth = 100
var wallTextureId by Delegates.notNull<Int>()
var enemyTextureId by Delegates.notNull<Int>()

fun main() {
    if (!glfwInit()) throw RuntimeException("GLFW init failed")
    val window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Raycaster Kotlin + LWJGL", NULL, NULL)
    glfwMakeContextCurrent(window)
    glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE)
    GL.createCapabilities()

    wallTextureId = loadTexture("src/main/resources/wall.png")
    enemyTextureId = loadTexture("src/main/resources/enemy.png")

    while (!glfwWindowShouldClose(window)) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()
        glOrtho(0.0, SCREEN_WIDTH.toDouble(), SCREEN_HEIGHT.toDouble(), 0.0, -1.0, 1.0)

        if (inMenu) {
            renderMenu()
            if (glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS) inMenu = false
            if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) glfwSetWindowShouldClose(window, true)
        } else {
            handleInput(window)
            updateEnemies()
            renderWalls()
            renderEnemies()
            renderHUD()
            renderMiniMap()
        }

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}

fun handleInput(window: Long) {
    val moveSpeed = 4.0
    val rotSpeed = 0.05
    val dx = cos(playerAngle) * moveSpeed
    val dy = sin(playerAngle) * moveSpeed

    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) move(dx, dy)
    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) move(-dx, -dy)
    if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) move(-dy, dx)
    if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) move(dy, -dx)

    if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) playerAngle -= rotSpeed
    if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) playerAngle += rotSpeed

    if (glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) FOV = min(Math.PI, FOV + 0.01)
    if (glfwGetKey(window, GLFW_KEY_K) == GLFW_PRESS) FOV = max(0.1, FOV - 0.01)

    if (glfwGetKey(window, GLFW_KEY_M) == GLFW_PRESS) showMiniMap = !showMiniMap
    if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) shoot()
}

fun move(dx: Double, dy: Double) {
    val nextX = playerX + dx
    val nextY = playerY + dy
    if (walkable(nextX, playerY)) playerX = nextX
    if (walkable(playerX, nextY)) playerY = nextY
}

fun walkable(x: Double, y: Double): Boolean {
    val mx = (x / TILE_SIZE).toInt()
    val my = (y / TILE_SIZE).toInt()
    return map.getOrNull(my)?.getOrNull(mx) == 0
}

fun shoot() {
    val now = System.currentTimeMillis()
    if (now - lastShotTime < SHOT_COOLDOWN) return
    lastShotTime = now

    for (enemy in enemies) {
        val dx = enemy.x - playerX
        val dy = enemy.y - playerY
        val dist = sqrt(dx * dx + dy * dy)
        var angle = atan2(dy, dx) - playerAngle
        if (angle < -Math.PI) angle += 2 * Math.PI
        if (angle > Math.PI) angle -= 2 * Math.PI
        if (abs(angle) < 0.1 && dist < 200 && canSee(playerX, playerY, enemy.x, enemy.y)) {
            enemy.health -= 50
            println("Enemy hit! HP: ${enemy.health}")
        }
    }
}

fun updateEnemies() {
    for (enemy in enemies) {
        if (enemy.health <= 0) continue

        val ex = (enemy.x / TILE_SIZE).toInt()
        val ey = (enemy.y / TILE_SIZE).toInt()
        val px = (playerX / TILE_SIZE).toInt()
        val py = (playerY / TILE_SIZE).toInt()

        val dx = px - ex
        val dy = py - ey

        val stepX = when {
            dx > 0 -> ENEMY_SPEED
            dx < 0 -> -ENEMY_SPEED
            else -> 0.0
        }

        val stepY = when {
            dy > 0 -> ENEMY_SPEED
            dy < 0 -> -ENEMY_SPEED
            else -> 0.0
        }

        val nextX = enemy.x + stepX
        val nextY = enemy.y + stepY

        if (isFree(nextX, enemy.y, enemy)) enemy.x = nextX
        if (isFree(enemy.x, nextY, enemy)) enemy.y = nextY

        if (hypot(enemy.x - playerX, enemy.y - playerY) < 20) {
            playerHealth -= 1
            if (playerHealth <= 0) {
                println("YOU DIED 💀")
                playerHealth = 0
            }
        }
    }
}

fun isFree(x: Double, y: Double, current: Enemy): Boolean {
    if (!walkable(x, y)) return false
    for (other in enemies) {
        if (other != current && other.health > 0) {
            val dist = hypot(other.x - x, other.y - y)
            if (dist < 20.0) return false
        }
    }
    return true
}

fun canSee(px: Double, py: Double, ex: Double, ey: Double): Boolean {
    val dx = ex - px
    val dy = ey - py
    val steps = max(abs(dx), abs(dy)).toInt()
    for (i in 0..steps) {
        val x = px + dx * i / steps
        val y = py + dy * i / steps
        if (!walkable(x, y)) return false
    }
    return true
}

fun renderWalls() {
    glEnable(GL_TEXTURE_2D)
    glBindTexture(GL_TEXTURE_2D, wallTextureId)

    for (ray in 0 until SCREEN_WIDTH) {
        val angle = playerAngle - FOV / 2 + FOV * ray / SCREEN_WIDTH
        val dist = castRay(angle)
        val corrected = dist * cos(angle - playerAngle)
        val height = min((TILE_SIZE * SCREEN_HEIGHT / max(corrected, 0.1)).toInt(), SCREEN_HEIGHT * 2)
        val start = max(0, SCREEN_HEIGHT / 2 - height / 2)
        val end = min(SCREEN_HEIGHT, SCREEN_HEIGHT / 2 + height / 2)

        val hitX = playerX + cos(angle) * dist
        val hitY = playerY + sin(angle) * dist
        val tileX = hitX % TILE_SIZE
        val tileY = hitY % TILE_SIZE
        val mapX = (hitX / TILE_SIZE).toInt()
        val mapY = (hitY / TILE_SIZE).toInt()

        val texX = if (abs(hitX - mapX * TILE_SIZE - TILE_SIZE / 2.0) > abs(hitY - mapY * TILE_SIZE - TILE_SIZE / 2.0)) {
            (tileY / TILE_SIZE).coerceIn(0.0, 1.0)
        } else {
            (tileX / TILE_SIZE).coerceIn(0.0, 1.0)
        }

        val texYStart = ((SCREEN_HEIGHT / 2 - start).toDouble() / height).coerceIn(0.0, 1.0)
        val texYEnd = ((end - SCREEN_HEIGHT / 2).toDouble() / height + 0.5).coerceIn(0.0, 1.0)

        glBegin(GL_QUADS)
        glTexCoord2f(texX.toFloat(), texYStart.toFloat()); glVertex2i(ray, start)
        glTexCoord2f(texX.toFloat(), texYEnd.toFloat()); glVertex2i(ray, end)
        glTexCoord2f(texX.toFloat(), texYEnd.toFloat()); glVertex2i(ray + 1, end)
        glTexCoord2f(texX.toFloat(), texYStart.toFloat()); glVertex2i(ray + 1, start)
        glEnd()
    }

    glDisable(GL_TEXTURE_2D)
}

fun castRay(angle: Double): Double {
    val MIN_DIST = 0.1
    var dist = 0.0
    while (dist < MAX_DEPTH) {
        val testX = playerX + cos(angle) * dist
        val testY = playerY + sin(angle) * dist
        val mapX = (testX / TILE_SIZE).toInt()
        val mapY = (testY / TILE_SIZE).toInt()
        if (map.getOrNull(mapY)?.getOrNull(mapX) == 1) {
            return max(MIN_DIST, dist)
        }
        dist += 1.0
    }
    return MAX_DEPTH
}

fun renderEnemies() {
    for (enemy in enemies.filter { it.health > 0 }) {
        val dx = enemy.x - playerX
        val dy = enemy.y - playerY
        val dist = sqrt(dx * dx + dy * dy)
        var angle = atan2(dy, dx) - playerAngle
        if (angle < -Math.PI) angle += 2 * Math.PI
        if (angle > Math.PI) angle -= 2 * Math.PI

        val projX = ((angle + FOV / 2) / FOV * SCREEN_WIDTH).toInt()
        val size = ((500 / dist) * 50).toInt()
        val x = projX - size / 2
        val y = SCREEN_HEIGHT / 2 - size / 2
        val visible = projX in -size..(SCREEN_WIDTH + size)

        if (visible && dist < MAX_DEPTH && canSee(playerX, playerY, enemy.x, enemy.y)) {
            glEnable(GL_TEXTURE_2D)
            glBindTexture(GL_TEXTURE_2D, enemyTextureId)
            glBegin(GL_QUADS)
            glTexCoord2f(0f, 0f); glVertex2i(x, y)
            glTexCoord2f(1f, 0f); glVertex2i(x + size, y)
            glTexCoord2f(1f, 1f); glVertex2i(x + size, y + size)
            glTexCoord2f(0f, 1f); glVertex2i(x, y + size)
            glEnd()
            glDisable(GL_TEXTURE_2D)
        }
    }
}

fun renderHUD() {
    glColor3f(1f, 1f, 1f)
    glBegin(GL_QUADS)
    val barWidth = playerHealth * 2
    glVertex2i(10, 10)
    glVertex2i(10 + barWidth, 10)
    glVertex2i(10 + barWidth, 30)
    glVertex2i(10, 30)
    glEnd()

    glBegin(GL_LINES)
    glVertex2i(SCREEN_WIDTH / 2 - 5, SCREEN_HEIGHT / 2)
    glVertex2i(SCREEN_WIDTH / 2 + 5, SCREEN_HEIGHT / 2)
    glVertex2i(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 - 5)
    glVertex2i(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 5)
    glEnd()
}

fun renderMiniMap() {
    if (!showMiniMap) return
    val lineLength = 20 // Długość linii kierunku (w pikselach na minimapie)
    val margin = 10 // Margines od krawędzi ekranu

    // Przesunięcie minimapy do prawego dolnego rogu
    glPushMatrix()
    glTranslatef(
        (SCREEN_WIDTH - map[0].size * MINIMAP_TILE_SIZE - margin).toFloat(),
        (SCREEN_HEIGHT - map.size * MINIMAP_TILE_SIZE - margin).toFloat(),
        0f
    )

    // Rysowanie mapy
    for (y in map.indices) {
        for (x in map[y].indices) {
            val tile = map[y][x]
            glColor3f(if (tile == 1) 0.3f else 0.1f, 0.1f, 0.1f)
            glBegin(GL_QUADS)
            glVertex2i(x * MINIMAP_TILE_SIZE, y * MINIMAP_TILE_SIZE)
            glVertex2i((x + 1) * MINIMAP_TILE_SIZE, y * MINIMAP_TILE_SIZE)
            glVertex2i((x + 1) * MINIMAP_TILE_SIZE, (y + 1) * MINIMAP_TILE_SIZE)
            glVertex2i(x * MINIMAP_TILE_SIZE, (y + 1) * MINIMAP_TILE_SIZE)
            glEnd()
        }
    }

    // Rysowanie gracza
    glColor3f(0f, 1f, 0f)
    glBegin(GL_QUADS)
    val px = (playerX / TILE_SIZE * MINIMAP_TILE_SIZE).toInt()
    val py = (playerY / TILE_SIZE * MINIMAP_TILE_SIZE).toInt()
    glVertex2i(px - 2, py - 2)
    glVertex2i(px + 2, py - 2)
    glVertex2i(px + 2, py + 2)
    glVertex2i(px - 2, py + 2)
    glEnd()

    // Rysowanie linii kierunku (biały kolor)
    glColor3f(1f, 1f, 1f) // Biały kolor dla linii
    glBegin(GL_LINES)
    glVertex2i(px, py)
    val lineEndX = (px + cos(playerAngle) * lineLength).toInt()
    val lineEndY = (py + sin(playerAngle) * lineLength).toInt()
    glVertex2i(lineEndX, lineEndY)
    glEnd()

    // Przywrócenie macierzy transformacji
    glPopMatrix()
}

fun renderMenu() {
    glColor3f(1f, 1f, 1f)
    glBegin(GL_QUADS)
    glVertex2i(300, 150)
    glVertex2i(500, 150)
    glVertex2i(500, 180)
    glVertex2i(300, 180)
    glEnd()

    glBegin(GL_QUADS)
    glVertex2i(350, 250)
    glVertex2i(450, 250)
    glVertex2i(450, 280)
    glVertex2i(350, 280)
    glEnd()

    glBegin(GL_QUADS)
    glVertex2i(350, 320)
    glVertex2i(450, 320)
    glVertex2i(450, 350)
    glVertex2i(350, 350)
    glEnd()
}

fun loadTexture(path: String): Int {
    val texId = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, texId)

    MemoryStack.stackPush().use { stack ->
        val w = stack.mallocInt(1)
        val h = stack.mallocInt(1)
        val channels = stack.mallocInt(1)

        stbi_set_flip_vertically_on_load(false)
        val image = stbi_load(path, w, h, channels, 4) ?: error("Can't load image: $path")

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(), h.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        stbi_image_free(image)
    }

    return texId
}
*/