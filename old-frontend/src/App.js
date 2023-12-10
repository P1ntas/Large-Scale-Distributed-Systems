import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import UsernameForm from './components/UsernameForm';
import ShoppingList from './components/ShoppingList';
import ShoppingListDetail from './components/ShoppingListDetail';

const App = () => {
  const [username, setUsername] = useState('');

  return (
    <Router>
      <Routes>
        <Route exact path="/" element={<UsernameForm setUsername={setUsername} />} />
        <Route path="/shopping-list" element={<ShoppingList username={username} />} />
        <Route path="/list/:listName" element={<ShoppingListDetail />} />
      </Routes>
    </Router>
  );
};

export default App;
