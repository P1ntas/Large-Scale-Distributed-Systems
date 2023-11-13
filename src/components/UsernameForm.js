import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const UsernameForm = ({ setUsername }) => {
  const [username, setTempUsername] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    setUsername(username);
    navigate('/shopping-list');
  };

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Username:
        <input type="text" value={username} onChange={e => setTempUsername(e.target.value)} required />
      </label>
      <button type="submit">Submit</button>
    </form>
  );
};

export default UsernameForm;
