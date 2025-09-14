-- Sample infrastructure assets
INSERT INTO infrastructure_assets (id, asset_id, name, type, description, latitude, longitude, status, maintenance_priority, installation_date, last_maintenance) VALUES
(1, 'BRG_001', '黄河大桥', 'BRIDGE', '连接城市南北的重要桥梁，长度1.2公里', 39.9042, 116.4074, 'OPERATIONAL', 3, '2015-03-15 00:00:00', '2023-06-15 00:00:00'),
(2, 'RD_001', '中央大道', 'ROAD', '市区主要交通干道，双向8车道', 39.9100, 116.4200, 'OPERATIONAL', 2, '2018-08-20 00:00:00', '2023-08-01 00:00:00'),
(3, 'TUN_001', '地铁隧道1号线', 'TUNNEL', '地下轨道交通隧道，全长15公里', 39.8950, 116.3950, 'MAINTENANCE_REQUIRED', 7, '2010-12-01 00:00:00', '2023-03-10 00:00:00'),
(4, 'BLD_001', '市政府大楼', 'BUILDING', '20层综合办公楼，总面积50000平方米', 39.9150, 116.4050, 'OPERATIONAL', 1, '2012-05-10 00:00:00', '2023-07-20 00:00:00'),
(5, 'BRG_002', '长江大桥', 'BRIDGE', '跨江大桥，悬索桥结构', 39.8800, 116.4300, 'CRITICAL', 9, '2008-09-15 00:00:00', '2022-12-15 00:00:00');