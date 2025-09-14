import React from 'react';

const Header = ({ user, onLogout }) => {
  return (
    <header className="header">
      <h1>🏗️ 城市基础设施预测性维护系统</h1>
      <div className="user-info">
        <span>欢迎, {user.username} ({user.role})</span>
        <button className="btn secondary" onClick={onLogout}>
          退出登录
        </button>
      </div>
    </header>
  );
};

export default Header;