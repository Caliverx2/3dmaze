package org.lewapnoob
/*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Map {
    val grid: Array<IntArray> = arrayOf(
        // wartości: 1-ściana, 0-pusta przestrzeń, 5-początek i koniec labiryntu
        intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
        intArrayOf(5,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1),
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
        intArrayOf(1,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,5),
        intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1)
    )
}

fun main() {
    // Inicjalizacja GLFW
    if (!glfwInit()) {
        throw IllegalStateException("Nie można zainicjalizować GLFW")
    }

    // Konfiguracja okna
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    val window = glfwCreateWindow(1280, 720, "FPS-maze-3d-OpenGL", MemoryUtil.NULL, MemoryUtil.NULL)
    if (window == MemoryUtil.NULL) {
        throw RuntimeException("Nie można utworzyć okna")
    }

    // Centrowanie okna
    val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
    glfwSetWindowPos(window, (vidMode.width() - 1280) / 2, (vidMode.height() - 720) / 2)

    // Ustawienie kontekstu OpenGL
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)
    GL.createCapabilities()

    // Ustawienia OpenGL
    glEnable(GL_DEPTH_TEST)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    // Ręczna konfiguracja perspektywy
    val fov = 90f
    val aspectRatio = 1280f / 720f
    val zNear = 0.1f
    val zFar = 100f
    val fH = (Math.tan(Math.toRadians(fov.toDouble()) / 2.0) * zNear).toFloat()
    val fW = fH * aspectRatio
    glFrustum(-fW.toDouble(), fW.toDouble(), -fH.toDouble(), fH.toDouble(), zNear.toDouble(), zFar.toDouble())
    glMatrixMode(GL_MODELVIEW)

    // Inicjalizacja mapy
    val map = Map()

    // Pozycja i rotacja kamery
    val blockScale = 1.0f // Rozmiar bloku: 2x2 jednostki
    var startX = 0f
    var startZ = 0f
    for (i in map.grid.indices) {
        for (j in map.grid[i].indices) {
            if (map.grid[i][j] == 5 && i == 1) { // Pierwszy punkt startowy
                startX = (j * 2 * blockScale) - (map.grid[0].size * blockScale) + blockScale
                startZ = (i * 2 * blockScale) - (map.grid.size * blockScale) + blockScale
            }
        }
    }
    var cameraX = startX
    val cameraY = 1f
    var cameraZ = startZ
    var yaw = 0f
    var pitch = 0f

    // Obsługa myszy
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    var lastX = 400.0
    var lastY = 300.0
    var firstMouse = true

    glfwSetCursorPosCallback(window, { _, xpos, ypos ->
        if (firstMouse) {
            lastX = xpos
            lastY = ypos
            firstMouse = false
        }

        val xoffset = xpos - lastX
        val yoffset = lastY - ypos
        lastX = xpos
        lastY = ypos

        val sensitivity = 0.1f
        yaw += (xoffset * sensitivity).toFloat()
        pitch -= (yoffset * sensitivity).toFloat()

        if (pitch > 89f) pitch = 89f
        if (pitch < -89f) pitch = -89f
    })

    // Główna pętla gry
    var lastTime = glfwGetTime()
    while (!glfwWindowShouldClose(window)) {
        // Obliczanie delty czasu
        val currentTime = glfwGetTime()
        val deltaTime = (currentTime - lastTime).toFloat()
        lastTime = currentTime

        // Czyszczenie ekranu
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glLoadIdentity()

        // Ręczna implementacja widoku kamery
        glRotatef(pitch, 1f, 0f, 0f)
        glRotatef(yaw, 0f, 1f, 0f)
        glTranslatef(-cameraX, -cameraY, -cameraZ)

        // Wektor kierunku kamery (tylko XZ dla ruchu poziomego)
        val dirX = cos(Math.toRadians(yaw.toDouble())).toFloat()
        val dirZ = sin(Math.toRadians(yaw.toDouble())).toFloat()

        // Normalizacja wektora kierunku
        val length = sqrt((dirX * dirX + dirZ * dirZ).toDouble()).toFloat()
        val forwardX = if (length > 0) dirX / length else 0f
        val forwardZ = if (length > 0) dirZ / length else 0f

        // Wektor prostopadły (dla ruchu bocznego)
        val rightX = -forwardZ
        val rightZ = forwardX

        // Ruch gracza
        val speed = 5f * deltaTime
        var newCameraX = cameraX
        var newCameraZ = cameraZ

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            newCameraX -= rightX * speed // Do przodu
            newCameraZ -= rightZ * speed
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            newCameraX += rightX * speed // Do tyłu
            newCameraZ += rightZ * speed
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            newCameraX -= forwardX * speed // W lewo
            newCameraZ -= forwardZ * speed
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            newCameraX += forwardX * speed // W prawo
            newCameraZ += forwardZ * speed
        }

        // Kolizje z każdą ścianą
        val playerRadius = 0.4f // Promień kolizji postaci
        var canMove = true

        for (i in map.grid.indices) {
            for (j in map.grid[i].indices) {
                if (map.grid[i][j] == 1) {
                    // Oblicz granice ściany proporcjonalnie do blockScale
                    val wallCenterX = (j * 2 * blockScale) - (map.grid[0].size * blockScale) + blockScale
                    val wallCenterZ = (i * 2 * blockScale) - (map.grid.size * blockScale) + blockScale
                    val wallMinX = wallCenterX - blockScale
                    val wallMaxX = wallCenterX + blockScale
                    val wallMinZ = wallCenterZ - blockScale
                    val wallMaxZ = wallCenterZ + blockScale

                    // Sprawdzenie kolizji z prostokątem ściany
                    val closestX = newCameraX.coerceIn(wallMinX, wallMaxX)
                    val closestZ = newCameraZ.coerceIn(wallMinZ, wallMaxZ)
                    val distanceX = newCameraX - closestX
                    val distanceZ = newCameraZ - closestZ
                    val distance = sqrt((distanceX * distanceX + distanceZ * distanceZ).toDouble()).toFloat()

                    if (distance < playerRadius) {
                        canMove = false
                        break
                    }
                }
            }
            if (!canMove) break
        }

        // Aktualizacja pozycji, jeśli ruch jest dozwolony
        if (canMove) {
            cameraX = newCameraX
            cameraZ = newCameraZ
        }

        // Renderowanie labiryntu
        renderMaze(map)

        // Swap buforów i obsługa zdarzeń
        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    // Sprzątanie
    glfwDestroyWindow(window)
    glfwTerminate()
}

// Funkcja renderująca labirynt
fun renderMaze(map: Map) {
    glBegin(GL_QUADS)

    val width = map.grid[0].size
    val height = map.grid.size
    val blockScale = 1.0f // Rozmiar bloku: 2x2 jednostki
    val halfWidth = width * blockScale // Skalowana połowa szerokości mapy
    val halfHeight = height * blockScale // Skalowana połowa wysokości mapy

    // Podłoga (y = 0)
    glColor3f(0.5f, 0.5f, 0.5f)
    glVertex3f(-halfWidth, 0f, -halfHeight)
    glVertex3f(halfWidth, 0f, -halfHeight)
    glVertex3f(halfWidth, 0f, halfHeight)
    glVertex3f(-halfWidth, 0f, halfHeight)

    // Sufit (y = 2)
    glColor3f(0.7f, 0.7f, 0.7f)
    glVertex3f(-halfWidth, 2f, -halfHeight)
    glVertex3f(-halfWidth, 2f, halfHeight)
    glVertex3f(halfWidth, 2f, halfHeight)
    glVertex3f(halfWidth, 2f, -halfHeight)

    // Ściany z proporcjonalnym pozycjonowaniem
    glColor3f(0.3f, 0.3f, 0.8f)
    for (i in 0 until height) {
        for (j in 0 until width) {
            if (map.grid[i][j] == 1) {
                // Oblicz centrum bloku proporcjonalnie do blockScale
                val x = (j * 2 * blockScale) - (map.grid[0].size * blockScale) + blockScale
                val z = (i * 2 * blockScale) - (map.grid.size * blockScale) + blockScale

                // Ściana przednia (z = z + blockScale)
                glVertex3f(x - blockScale, 0f, z + blockScale)
                glVertex3f(x + blockScale, 0f, z + blockScale)
                glVertex3f(x + blockScale, 2f, z + blockScale)
                glVertex3f(x - blockScale, 2f, z + blockScale)

                // Ściana tylna (z = z - blockScale)
                glVertex3f(x - blockScale, 0f, z - blockScale)
                glVertex3f(x - blockScale, 2f, z - blockScale)
                glVertex3f(x + blockScale, 2f, z - blockScale)
                glVertex3f(x + blockScale, 0f, z - blockScale)

                // Ściana lewa (x = x - blockScale)
                glVertex3f(x - blockScale, 0f, z - blockScale)
                glVertex3f(x - blockScale, 2f, z - blockScale)
                glVertex3f(x - blockScale, 2f, z + blockScale)
                glVertex3f(x - blockScale, 0f, z + blockScale)

                // Ściana prawa (x = x + blockScale)
                glVertex3f(x + blockScale, 0f, z - blockScale)
                glVertex3f(x + blockScale, 0f, z + blockScale)
                glVertex3f(x + blockScale, 2f, z + blockScale)
                glVertex3f(x + blockScale, 2f, z - blockScale)
            }
        }
    }

    glEnd()
}
*/