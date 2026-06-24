CREATE DATABASE IF NOT EXISTS minsu_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE minsu_db;

-- ============================================================
-- 1. 用户表（含管理员和普通用户）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    password    VARCHAR(32)  NOT NULL COMMENT 'MD5密码',
    name        VARCHAR(64)           COMMENT '昵称',
    email       VARCHAR(128)          COMMENT '邮箱（可选）',
    phone       VARCHAR(32)           COMMENT '手机号（可选）',
    avatar      VARCHAR(255)          COMMENT '头像URL',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员 USER-普通用户',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 房源表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_house (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(128) NOT NULL COMMENT '房源名称',
    city         VARCHAR(64)  NOT NULL COMMENT '所在城市',
    price        DECIMAL(10,2) NOT NULL COMMENT '每晚价格',
    clean_fee    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '清洁费',
    type         VARCHAR(32)  NOT NULL COMMENT '类型: 单间/一居室/两居室/整套',
    rooms        VARCHAR(64)           COMMENT '房间描述',
    area         VARCHAR(32)           COMMENT '面积',
    capacity     INT          NOT NULL DEFAULT 2 COMMENT '最大可住人数',
    address      VARCHAR(255) NOT NULL COMMENT '详细地址',
    host         VARCHAR(64)  NOT NULL COMMENT '房东姓名',
    cover_image  VARCHAR(255)          COMMENT '封面图片URL',
    facilities   TEXT                  COMMENT '配套设施标签，逗号分隔',
    description  TEXT                  COMMENT '房源介绍',
    rule         TEXT                  COMMENT '入住规则',
    status       VARCHAR(8)   NOT NULL DEFAULT 'ON' COMMENT '上架状态: ON-上架 OFF-下架',
    rating       DECIMAL(2,1) NOT NULL DEFAULT 5.0 COMMENT '平均评分',
    sales        INT          NOT NULL DEFAULT 0 COMMENT '累计订单量',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_city (city),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房源表';

