# Annaskills

Annaskills 是面向 Minecraft Paper 与 Folia 26.2 的 RPG 技能插件。项目基于
[AuraSkills](https://github.com/Archy-X/AuraSkills) 修改，在保留原有技能、属性、能力、
法力、战利品与菜单体系的基础上，将服务器线程调度迁移到 Paper 官方 Region、Entity、
Global Region 与 Async Scheduler API。

本项目不是 AuraSkills 官方版本。原项目由 Archy-X / Archy 开发，本适配版本依据
GNU General Public License v3.0 发布。

## 主要特性

- 同一个 JAR 自动兼容 Paper 26.2 与 Folia 26.2
- Folia 原生区域调度、实体调度与全局区域调度
- 玩家循环任务按玩家实体线程分发，避免跨区域访问
- 延迟区块操作按目标位置所属区域执行
- 群体伤害、钓鱼、射箭、作物生长、伐木等跨实体路径经过 Folia 安全处理
- 保留 `auraskills.*` 权限、API 包名和常用集成标识，降低现有生态迁移成本
- 支持 PlaceholderAPI、Vault、WorldGuard、PacketEvents 等原项目兼容钩子

## 运行环境

| 项目 | 要求 |
| --- | --- |
| Minecraft | 26.2 |
| 服务端 | Paper 26.2 或 Folia 26.2 |
| Java | 25 |
| 插件版本 | 1.0.0 |

不支持 Spigot/CraftBukkit。Annaskills 使用 Paper 官方调度 API，并由 Paper 在普通单主线程
环境下提供兼容实现、由 Folia 在区域化线程环境下提供原生实现。

## 安装

1. 使用 Java 25 启动 Paper 26.2 或 Folia 26.2。
2. 将 `Annaskills-1.0.0.jar` 放入服务器的 `plugins` 目录。
3. 启动服务器，插件数据将写入 `plugins/Annaskills`。
4. 按需修改配置后执行 `/skills reload` 或重启服务器。

从 AuraSkills 迁移时，请先备份原数据。由于插件目录名已经改为 `Annaskills`，需要由服主
确认并迁移原 `plugins/AuraSkills` 中的配置与数据；权限节点和 API 命名空间继续兼容上游。

## 构建

Windows：

```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-21"
.\gradlew.bat clean test shadowJar
```

Gradle 运行时可使用 Java 21，项目 Java Toolchain 会调用 Java 25 编译器。构建产物位于：

```text
build/libs/Annaskills-1.0.0.jar
```

## 项目结构

- `api`：平台无关公开 API
- `api-bukkit`：Bukkit/Paper API 适配层
- `common`：技能、用户、存储与通用业务逻辑
- `bukkit`：Paper/Folia 插件实现及最终 JAR
- `paper`：Paper 专用能力桥接

## 上游与许可证

Annaskills 基于 [Archy-X/AuraSkills](https://github.com/Archy-X/AuraSkills)，上游源码采用
[GPL-3.0](LICENSE.md)。本仓库继续以 GPL-3.0 许可分发，并保留上游版权、提交历史与来源说明。
详细修改范围见 [NOTICE.md](NOTICE.md)。分发修改后的二进制时，应同时提供对应源码及许可证。

AuraSkills 的官方文档仍可作为功能和配置参考，但其中不属于本仓库的商标、网站、下载渠道
及支持渠道均归原项目所有。
