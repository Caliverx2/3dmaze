import java.awt.*
import java.awt.event.*
import javax.swing.*
import kotlin.math.*
/*
const val SCREEN_WIDTH = 800
const val SCREEN_HEIGHT = 600
const val MAP_SCALE = 64
const val FOV = Math.PI / 2  // 90 stopni
const val RAYS = 120
const val MAX_DEPTH = 500.0

val map = arrayOf(
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(1, 0, 0, 0, 0, 0, 0, 1),
    arrayOf(1, 0, 1, 0, 1, 0, 0, 1),
    arrayOf(1, 0, 1, 0, 1, 0, 0, 1),
    arrayOf(1, 0, 0, 0, 0, 0, 0, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1),
)

class Player(var x: Double, var y: Double, var angle: Double)

class RaycastVisualizer : JPanel(), KeyListener {
    private val player = Player(100.0, 150.0, 0.0)

    init {
        preferredSize = Dimension(SCREEN_WIDTH, SCREEN_HEIGHT)
        isFocusable = true
        addKeyListener(this)
        Timer(16) { repaint() }.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D

        drawMap(g2)
        drawPlayer(g2)
        drawRays(g2)
    }

    private fun drawMap(g: Graphics2D) {
        for (y in map.indices) {
            for (x in map[y].indices) {
                if (map[y][x] == 1) {
                    g.color = Color.DARK_GRAY
                    g.fillRect(x * MAP_SCALE, y * MAP_SCALE, MAP_SCALE, MAP_SCALE)
                } else {
                    g.color = Color.LIGHT_GRAY
                    g.fillRect(x * MAP_SCALE, y * MAP_SCALE, MAP_SCALE, MAP_SCALE)
                }
                g.color = Color.BLACK
                g.drawRect(x * MAP_SCALE, y * MAP_SCALE, MAP_SCALE, MAP_SCALE)
            }
        }
    }

    private fun drawPlayer(g: Graphics2D) {
        g.color = Color.RED
        g.fillOval((player.x - 5).toInt(), (player.y - 5).toInt(), 10, 10)
        val dirX = cos(player.angle) * 20
        val dirY = sin(player.angle) * 20
        g.drawLine(player.x.toInt(), player.y.toInt(), (player.x + dirX).toInt(), (player.y + dirY).toInt())
    }

    private fun drawRays(g: Graphics2D) {
        for (i in 0 until RAYS) {
            val rayAngle = player.angle - FOV / 2 + FOV * (i.toDouble() / RAYS)
            val hit = castRay(rayAngle)

            g.color = Color.BLUE
            g.drawLine(
                player.x.toInt(), player.y.toInt(),
                hit.first.toInt(), hit.second.toInt()
            )

            g.color = Color.GREEN
            g.fillOval(hit.first.toInt() - 2, hit.second.toInt() - 2, 4, 4)
        }
    }

    private fun castRay(angle: Double): Pair<Double, Double> {
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
                return Pair(tx, ty)
            }
            depth += 1.0
        }
        return Pair(player.x, player.y) // fallback jeśli nie trafi nic
    }

    override fun keyPressed(e: KeyEvent) {
        val moveSpeed = 5.0
        val rotSpeed = 0.1
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
        val frame = JFrame("Raycast Collision Visualizer")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.add(RaycastVisualizer())
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}
*/