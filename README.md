# High-Performance Trading System

# High-Performance Trading System

This project is a high-performance trading system that includes an Order Management System (OMS) module and a Matching Engine module. The project uses Aeron Cluster to achieve high availability and consistency, while the matching engine utilizes the Disruptor framework for high-performance order matching.

## Table of Contents

- [Project Overview](#project-overview)
- [System Architecture](#system-architecture)
- [Functional Modules](#functional-modules)
    - [Order Management System (OMS)](#order-management-system-oms)
    - [Matching Engine Module](#matching-engine-module)
- [Technology Stack](#technology-stack)
- [Environment Dependencies](#environment-dependencies)
- [Quick Start](#quick-start)
- [Configuration Guide](#configuration-guide)
- [Performance Optimization](#performance-optimization)
- [License](#license)

## Project Overview

This project aims to build a high-performance, low-latency trading system that supports high-concurrency order processing and matching. By using high-performance frameworks such as Aeron Cluster and Disruptor, the system ensures stability and performance.

## System Architecture

![System Architecture](docs/images/system_architecture.png)

## Functional Modules

### Order Management System (OMS)

**Features:**

- Receive and validate order requests (RESTful API).
- Maintain order lifecycle (new order, partially filled, fully filled, cancelled, etc.).
- Send orders to the matching engine for processing.

**Implementation Highlights:**

- Use Aeron Cluster to ensure high availability and data consistency.
- Validate order parameters and user account information.
- Efficient and reliable communication with the matching engine.

### Matching Engine Module

**Features:**

- Maintain buy and sell order books, match orders.
- Generate trade records, update order status.

**Implementation Highlights:**

- Use the Disruptor framework for high-performance order processing.
- Use efficient data structures to manage order books (e.g., `TreeMap`).
- Single-threaded matching logic to avoid lock contention.

## Technology Stack

- **Aeron Cluster**: High-performance, low-latency messaging framework, ensuring system high availability and data consistency.
- **Disruptor**: High-performance lock-free concurrent framework, suitable for low-latency message processing scenarios.
- **Spring Boot**: Simplify application development and configuration.
- **Docker Compose**: Containerized deployment, facilitating distributed system deployment and management.

## Environment Dependencies

- JDK 17 or above
- Maven 3.6 or above
- Docker and Docker Compose
- Nacos (for service registration and configuration management)

## Quick Start

### Clone the Project

```bash
git clone https://github.com/crypto-to-moon/trading-service.git
cd trading-service
```

### Compile the Project

```bash
mvn clean package
```

### Start the Service

```bash
./start.sh
```

### Send Order Requests

Use Postman or other tools to send order requests to the OMS module's RESTful API.

Example:

```http
POST http://localhost:8080/order
Content-Type: application/json

{
  "userId": 123,
  "symbol": "BTC/USD",
  "side": "BUY",
  "type": "LIMIT",
  "price": 30000,
  "amount": 0.5,
  "orderClientId": "123456"
}
```

## Configuration Guide

[To be completed]

## Performance Optimization

[To be completed]

## License

[To be completed]

---

## Conclusion

I hope this content helps you continue with the implementation of your project. If you have any questions or need further assistance, please don't hesitate to contact me. Good luck with your project!

---

本项目是一个高性能的交易系统，包括订单管理模块（OMS）和撮合引擎模块。项目使用 Aeron Cluster 实现高可用和一致性，撮合引擎使用 Disruptor 框架实现高性能的订单撮合。

## 目录

- [项目简介](#项目简介)
- [系统架构](#系统架构)
- [功能模块](#功能模块)
    - [订单管理模块（OMS）](#订单管理模块oms)
    - [撮合引擎模块](#撮合引擎模块)
- [技术选型](#技术选型)
- [环境依赖](#环境依赖)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [性能优化](#性能优化)
- [许可证](#许可证)

## 项目简介

本项目旨在构建一个高性能、低延迟的交易系统，支持高并发的订单处理和撮合。通过使用 Aeron Cluster 和 Disruptor 等高性能框架，保证系统的稳定性和性能。

## 系统架构

![System Architecture](docs/images/system_architecture.png)

## 功能模块

### 订单管理模块（OMS）

**功能：**

- 接收并验证订单请求（RESTful API）。
- 维护订单的生命周期（新订单、部分成交、完全成交、取消等）。
- 将订单发送给撮合引擎进行处理。

**实现要点：**

- 使用 Aeron Cluster 保证高可用性和数据一致性。
- 验证订单参数和用户账户信息。
- 与撮合引擎进行高效可靠的通信。

### 撮合引擎模块

**功能：**

- 维护买卖订单簿，撮合订单。
- 生成成交记录，更新订单状态。

**实现要点：**

- 使用 Disruptor 框架实现高性能的订单处理。
- 使用高效的数据结构管理订单簿（如 `TreeMap`）。
- 单线程处理撮合逻辑，避免锁竞争。

## 技术选型

- **Aeron Cluster**：高性能、低延迟的消息传递框架，保证系统的高可用性和数据一致性。
- **Disruptor**：高性能的无锁并发框架，适用于低延迟的消息处理场景。
- **Spring Boot**：简化应用程序的开发和配置。
- **Docker Compose**：容器化部署，方便系统的分布式部署和管理。

## 环境依赖

- JDK 17 及以上
- Maven 3.6 及以上
- Docker 和 Docker Compose
- Nacos（用于服务注册和配置管理）

## 快速开始

### 克隆项目

```bash
git clone https://github.com/crypto-to-moon/trading-service.git
cd trading-service
```

### 编译项目

```bash
mvn clean package
```

### 启动服务

```bash
./start.sh
```

### 发送订单请求

使用 Postman 或其他工具，发送订单请求到 OMS 模块的 RESTful API。

示例：

```http
POST http://localhost:8080/order
Content-Type: application/json

{
  "userId": 123,
  "symbol": "BTC/USD",
  "side": "BUY",
  "type": "LIMIT",
  "price": 30000,
  "amount": 0.5,
  "orderClientId": "123456"
}
```
--- 

## 结语

希望以上内容能帮助您继续完成项目的实现。如果您有任何疑问或需要进一步的帮助，请随时与我联系。祝您的项目顺利完成！