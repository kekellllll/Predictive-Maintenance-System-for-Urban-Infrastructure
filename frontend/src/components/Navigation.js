import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Navigation = () => {
  const location = useLocation();

  const navItems = [
    { path: '/dashboard', label: '仪表板', icon: '📊' },
    { path: '/assets', label: '基础设施资产', icon: '🏗️' },
    { path: '/sensors', label: '传感器数据', icon: '📡' },
    { path: '/predictions', label: '预测分析', icon: '🔮' }
  ];

  return (
    <nav className="navigation">
      <ul className="nav-links">
        {navItems.map(item => (
          <li key={item.path}>
            <Link 
              to={item.path} 
              className={location.pathname === item.path ? 'active' : ''}
            >
              <span style={{ marginRight: '0.5rem' }}>{item.icon}</span>
              {item.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default Navigation;