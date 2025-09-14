import React, { useState } from 'react';
import { AuthService } from '../services/AuthService';

const Login = ({ onLogin }) => {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await AuthService.login(credentials);
      onLogin(response.data);
    } catch (error) {
      setError(error.response?.data?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value
    });
  };

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh', 
      backgroundColor: '#f5f5f5' 
    }}>
      <div className="card" style={{ width: '400px' }}>
        <h2 style={{ textAlign: 'center', marginBottom: '2rem' }}>
          🏗️ 城市基础设施预测性维护系统
        </h2>
        
        {error && (
          <div className="error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">用户名</label>
            <input
              type="text"
              id="username"
              name="username"
              value={credentials.username}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">密码</label>
            <input
              type="password"
              id="password"
              name="password"
              value={credentials.password}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <button 
            type="submit" 
            className="btn" 
            disabled={loading}
            style={{ width: '100%', marginTop: '1rem' }}
          >
            {loading ? '登录中...' : '登录'}
          </button>
        </form>

        <div style={{ marginTop: '2rem', padding: '1rem', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
          <h4 style={{ margin: '0 0 0.5rem 0' }}>测试账户:</h4>
          <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>管理员: admin / admin123</p>
          <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>经理: manager / manager123</p>
          <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>操作员: operator / operator123</p>
          <p style={{ margin: '0.25rem 0', fontSize: '0.9rem' }}>查看者: viewer / viewer123</p>
        </div>
      </div>
    </div>
  );
};

export default Login;