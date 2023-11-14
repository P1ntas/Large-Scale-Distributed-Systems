import React, { useState, useEffect } from 'react';
import localForage from 'localforage';
import { Link } from 'react-router-dom';

const ShoppingList = ({ username }) => {
  const [lists, setLists] = useState([]);
  const [newListName, setNewListName] = useState('');

  useEffect(() => {
    const loadLists = async () => {
      const data = await localForage.getItem(username);
      if (data && data.shopping_lists) {
        setLists(data.shopping_lists);
      }
    };
  
    if (username) {
      loadLists();
    }
  }, [username]);

  useEffect(() => {
    const loadLists = async () => {
      const data = await localForage.getItem(username);
      if (data && data.shopping_lists) {
        setLists(data.shopping_lists.map(list => list.list_name));
      }
    };
  
    loadLists();
  }, [username]);

  const updateStorage = async (updatedLists) => {
    const data = await localForage.getItem(username);
    await localForage.setItem(username, { ...data, shopping_lists: updatedLists });
  };
  
  const createList = async () => {
    if (newListName && !lists.some(list => list.list_name === newListName)) {
      const newList = {
        list_name: newListName,
        last_edited: new Date().toISOString(),
        products: []
      };
      const updatedLists = [...lists, newList];
      setLists(updatedLists);
      setNewListName('');
      await updateStorage(updatedLists);
    }
  };
  
  const deleteList = async (name) => {
    const updatedLists = lists.filter(list => list.list_name !== name);
    setLists(updatedLists);
    await updateStorage(updatedLists);
  };

  return (
    <div>
      <h1>{username}'s Shopping Lists</h1>
      <input type="text" value={newListName} onChange={(e) => setNewListName(e.target.value)} />
      <button onClick={createList}>Create List</button>
      <ul>
        {lists.map(list => (
          <li key={list.list_name}>
            <Link to={`/list/${list.list_name}?username=${encodeURIComponent(username)}`}>
              {list.list_name}
            </Link>
            <button onClick={() => deleteList(list.list_name)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ShoppingList;
