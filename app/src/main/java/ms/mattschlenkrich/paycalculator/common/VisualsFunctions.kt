package ms.mattschlenkrich.paycalculator.common

import android.graphics.Color
import java.util.Random

class VisualsFunctions {
    fun getRandomColorInt(): Int {
        val random = Random()
        return Color.argb(
            120, random.nextInt(256), random.nextInt(256), random.nextInt(256)
        )
    }
}