package com.ricrug.data

import com.ricrug.model.CelestialBody
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class OrbitVec3(val x: Float, val y: Float, val z: Float) {
  operator fun plus(o: OrbitVec3) = OrbitVec3(x + o.x, y + o.y, z + o.z)
  operator fun minus(o: OrbitVec3) = OrbitVec3(x - o.x, y - o.y, z - o.z)
  operator fun times(s: Float) = OrbitVec3(x * s, y * s, z * s)
  fun dot(o: OrbitVec3): Float = x * o.x + y * o.y + z * o.z
  fun cross(o: OrbitVec3): OrbitVec3 =
    OrbitVec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
  fun length(): Float = sqrt(x * x + y * y + z * z)
  fun normalized(): OrbitVec3 {
    val len = length().coerceAtLeast(1e-5f)
    return OrbitVec3(x / len, y / len, z / len)
  }
}

data class OrbitElements(
  val inclinationDeg: Float,
  val eccentricity: Float,
  val ascendingNodeDeg: Float,
  val periapsisDeg: Float,
  val initialMeanAnomalyDeg: Float,
)

object OrbitalMechanics {

  private val elementsById: Map<String, OrbitElements> =
    mapOf(
      "sun" to OrbitElements(0f, 0f, 0f, 0f, 0f),
      "mercury" to OrbitElements(inclinationDeg = 7.0f, eccentricity = 0.206f, ascendingNodeDeg = 48f, periapsisDeg = 29f, initialMeanAnomalyDeg = 25f),
      "venus" to OrbitElements(inclinationDeg = 3.4f, eccentricity = 0.007f, ascendingNodeDeg = 76f, periapsisDeg = 55f, initialMeanAnomalyDeg = 110f),
      "earth" to OrbitElements(inclinationDeg = 0.0f, eccentricity = 0.017f, ascendingNodeDeg = 0f, periapsisDeg = 102f, initialMeanAnomalyDeg = 200f),
      "moon" to OrbitElements(inclinationDeg = 5.15f, eccentricity = 0.055f, ascendingNodeDeg = 125f, periapsisDeg = 18f, initialMeanAnomalyDeg = 40f),
      "mars" to OrbitElements(inclinationDeg = 1.85f, eccentricity = 0.093f, ascendingNodeDeg = 49f, periapsisDeg = 286f, initialMeanAnomalyDeg = 305f),
      "jupiter" to OrbitElements(inclinationDeg = 1.3f, eccentricity = 0.049f, ascendingNodeDeg = 100f, periapsisDeg = 273f, initialMeanAnomalyDeg = 55f),
      "saturn" to OrbitElements(inclinationDeg = 2.5f, eccentricity = 0.057f, ascendingNodeDeg = 113f, periapsisDeg = 339f, initialMeanAnomalyDeg = 165f),
      "uranus" to OrbitElements(inclinationDeg = 0.8f, eccentricity = 0.046f, ascendingNodeDeg = 74f, periapsisDeg = 97f, initialMeanAnomalyDeg = 255f),
      "neptune" to OrbitElements(inclinationDeg = 1.8f, eccentricity = 0.009f, ascendingNodeDeg = 131f, periapsisDeg = 273f, initialMeanAnomalyDeg = 340f),
      "pluto" to OrbitElements(inclinationDeg = 17.2f, eccentricity = 0.249f, ascendingNodeDeg = 110f, periapsisDeg = 113f, initialMeanAnomalyDeg = 80f),
    )

  fun elementsFor(planetId: String): OrbitElements =
    elementsById[planetId] ?: OrbitElements(0f, 0f, 0f, 0f, 0f)

  /**
   * Computes a 3D orbital point on the planet's Keplerian ellipse tilted by its
   * real orbital inclination and ascending node relative to the Earth's ecliptic plane (y = 0).
   * [inclinationVisualBoost] subtly amplifies small tilts so planetary inclinations (1°..17°)
   * remain visually distinguishable in 3D space while preserving real ordering.
   */
  fun orbitPointAtAngle(
    body: CelestialBody,
    anomalyDeg: Float,
    inclinationVisualBoost: Float = 1.45f,
  ): OrbitVec3 {
    if (body.orbitDistanceScene <= 0f) return OrbitVec3(0f, 0f, 0f)
    val el = elementsFor(body.id)
    val a = body.orbitDistanceScene
    val e = el.eccentricity.coerceIn(0f, 0.45f)

    // True anomaly approximation via first-order Kepler equation E ≈ M + e*sin(M)
    val mRad = Math.toRadians(anomalyDeg.toDouble())
    val eccentricAnomaly = mRad + e * sin(mRad)
    val r = a * (1f - e * cos(eccentricAnomaly).toFloat())

    // Position in orbital plane before periapsis rotation
    val trueAnomaly = mRad + 2.0 * e * sin(mRad)
    val arg = trueAnomaly + Math.toRadians(el.periapsisDeg.toDouble())
    val xOrb = (r * cos(arg)).toFloat()
    val zOrb = (r * sin(arg)).toFloat()

    // Tilt by inclination around local X-axis (line of nodes), then rotate by ascending node around Y
    val incRad = Math.toRadians((el.inclinationDeg * inclinationVisualBoost).toDouble())
    val yTilted = (zOrb * sin(incRad)).toFloat()
    val zTilted = (zOrb * cos(incRad)).toFloat()

    val nodeRad = Math.toRadians(el.ascendingNodeDeg.toDouble())
    val cosN = cos(nodeRad).toFloat()
    val sinN = sin(nodeRad).toFloat()
    val worldX = xOrb * cosN - zTilted * sinN
    val worldZ = xOrb * sinN + zTilted * cosN

    return OrbitVec3(worldX, yTilted, worldZ)
  }

  fun bodyWorldPosition(
    body: CelestialBody,
    simTimeSec: Float,
    inclinationVisualBoost: Float = 1.45f,
  ): OrbitVec3 {
    if (body.id == "sun" || body.orbitDistanceScene <= 0f) return OrbitVec3(0f, 0f, 0f)
    val el = elementsFor(body.id)
    val anomalyDeg = el.initialMeanAnomalyDeg + simTimeSec * body.orbitSpeedDegPerSec
    val localOffset = orbitPointAtAngle(body, anomalyDeg, inclinationVisualBoost)
    if (body.id == "moon") {
      val earth = SolarSystemData.getPlanetById("earth")
      return bodyWorldPosition(earth, simTimeSec, inclinationVisualBoost) + localOffset
    }
    return localOffset
  }

  fun sampleOrbitLoop(
    body: CelestialBody,
    simTimeSec: Float,
    segments: Int = 64,
    inclinationVisualBoost: Float = 1.45f,
  ): List<OrbitVec3> {
    if (body.orbitDistanceScene <= 0f) return emptyList()
    val center =
      if (body.id == "moon") {
        bodyWorldPosition(SolarSystemData.getPlanetById("earth"), simTimeSec, inclinationVisualBoost)
      } else {
        OrbitVec3(0f, 0f, 0f)
      }
    return List(segments + 1) { i ->
      val deg = (i.toFloat() / segments) * 360f
      center + orbitPointAtAngle(body, deg, inclinationVisualBoost)
    }
  }
}
