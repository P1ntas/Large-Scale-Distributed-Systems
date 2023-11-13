import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const ShoppingList = ({ username }) => {
  const [lists, setLists] = useState([]);
  const [newListName, setNewListName] = useState('');

  const createList = () => {
    if (newListName && !lists.includes(newListName)) {
      setLists([...lists, newListName]);
      setNewListName('');
    }
  };

  const deleteList = (name) => {
    setLists(lists.filter(list => list !== name));
  };

  return (
    <div>
      <h1>{username}'s Shopping Lists</h1>
      <input type="text" value={newListName} onChange={(e) => setNewListName(e.target.value)} />
      <button onClick={createList}>Create List</button>
      <ul>
        {lists.map(list => (
          <li key={list}>
            <Link to={`/list/${list}?username=${encodeURIComponent(username)}`}>{list}</Link>
            <button onClick={() => deleteList(list)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ShoppingList;
