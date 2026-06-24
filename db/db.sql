-- MySQL dump 10.13  Distrib 8.0.46, for Linux (aarch64)
--
-- Host: localhost    Database: minsu_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_availability`
--

DROP TABLE IF EXISTS `t_availability`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_availability` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `date` date NOT NULL COMMENT '锁定日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_house_date` (`house_id`,`date`),
  KEY `idx_house` (`house_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房态锁定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_availability`
--

LOCK TABLES `t_availability` WRITE;
/*!40000 ALTER TABLE `t_availability` DISABLE KEYS */;
INSERT INTO `t_availability` VALUES (1,1,'2026-07-20','2026-06-23 12:31:33'),(2,1,'2026-07-21','2026-06-23 12:31:33'),(3,3,'2026-08-01','2026-06-23 12:31:33');
/*!40000 ALTER TABLE `t_availability` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_city`
--

DROP TABLE IF EXISTS `t_city`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_city` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='城市配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_city`
--

LOCK TABLES `t_city` WRITE;
/*!40000 ALTER TABLE `t_city` DISABLE KEYS */;
INSERT INTO `t_city` VALUES (1,'厦门',1),(2,'青岛',2),(3,'三亚',3),(4,'成都',4),(5,'杭州',5),(6,'大理',6);
/*!40000 ALTER TABLE `t_city` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_favorite`
--

DROP TABLE IF EXISTS `t_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_house` (`user_id`,`house_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_favorite`
--

LOCK TABLES `t_favorite` WRITE;
/*!40000 ALTER TABLE `t_favorite` DISABLE KEYS */;
INSERT INTO `t_favorite` VALUES (1,2,2,'2026-06-23 12:31:33'),(2,2,4,'2026-06-23 12:31:33'),(3,3,1,'2026-06-23 12:31:33'),(4,3,3,'2026-06-23 12:31:33'),(5,4,2,'2026-06-23 12:31:33'),(6,4,5,'2026-06-23 12:31:33'),(13,1,7,'2026-06-24 07:02:44'),(14,1,8,'2026-06-24 09:45:50');
/*!40000 ALTER TABLE `t_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_house`
--

DROP TABLE IF EXISTS `t_house`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_house` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房源名称',
  `city` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所在城市',
  `price` decimal(10,2) NOT NULL COMMENT '每晚价格',
  `clean_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '清洁费',
  `type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: 单间/一居室/两居室/整套',
  `rooms` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '房间描述',
  `area` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '面积',
  `capacity` int NOT NULL DEFAULT '2' COMMENT '最大可住人数',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `host` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房东姓名',
  `cover_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片URL',
  `facilities` text COLLATE utf8mb4_unicode_ci COMMENT '配套设施标签，逗号分隔',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '房源介绍',
  `rule` text COLLATE utf8mb4_unicode_ci COMMENT '入住规则',
  `status` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ON' COMMENT '上架状态: ON-上架 OFF-下架',
  `rating` decimal(2,1) NOT NULL DEFAULT '5.0' COMMENT '平均评分',
  `sales` int NOT NULL DEFAULT '0' COMMENT '累计订单量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_city` (`city`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_house`
--

LOCK TABLES `t_house` WRITE;
/*!40000 ALTER TABLE `t_house` DISABLE KEYS */;
INSERT INTO `t_house` VALUES (1,'海边日落小屋','厦门',399.00,50.00,'整套','1室1厅1卫','60㎡',2,'厦门市思明区曾厝垵123号','李房东','/uploads/2026/06/24/new-houses/house-1.jpg','WiFi,空调,洗衣机,厨房','靠近海边的温馨小屋，步行5分钟到海滩，适合情侣度假。','14:00后入住，12:00前离店。禁止携带宠物。','ON',4.8,12,'2026-06-23 12:31:33','2026-06-24 10:16:42'),(2,'山景别墅','大理',888.00,100.00,'整套','3室2厅2卫','200㎡',6,'大理市古城区苍山脚下','王房东','/uploads/2026/06/24/new-houses/house-2.jpg','WiFi,空调,洗衣机,厨房,停车位,冰箱,阳台','苍山洱海全景别墅，独享山景，适合家庭聚会。','14:00后入住，12:00前离店。','ON',4.9,8,'2026-06-23 12:31:33','2026-06-24 10:16:42'),(3,'市中心公寓','杭州',299.00,40.00,'一居室','1室1厅1卫','45㎡',2,'杭州市西湖区文三路88号','陈房东','/uploads/2026/06/24/new-houses/house-3.jpg','WiFi,空调,洗衣机,电视,热水器','紧邻西湖景区，交通便利，地铁口50米。','14:00后入住，12:00前离店。','ON',4.6,25,'2026-06-23 12:31:33','2026-06-24 10:16:42'),(4,'海景大床房','三亚',599.00,60.00,'单间','1室1卫','35㎡',2,'三亚市海棠湾免税店旁','林房东','/uploads/2026/06/24/new-houses/house-4.jpg','WiFi,空调,电视,热水器,浴缸','一线海景房，推开窗就是大海，度假首选。','14:00后入住，12:00前离店。','ON',4.7,18,'2026-06-23 12:31:33','2026-06-24 10:16:42'),(5,'老城四合院','成都',459.00,50.00,'整套','2室1厅1卫','80㎡',4,'成都市青羊区宽窄巷子旁','赵房东','/uploads/2026/06/24/new-houses/house-5.jpg','WiFi,空调,洗衣机,厨房,电视,冰箱','传统四合院改造，闹中取静，体验老成都生活。','14:00后入住，12:00前离店。','ON',4.5,10,'2026-06-23 12:31:33','2026-06-24 10:16:42'),(6,'山间木屋','青岛',359.00,45.00,'两居室','2室1厅1卫','70㎡',4,'青岛市崂山区仰口风景区','孙房东','/uploads/2026/06/24/new-houses/house-6.jpg','WiFi,空调,洗衣机,厨房,停车位,阳台','崂山脚下木屋，远离城市喧嚣，亲近自然。','14:00后入住，12:00前离店。','OFF',4.4,5,'2026-06-23 12:31:33','2026-06-24 10:16:42'),(7,'海边日落小屋','厦门',399.00,50.00,'整套','1室1厅1卫','60㎡',2,'厦门市思明区曾厝垵123号','李房东','/uploads/2026/06/24/new-houses/house-1.jpg','WiFi,空调,洗衣机,厨房','靠近海边的温馨小屋，步行5分钟到海滩，适合情侣度假。','14:00后入住，12:00前离店。禁止携带宠物。','ON',4.8,13,'2026-06-24 06:58:35','2026-06-24 10:16:42'),(8,'山景别墅','大理',888.00,100.00,'整套','3室2厅2卫','200㎡',6,'大理市古城区苍山脚下','王房东','/uploads/2026/06/24/new-houses/house-2.jpg','WiFi,空调,洗衣机,厨房,停车位,冰箱,阳台','苍山洱海全景别墅，独享山景，适合家庭聚会。','14:00后入住，12:00前离店。','ON',4.9,8,'2026-06-24 06:58:35','2026-06-24 10:16:42'),(9,'市中心公寓','杭州',299.00,40.00,'一居室','1室1厅1卫','45㎡',2,'杭州市西湖区文三路88号','陈房东','/uploads/2026/06/24/new-houses/house-3.jpg','WiFi,空调,洗衣机,电视,热水器','紧邻西湖景区，交通便利，地铁口50米。','14:00后入住，12:00前离店。','ON',4.6,25,'2026-06-24 06:58:35','2026-06-24 10:16:42'),(10,'海景大床房','三亚',599.00,60.00,'单间','1室1卫','35㎡',2,'三亚市海棠湾免税店旁','林房东','/uploads/2026/06/24/new-houses/house-4.jpg','WiFi,空调,电视,热水器,浴缸','一线海景房，推开窗就是大海，度假首选。','14:00后入住，12:00前离店。','ON',4.7,18,'2026-06-24 06:58:35','2026-06-24 10:16:42'),(11,'老城四合院','成都',459.00,50.00,'整套','2室1厅1卫','80㎡',4,'成都市青羊区宽窄巷子旁','赵房东','/uploads/2026/06/24/new-houses/house-5.jpg','WiFi,空调,洗衣机,厨房,电视,冰箱','传统四合院改造，闹中取静，体验老成都生活。','14:00后入住，12:00前离店。','ON',4.5,10,'2026-06-24 06:58:35','2026-06-24 10:16:42'),(12,'山间木屋','青岛',359.00,45.00,'两居室','2室1厅1卫','70㎡',4,'青岛市崂山区仰口风景区','孙房东','/uploads/2026/06/24/new-houses/house-6.jpg','WiFi,空调,洗衣机,厨房,停车位,阳台','崂山脚下木屋，远离城市喧嚣，亲近自然。','14:00后入住，12:00前离店。','OFF',4.4,5,'2026-06-24 06:58:35','2026-06-24 10:16:42');
/*!40000 ALTER TABLE `t_house` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order` (
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `guest_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人姓名',
  `phone` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人手机号',
  `check_in` date NOT NULL COMMENT '入住日期',
  `check_out` date NOT NULL COMMENT '离店日期',
  `nights` int NOT NULL COMMENT '入住晚数',
  `guests` int NOT NULL COMMENT '入住人数',
  `amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
  `paid` tinyint NOT NULL DEFAULT '0' COMMENT '是否已支付: 0-否 1-是',
  `note` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_no`),
  KEY `idx_house` (`house_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_check_in` (`check_in`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_order`
--

LOCK TABLES `t_order` WRITE;
/*!40000 ALTER TABLE `t_order` DISABLE KEYS */;
INSERT INTO `t_order` VALUES ('MS202606100001',4,3,'李四','13999999999','2026-06-10','2026-06-12',2,2,1188.00,'REFUNDED',0,NULL,'2026-06-10 09:00:00','2026-06-23 12:31:33'),('MS202606150001',2,4,'王五','13777777777','2026-06-25','2026-06-28',3,3,2664.00,'REFUNDING',1,'临时有事需要退款','2026-06-15 11:00:00','2026-06-23 12:31:33'),('MS202606180001',5,2,'张三','13888888888','2026-06-18','2026-06-20',2,2,918.00,'CANCELLED',0,'行程变更','2026-06-18 10:00:00','2026-06-23 12:31:33'),('MS202606200001',1,2,'张三','13888888888','2026-07-01','2026-07-03',2,2,818.00,'REVIEWED',1,'需要加床','2026-06-20 10:00:00','2026-06-23 12:31:33'),('MS202606200002',3,2,'张三','13888888888','2026-07-10','2026-07-12',2,2,608.00,'COMPLETED',1,NULL,'2026-06-20 14:00:00','2026-06-23 12:31:33'),('MS202606210001',2,3,'李四','13999999999','2026-07-05','2026-07-08',3,4,2664.00,'COMPLETED',1,'家庭出行','2026-06-21 09:00:00','2026-06-24 10:03:04'),('MS202606220001',4,4,'王五','13777777777','2026-07-15','2026-07-17',2,2,1188.00,'CANCELLED',0,NULL,'2026-06-22 16:00:00','2026-06-23 12:32:24'),('MS202606230001',1,3,'李四','13999999999','2026-08-01','2026-08-03',2,2,818.00,'CANCELLED',0,'蜜月旅行','2026-06-23 08:00:00','2026-06-24 10:02:59'),('MS20260623210001904',2,2,'张三','13888888888','2026-08-10','2026-08-12',2,2,1846.00,'CANCELLED',0,'测试订单','2026-06-23 13:00:01','2026-06-23 13:00:10'),('MS20260624152122583',1,2,'测试','13800001111','2026-07-20','2026-07-22',2,2,818.00,'CANCELLED',0,NULL,'2026-06-24 07:21:22','2026-06-24 07:22:17'),('MS20260624180032256',7,1,'1','19000990099','2026-06-24','2026-07-08',14,1,5606.00,'CANCELLED',0,'','2026-06-24 10:00:32','2026-06-24 10:01:07');
/*!40000 ALTER TABLE `t_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_review`
--

DROP TABLE IF EXISTS `t_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联订单编号',
  `house_id` bigint NOT NULL COMMENT '关联房源ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `user_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示名称',
  `rating` int NOT NULL COMMENT '整体评分(1-5)',
  `clean_score` int NOT NULL COMMENT '卫生评分(1-5)',
  `location_score` int NOT NULL COMMENT '位置评分(1-5)',
  `service_score` int NOT NULL COMMENT '服务评分(1-5)',
  `value_score` int NOT NULL COMMENT '性价比评分(1-5)',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价内容',
  `images` text COLLATE utf8mb4_unicode_ci COMMENT '评价图片URL，逗号分隔',
  `anonymous` tinyint NOT NULL DEFAULT '0' COMMENT '是否匿名: 0-否 1-是',
  `status` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'VISIBLE' COMMENT '状态: VISIBLE-显示 HIDDEN-隐藏',
  `reply` text COLLATE utf8mb4_unicode_ci COMMENT '商家回复',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_no`),
  KEY `idx_house` (`house_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_review`
--

LOCK TABLES `t_review` WRITE;
/*!40000 ALTER TABLE `t_review` DISABLE KEYS */;
INSERT INTO `t_review` VALUES (1,'MS202606200001',1,2,'张三',5,5,5,4,5,'非常棒的体验！房间干净整洁，离海边很近，房东也很热情。强烈推荐！',NULL,0,'VISIBLE','感谢您的好评，欢迎下次再来！','2026-06-24 18:03:16','2026-06-21 09:00:00'),(2,'MS202606100001',4,3,'匿名用户',3,3,5,3,2,'位置确实好，但设施有些旧了，性价比一般。',NULL,1,'VISIBLE',NULL,NULL,'2026-06-12 15:00:00');
/*!40000 ALTER TABLE `t_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_system_config`
--

DROP TABLE IF EXISTS `t_system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_system_config` (
  `config_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置说明',
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_system_config`
--

LOCK TABLES `t_system_config` WRITE;
/*!40000 ALTER TABLE `t_system_config` DISABLE KEYS */;
INSERT INTO `t_system_config` VALUES ('default_discount','30','默认优惠金额（元）'),('free_cancel_hours','24','最晚免费取消（入住前小时数）'),('pay_timeout_minutes','30','待支付超时分钟数'),('review_available_days','30','评价入口保留天数（离店后）');
/*!40000 ALTER TABLE `t_system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_tag`
--

DROP TABLE IF EXISTS `t_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设施标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_tag`
--

LOCK TABLES `t_tag` WRITE;
/*!40000 ALTER TABLE `t_tag` DISABLE KEYS */;
INSERT INTO `t_tag` VALUES (1,'WiFi'),(5,'停车位'),(9,'冰箱'),(4,'厨房'),(3,'洗衣机'),(8,'浴缸'),(7,'热水器'),(6,'电视'),(2,'空调'),(10,'阳台');
/*!40000 ALTER TABLE `t_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MD5密码',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号（可选）',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员 USER-普通用户',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (1,'admin','0192023a7bbd73250516f069df18b500','系统管理员',NULL,'13800000000',NULL,'ADMIN',1,'2026-06-23 12:31:33','2026-06-24 10:18:41'),(2,'zhangsan','e10adc3949ba59abbe56e057f20f883e','张三',NULL,'13888888888',NULL,'USER',1,'2026-06-23 12:31:33','2026-06-23 12:31:33'),(3,'lisi','e10adc3949ba59abbe56e057f20f883e','李四',NULL,'13999999999',NULL,'USER',1,'2026-06-23 12:31:33','2026-06-23 12:31:33'),(4,'wangwu','e10adc3949ba59abbe56e057f20f883e','王五',NULL,'13777777777',NULL,'USER',1,'2026-06-23 12:31:33','2026-06-23 12:31:33');
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-24 10:18:41
