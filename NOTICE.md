# 来源与修改声明

Annaskills 1.0.0 是基于 AuraSkills 的非官方适配版本。

- 上游项目：AuraSkills
- 上游仓库：https://github.com/Archy-X/AuraSkills
- 上游作者：Archy-X / Archy 及 AuraSkills 贡献者
- 基线提交：`569ecbdf`
- 许可证：GNU General Public License v3.0

本版本的主要修改包括：

1. 将插件显示名、构建产物与独立仓库标识调整为 Annaskills。
2. 将目标运行环境更新到 Minecraft 26.2、Paper API 26.2 与 Java 25。
3. 移除 FoliaLib 调度封装，直接使用 Paper 官方的全局区域、区域、实体和异步调度器。
4. 对玩家周期任务、延迟区块任务、命令异步回调及跨实体能力进行 Paper/Folia 双向兼容处理。
5. 保留原有 API 包、权限节点和兼容标识，尽量兼容 AuraSkills 的扩展生态。

本声明不表示 AuraSkills 原作者为 Annaskills 提供背书或官方支持。完整许可条款见
`LICENSE.md`；对应源代码应与任何二进制分发版本一同可获得。
