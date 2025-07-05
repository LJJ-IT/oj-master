# Online IDE 项目

## 简介
本项目是一个在线 IDE，支持 Java、Python 和 C++ 代码的编译和运行。用户可以在前端页面选择语言、输入代码并提交，后端会处理编译和执行请求，最后返回运行结果。

## 功能特性
- 支持多种编程语言：Java、Python、C++
- 代码编辑区域提供初始代码模板
- 显示运行耗时和编译状态
- 展示代码执行结果

## 项目结构
```plaintext
项目根目录
├── .gitignore
├── .idea/
├── document/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
└── target/
```

## 技术栈
- 前端：HTML、jQuery、Bootstrap、Layer
- 后端：Spring Boot
- 依赖管理：Maven

## 使用方法
1. 启动项目
2. 打开浏览器访问前端页面: localhost:8081
3. 选择编程语言
4. 在代码编辑区域输入代码
5. 点击“提交”按钮查看运行结果

## 注意事项
- 运行 C++ 代码需要确保系统已安装 `g++` 编译器。
- 项目中部分功能（如限时执行、输入参数）已注释，如需使用请取消注释。