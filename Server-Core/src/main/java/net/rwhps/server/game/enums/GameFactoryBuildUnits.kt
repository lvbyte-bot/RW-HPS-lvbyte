/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.enums

import net.rwhps.server.util.EnumUtils

/**
 * 游戏工厂创建的单位
 *
 * @date 2024/1/27 18:48
 * @author 梦醒了 (https://github.com/fgsqme)
 * @author Dr (dr@der.kim)
 */
enum class GameFactoryBuildUnits {
    u_combatEngineer,
    u_c_experimentalTank,
    u_nautilusSubmarineLand,
    u_experimentalHoverTank,
    u_experimentalDropship,
    u_experimentalSpider,
    u_fireBee,
    u_builder,
    u_scout,
    u_c_tank,
    u_hoverTank,
    u_c_artillery,
    u_missileTank,
    u_plasmaTank,
    u_heavyTank,
    u_heavyArtillery,
    u_heavyHoverTank,
    u_c_laserTank,
    u_c_mammothTank,
    u_lightGunship,
    u_c_interceptor,
    u_c_helicopter,
    u_spyDrone,
    u_dropship,
    u_gunShip,
    u_heavyInterceptor,
    u_amphibiousJet,
    u_aaBeamGunship,
    u_bomber,
    u_missileAirship,
    u_mechGun,
    u_mechMissile,
    u_mechEngineer,
    u_mechArtillery,
    u_mechBunker,
    u_mechMinigun,
    u_mechLaser,
    u_mechLightning,
    u_mechHeavyMissile,
    u_mechFlame,
    u_builderShip,
    u_gunBoat,
    u_lightSub,
    u_missileShip,
    u_hovercraft,
    u_battleShip,
    u_attackSubmarine,
    u_heavyAAShip,
    u_heavyBattleship,
    u_heavyMissileShip,
    u_heavySub,
    u_nautilusSubmarine,
    u_experiementalCarrier,
    _0, // 核弹 生产
    _1, // 核弹 发射
    c_turret_t2_gun_0,
    c_turret_t3_gun_0,
    c_turret_t1_artillery_1,
    c_turret_t2_flame_2,
    c_turret_t1_lightning_3,
    mechFactoryT2_2,
    c_antiAirTurretT2_0,
    c_antiAirTurretT3_0,
    antiAirTurretFlak_1,
    UNKNOWN;

    companion object {
        fun from(name: String): GameFactoryBuildUnits = EnumUtils.from(entries, name, UNKNOWN)!!

        fun from(index: Int): GameFactoryBuildUnits = EnumUtils.from(entries, index, UNKNOWN)
    }
}