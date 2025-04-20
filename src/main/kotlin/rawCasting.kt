import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.math.*

const val SCREEN_WIDTH = 1280
const val SCREEN_HEIGHT = 720
const val MAP_SCALE = 64
const val FOV = Math.PI / 2  // 60 stopni
const val RAYS = 240*2
const val MAX_DEPTH = 800.0

val map = arrayOf(
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1),
    arrayOf(1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1),
    arrayOf(1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1),
    arrayOf(1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
)

class Player(var x: Double, var y: Double, var angle: Double)

class Raycaster3D : JPanel(), KeyListener {
    private val player = Player(100.0, 150.0, 0.0)
    private val image = BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB)

    init {
        preferredSize = Dimension(SCREEN_WIDTH, SCREEN_HEIGHT)
        isFocusable = true
        addKeyListener(this)
        Timer(16) { repaint() }.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = image.createGraphics()

        // Sufit i podłoga
        g2.color = Color.DARK_GRAY
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT / 2)
        g2.color = Color.GRAY
        g2.fillRect(0, SCREEN_HEIGHT / 2, SCREEN_WIDTH, SCREEN_HEIGHT / 2)

        // Raycasting
        for (i in 0 until RAYS) {
            val rayAngle = player.angle - FOV / 2 + (FOV / RAYS) * i
            val (hitX, hitY, distance) = castRay(rayAngle)

            // Poprawka na efekt "fish-eye"
            val correctedDist = distance * cos(rayAngle - player.angle)

            val wallHeight = (MAP_SCALE * 320 / correctedDist).toInt()
            val wallColor = getShadedColor(Color.BLUE, correctedDist)

            val x = i * (SCREEN_WIDTH / RAYS)
            val y = SCREEN_HEIGHT / 2 - wallHeight / 2

            g2.color = wallColor
            g2.fillRect(x, y, SCREEN_WIDTH / RAYS + 1, wallHeight)
        }

        // Rysujemy minimapę w rogu
        drawMiniMap(g2)

        g.drawImage(image, 0, 0, null)
    }

    private fun getShadedColor(base: Color, distance: Double): Color {
        val shade = (255.0 / (1.0 + distance * distance * 0.0001)).toInt().coerceIn(0, 255)
        return Color(0, 0, shade)
    }

    private fun castRay(angle: Double): Triple<Double, Double, Double> {
        val sinA = sin(angle)
        val cosA = cos(angle)
        var depth = 0.0

        while (depth < MAX_DEPTH) {
            val tx = player.x + cosA * depth
            val ty = player.y + sinA * depth

            val mapX = (tx / MAP_SCALE).toInt()
            val mapY = (ty / MAP_SCALE).toInt()

            if (mapY !in map.indices || mapX !in map[0].indices) break

            if (map[mapY][mapX] == 1) {
                return Triple(tx, ty, depth)
            }
            depth += 1.0
        }
        return Triple(player.x, player.y, MAX_DEPTH)
    }

    private fun drawMiniMap(g: Graphics2D) {
        val scale = 0.2
        for (y in map.indices) {
            for (x in map[y].indices) {
                g.color = if (map[y][x] == 1) Color.DARK_GRAY else Color.LIGHT_GRAY
                g.fillRect(
                    (x * MAP_SCALE * scale).toInt(),
                    (y * MAP_SCALE * scale).toInt(),
                    (MAP_SCALE * scale).toInt(),
                    (MAP_SCALE * scale).toInt()
                )
                g.color = Color.BLACK
                g.drawRect(
                    (x * MAP_SCALE * scale).toInt(),
                    (y * MAP_SCALE * scale).toInt(),
                    (MAP_SCALE * scale).toInt(),
                    (MAP_SCALE * scale).toInt()
                )
            }
        }

        // Gracz na minimapie
        g.color = Color.RED
        val px = (player.x * scale).toInt()
        val py = (player.y * scale).toInt()
        g.fillOval(px - 3, py - 3, 6, 6)

        val dx = cos(player.angle) * 10
        val dy = sin(player.angle) * 10
        g.drawLine(px, py, (px + dx).toInt(), (py + dy).toInt())
    }

    override fun keyPressed(e: KeyEvent) {
        val moveSpeed = 5.0
        val rotSpeed = 0.08

        when (e.keyCode) {
            KeyEvent.VK_W -> move(cos(player.angle) * moveSpeed, sin(player.angle) * moveSpeed)
            KeyEvent.VK_S -> move(-cos(player.angle) * moveSpeed, -sin(player.angle) * moveSpeed)
            KeyEvent.VK_A -> player.angle -= rotSpeed
            KeyEvent.VK_D -> player.angle += rotSpeed
        }
    }

    private fun move(dx: Double, dy: Double) {
        val newX = player.x + dx
        val newY = player.y + dy
        val mapX = (newX / MAP_SCALE).toInt()
        val mapY = (newY / MAP_SCALE).toInt()
        if (map[mapY][mapX] == 0) {
            player.x = newX
            player.y = newY
        }
    }

    override fun keyReleased(e: KeyEvent?) {}
    override fun keyTyped(e: KeyEvent?) {}
}

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Raycasting 3D in Kotlin")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.add(Raycaster3D())
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}
