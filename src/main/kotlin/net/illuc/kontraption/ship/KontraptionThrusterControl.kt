package net.illuc.kontraption.ship

import net.illuc.kontraption.ThrusterInterface
import net.illuc.kontraption.util.toBlockPos
import net.illuc.kontraption.util.toJOML
import net.minecraft.core.BlockPos
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.*
import java.util.concurrent.CopyOnWriteArrayList

class KontraptionThrusterControl : ShipForcesInducer {
    data class Thruster(val position: Vector3i, val forceDirection: Vector3d, val forceStrength: Double, val thruster: ThrusterInterface)

    private val thrusters = CopyOnWriteArrayList<Thruster>()

    val thrusterStrength = 100000.0

    override fun applyForces(physShip: PhysShip) {
        thrusters.forEach {
            val (position, forceDirection, forceStrength, be) = it
            //be.enable()
            if (forceStrength != 0.0){
                val tForce = physShip.transform.shipToWorld.transformDirection(forceDirection, Vector3d())
                //val tPos2 = position.toDouble().add(0.5, 0.5, 0.5).sub(physShip.transform.positionInShip)
                val tPos = Vector3d(0.0, 0.0, 0.0) //position.toDouble().add(0.5, 0.5, 0.5).sub(physShip.transform.positionInShip)

                if (forceDirection.isFinite) {
                    val forceFinal = forceStrength*thrusterStrength
                    if (forceFinal > 0){
                        be.powered = true
                        physShip.applyInvariantForceToPos(tForce.mul(forceFinal), tPos)
                    }
                }
            }else{
                be.powered = false
            }
        }
    }

    fun addThruster(pos: BlockPos, force: Vector3d, tier: Double, thruster: ThrusterInterface) {
        thrusters.add(Thruster(pos.toJOML(), force, tier, thruster))
    }

    fun removeThruster(pos: BlockPos, force: Vector3d, tier: Double, thruster: ThrusterInterface) {
        thrusters.remove(Thruster(pos.toJOML(), force, tier, thruster))
    }

    fun stopThruster(pos: BlockPos) {
        thrusters.removeAll { it.position == pos.toJOML() }
    }

    fun thrusterControlAll(forceDirection: Vector3d, power: Double) {
        //TODO you want prob a hashmap with directions as keys
        thrusters.forEach {
            if (it.forceDirection == forceDirection){
                val (pos, forceDir, tier, be) = it
                stopThruster(pos.toBlockPos())
                addThruster(pos.toBlockPos(), forceDir, power*it.thruster.thrusterPower, be)
                //println("INSANITY " + tier + " " + power*it.thruster.thrusterPower)
            }
        }
    }

    companion object {
        fun getOrCreate(ship: ServerShip): KontraptionThrusterControl {
            return ship.getAttachment<KontraptionThrusterControl>()
                ?: KontraptionThrusterControl().also { ship.saveAttachment(it) }
        }
    }
}