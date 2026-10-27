# Simple Package Box

一个适用于 Minecraft 26.3 的 Fabric 打包盒模组。

## 功能

- 120 格容量（12×9）
- 不可堆叠，手持右键打开
- 关闭界面时自动保存内容物
- 悬浮提示汇总内容物数量和种类
- 禁止装入打包盒、潜影盒、收纳袋以及其他背包/包裹类容器
- 打开时锁定打包盒本体槽位
- 3D 纸箱模型，带箱盖和马铃薯标签
- 合成配方：箱子 ×1 + 纸 ×4

## 环境

- Minecraft 26.3
- Fabric Loader 0.19.5 或更高版本
- Java 25
- Fabric API

## 构建

```bash
./gradlew build
```

构建产物位于：

```text
build/libs/simplebag-3.0.0.jar
```

## 致谢

感谢 **chinyou / 土豆服** 的 **土豆打包盒（ChinyouPackage）** 提供的灵感。  
本项目是面向 Fabric 26.3 的独立实现，没有复制原模组的代码或素材。

## 许可证

本项目使用 [CC0-1.0](LICENSE) 许可证开源。