-- ============================================================
-- 3. 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_order (
    order_no     VARCHAR(32)  PRIMARY KEY COMMENT '订单编号',
    house_id     BIGINT       NOT NULL COMMENT '房源ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    guest_name   VARCHAR(64)  NOT NULL COMMENT '联系人姓名',
    phone        VARCHAR(32)  NOT NULL COMMENT '联系人手机号',
    check_in     DATE         NOT NULL COMMENT '入住日期',
    check_out    DATE         NOT NULL COMMENT '离店日期',
    nights       INT          NOT NULL COMMENT '入住晚数',
    guests       INT          NOT NULL COMMENT '入住人数',
    amount       DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
    paid         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已支付: 0-否 1-是',
    note         TEXT                  COMMENT '备注',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_house (house_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_check_in (check_in)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 4. 评价表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_review (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(32)  NOT NULL COMMENT '关联订单编号',
    house_id      BIGINT       NOT NULL COMMENT '关联房源ID',
    user_id       BIGINT       NOT NULL COMMENT '关联用户ID',
    user_name     VARCHAR(64)  NOT NULL COMMENT '展示名称',
    rating        INT          NOT NULL COMMENT '整体评分(1-5)',
    clean_score   INT          NOT NULL COMMENT '卫生评分(1-5)',
    location_score INT         NOT NULL COMMENT '位置评分(1-5)',
    service_score INT          NOT NULL COMMENT '服务评分(1-5)',
    value_score   INT          NOT NULL COMMENT '性价比评分(1-5)',
    content       TEXT         NOT NULL COMMENT '评价内容',
    images        TEXT                  COMMENT '评价图片URL，逗号分隔',
    anonymous     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
    status        VARCHAR(10)  NOT NULL DEFAULT 'VISIBLE' COMMENT '状态: VISIBLE-显示 HIDDEN-隐藏',
    reply         TEXT                  COMMENT '商家回复',
    reply_time    DATETIME              COMMENT '回复时间',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_order (order_no),
    INDEX idx_house (house_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================================
-- 5. 收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    house_id    BIGINT       NOT NULL COMMENT '房源ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE INDEX uk_user_house (user_id, house_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================================
-- 6. 房态锁定表（只存储 LOCKED 记录）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_availability (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_id    BIGINT       NOT NULL COMMENT '房源ID',
    date        DATE         NOT NULL COMMENT '锁定日期',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_house_date (house_id, date),
    INDEX idx_house (house_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房态锁定表';

-- ============================================================
-- 7. 城市配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_city (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(64) NOT NULL COMMENT '城市名称',
    sort  INT         NOT NULL DEFAULT 0 COMMENT '排序权重',
    UNIQUE INDEX uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='城市配置表';

-- ============================================================
-- 8. 设施标签表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_tag (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(64) NOT NULL COMMENT '标签名称',
    UNIQUE INDEX uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设施标签表';

-- ============================================================
-- 9. 系统配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_system_config (
    config_key   VARCHAR(64)  PRIMARY KEY COMMENT '配置键',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    description  VARCHAR(255)          COMMENT '配置说明'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 种子数据
-- ============================================================

-- 用户数据（admin123 的 MD5 = 0192023a7bbd73250516f069df18b500）
-- （123456 的 MD5 = e10adc3949ba59abbe56e057f20f883e）
INSERT IGNORE INTO t_user (username, password, name, phone, avatar, role, status) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', '13800000000', NULL, 'ADMIN', 1),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13888888888', NULL, 'USER', 1),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13999999999', NULL, 'USER', 1),
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13777777777', NULL, 'USER', 1);

-- 城市数据
INSERT IGNORE INTO t_city (name, sort) VALUES
('厦门', 1),
('青岛', 2),
('三亚', 3),
('成都', 4),
('杭州', 5),
('大理', 6);

-- 设施标签数据
INSERT IGNORE INTO t_tag (name) VALUES
('WiFi'),
('空调'),
('洗衣机'),
('厨房'),
('停车位'),
('电视'),
('热水器'),
('浴缸'),
('冰箱'),
('阳台');

-- 系统配置数据
INSERT IGNORE INTO t_system_config (config_key, config_value, description) VALUES
('default_discount', '30', '默认优惠金额（元）'),
('pay_timeout_minutes', '30', '待支付超时分钟数'),
('free_cancel_hours', '24', '最晚免费取消（入住前小时数）'),
('review_available_days', '30', '评价入口保留天数（离店后）');

-- 房源数据
INSERT IGNORE INTO t_house (name, city, price, clean_fee, type, rooms, area, capacity, address, host, cover_image, facilities, description, rule, status, rating, sales) VALUES
('海边日落小屋', '厦门', 399.00, 50.00, '整套', '1室1厅1卫', '60㎡', 2, '厦门市思明区曾厝垵123号', '李房东', '/uploads/2026/06/24/new-houses/house-1.jpg', 'WiFi,空调,洗衣机,厨房', '靠近海边的温馨小屋，步行5分钟到海滩，适合情侣度假。', '14:00后入住，12:00前离店。禁止携带宠物。', 'ON', 4.8, 12),
('山景别墅', '大理', 888.00, 100.00, '整套', '3室2厅2卫', '200㎡', 6, '大理市古城区苍山脚下', '王房东', '/uploads/2026/06/24/new-houses/house-2.jpg', 'WiFi,空调,洗衣机,厨房,停车位,冰箱,阳台', '苍山洱海全景别墅，独享山景，适合家庭聚会。', '14:00后入住，12:00前离店。', 'ON', 4.9, 8),
('市中心公寓', '杭州', 299.00, 40.00, '一居室', '1室1厅1卫', '45㎡', 2, '杭州市西湖区文三路88号', '陈房东', '/uploads/2026/06/24/new-houses/house-3.jpg', 'WiFi,空调,洗衣机,电视,热水器', '紧邻西湖景区，交通便利，地铁口50米。', '14:00后入住，12:00前离店。', 'ON', 4.6, 25),
('海景大床房', '三亚', 599.00, 60.00, '单间', '1室1卫', '35㎡', 2, '三亚市海棠湾免税店旁', '林房东', '/uploads/2026/06/24/new-houses/house-4.jpg', 'WiFi,空调,电视,热水器,浴缸', '一线海景房，推开窗就是大海，度假首选。', '14:00后入住，12:00前离店。', 'ON', 4.7, 18),
('老城四合院', '成都', 459.00, 50.00, '整套', '2室1厅1卫', '80㎡', 4, '成都市青羊区宽窄巷子旁', '赵房东', '/uploads/2026/06/24/new-houses/house-5.jpg', 'WiFi,空调,洗衣机,厨房,电视,冰箱', '传统四合院改造，闹中取静，体验老成都生活。', '14:00后入住，12:00前离店。', 'ON', 4.5, 10),
('山间木屋', '青岛', 359.00, 45.00, '两居室', '2室1厅1卫', '70㎡', 4, '青岛市崂山区仰口风景区', '孙房东', '/uploads/2026/06/24/new-houses/house-6.jpg', 'WiFi,空调,洗衣机,厨房,停车位,阳台', '崂山脚下木屋，远离城市喧嚣，亲近自然。', '14:00后入住，12:00前离店。', 'OFF', 4.4, 5);

-- 订单数据
INSERT IGNORE INTO t_order (order_no, house_id, user_id, guest_name, phone, check_in, check_out, nights, guests, amount, status, paid, note, create_time) VALUES
('MS202606200001', 1, 2, '张三', '13888888888', '2026-07-01', '2026-07-03', 2, 2, 818.00, 'REVIEWED', 1, '需要加床', '2026-06-20T10:00:00'),
('MS202606200002', 3, 2, '张三', '13888888888', '2026-07-10', '2026-07-12', 2, 2, 608.00, 'COMPLETED', 1, NULL, '2026-06-20T14:00:00'),
('MS202606210001', 2, 3, '李四', '13999999999', '2026-07-05', '2026-07-08', 3, 4, 2664.00, 'BOOKED', 1, '家庭出行', '2026-06-21T09:00:00'),
('MS202606220001', 4, 4, '王五', '13777777777', '2026-07-15', '2026-07-17', 2, 2, 1188.00, 'WAIT_PAY', 0, NULL, '2026-06-22T16:00:00'),
('MS202606230001', 1, 3, '李四', '13999999999', '2026-08-01', '2026-08-03', 2, 2, 818.00, 'PENDING', 0, '蜜月旅行', '2026-06-23T08:00:00'),
('MS202606180001', 5, 2, '张三', '13888888888', '2026-06-18', '2026-06-20', 2, 2, 918.00, 'CANCELLED', 0, '行程变更', '2026-06-18T10:00:00'),
('MS202606150001', 2, 4, '王五', '13777777777', '2026-06-25', '2026-06-28', 3, 3, 2664.00, 'REFUNDING', 1, '临时有事需要退款', '2026-06-15T11:00:00'),
('MS202606100001', 4, 3, '李四', '13999999999', '2026-06-10', '2026-06-12', 2, 2, 1188.00, 'REFUNDED', 0, NULL, '2026-06-10T09:00:00');

-- 评价数据（仅 VISIBLE 状态评价参与评分计算）
INSERT IGNORE INTO t_review (order_no, house_id, user_id, user_name, rating, clean_score, location_score, service_score, value_score, content, images, anonymous, status, reply, reply_time, create_time) VALUES
('MS202606200001', 1, 2, '张三', 5, 5, 5, 4, 5, '非常棒的体验！房间干净整洁，离海边很近，房东也很热情。强烈推荐！', NULL, 0, 'VISIBLE', '感谢您的好评，欢迎下次再来！', '2026-06-21T10:00:00', '2026-06-21T09:00:00'),
('MS202606100001', 4, 3, '匿名用户', 3, 3, 5, 3, 2, '位置确实好，但设施有些旧了，性价比一般。', NULL, 1, 'VISIBLE', NULL, NULL, '2026-06-12T15:00:00');

-- 收藏数据
INSERT IGNORE INTO t_favorite (user_id, house_id) VALUES
(2, 2),
(2, 4),
(3, 1),
(3, 3),
(4, 2),
(4, 5);

-- 房态锁定数据
INSERT IGNORE INTO t_availability (house_id, date) VALUES
(1, '2026-07-20'),
(1, '2026-07-21'),
(3, '2026-08-01');
