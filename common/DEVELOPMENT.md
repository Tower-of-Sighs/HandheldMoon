# 实体 entityData 光源参数说明

实体光源的同步数据定义在各 target 的 FullMoonEntity 中，common 只负责读取和解释。
服务器写入 entityData，Minecraft 自动同步给客户端；客户端只读，不应反向修改。

## 实体级字段

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| ANCHOR_POS | Optional<BlockPos> | 绑定的方块位置；为空表示没有锚点 |
| LAMP_BOUND | Boolean | true 为定向灯筒，false 为全向盈月 |
| LAMP_LUMINANCE | Integer | 灯筒当前工作亮度；关闭必须为 0 |
| LAMP_X_ROT | Float | 灯筒俯仰角，单位为度 |
| LAMP_Y_ROT | Float | 灯筒水平角，单位为度 |
| LIGHT_PROFILE | String | 完整 EntityLightProfile 的 JSON 字符串 |
| LIGHT_PROFILE_OVERRIDE | Boolean | true 表示使用自定义 profile，不跟随默认配置 |

## LIGHT_PROFILE 参数

LIGHT_PROFILE 通过 EntityLightProfile.toNetworkString() 编码，禁止手拼 JSON。
读取使用 EntityLightProfile.fromNetworkString()；空值或非法值应回退默认 profile。

| 参数 | 含义 |
| --- | --- |
| shape | POINT 全向光，CONE 定向锥光 |
| luminance | 光源点强度，范围 0..15；衰减曲线在距离 0 的值 |
| range | 最大光照半径，范围 0..64；距离达到 range 时光照必为 0 |
| innerAngle | 锥光内半角，弧度；内角内保持满强度 |
| outerAngle | 锥光外半角，弧度；必须不小于 innerAngle，超出后为 0 |
| realLight | 是否产生真实世界光照 |
| visibleCone | 是否允许客户端绘制可见光锥 |
| occlusion | 是否检查方块遮挡 |
| offsetX/Y/Z | 相对实体运行时光源位置的世界坐标偏移 |
| attenuationCurve | LINEAR、QUADRATIC、EXPONENTIAL、LOGARITHMIC、NONE |

光照强度统一为：
L(d) = luminance × curve.at(d / range)；d >= range 时为 0。
innerAngle 和 outerAngle 是半角弧度，不要与可见光锥的完整角度混用。

## 写入和持久化规则

未设置 LIGHT_PROFILE_OVERRIDE 时，服务器 tick 根据设备配置生成默认 profile 并写入 LIGHT_PROFILE。
调用 setLightProfile(profile) 先写 profile，再将 override 设为 true。
调用 clearLightProfileOverride() 将 override 设为 false，并恢复默认 profile。
LIGHT_PROFILE 和 LIGHT_PROFILE_OVERRIDE 还必须在实体 NBT 的 light_profile、
light_profile_override 字段中保存和读取，否则重载存档会丢失自定义参数。

