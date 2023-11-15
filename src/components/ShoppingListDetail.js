import React, { useState, useEffect } from 'react';
import localForage from 'localforage';
import { useParams, useLocation } from 'react-router-dom';

function useQuery() {
    return new URLSearchParams(useLocation().search);
}

const ShoppingListDetail = () => {
  const { listName } = useParams();
  const [items, setItems] = useState({});
  const [newItemName, setNewItemName] = useState('');
  const [editItemName, setEditItemName] = useState('');
  const [editQuantity, setEditQuantity] = useState(1);
  const [isEditing, setIsEditing] = useState(null);
  const [originalItem, setOriginalItem] = useState({ name: '', quantity: 1 });
  const query = useQuery();
  const username = query.get('username');

  useEffect(() => {
    const loadItems = async () => {
      const data = await localForage.getItem(username);
      const list = data.shopping_lists.find(l => l.list_name === listName);
      if (list) {
        setItems(list.products.reduce((acc, product) => ({ ...acc, [product.name]: product.quantity }), {}));
      }
    };
  
    loadItems();
  }, [username, listName]);

  const updateStorageItems = async (newItems) => {
    const data = await localForage.getItem(username);
    const updatedLists = data.shopping_lists.map(list => 
      list.list_name === listName ? 
      { ...list, 
        last_edited: new Date().toISOString(), 
        products: Object.entries(newItems).map(([name, quantity]) => ({ name, quantity }))
      } : 
      list
    );
  
    await localForage.setItem(username, { ...data, shopping_lists: updatedLists });
  };

  const handleAddItem = async () => {
    if (newItemName && !items[newItemName]) {
      const newItems = { ...items, [newItemName]: { quantity: 1, last_edited: new Date().toISOString() }};
      setItems(newItems);
      setNewItemName('');
      await updateStorageItems(newItems);
    }
  };
  

  const handleEditToggle = (itemName) => {
    setIsEditing(itemName);
    setEditItemName(itemName);
    setEditQuantity(items[itemName]);
    setOriginalItem({ name: itemName, quantity: items[itemName] });
  };

  const handleEditItem = async (oldItemName) => {
    const newItems = { ...items };
    if (oldItemName !== editItemName) {
      delete newItems[oldItemName];
    }
    newItems[editItemName] = { quantity: editQuantity, last_edited: new Date().toISOString() };
  
    setItems(newItems);
    setIsEditing(null);
    await updateStorageItems(newItems);
  };
  

  const handleCancelEdit = () => {
    const newItems = { ...items };
    delete newItems[isEditing];
    newItems[originalItem.name] = originalItem.quantity;

    setItems(newItems);
    setIsEditing(null);
    setOriginalItem({ name: '', quantity: 1 });
  };

  const handleDeleteItem = async (itemName) => {
    const newItems = { ...items };
    delete newItems[itemName];
    setItems(newItems);
    await updateStorageItems(newItems);
  };

  return (
    <div>
      <h2>{username}'s Shopping List: {listName}</h2>
      <input
        type="text"
        placeholder="Add new item"
        value={newItemName}
        onChange={(e) => setNewItemName(e.target.value)}
      />
      <button onClick={handleAddItem}>Add Item</button>
      
      <ul>
        {Object.entries(items).map(([itemName, itemDetails]) => (
          <li key={itemName}>
            {isEditing === itemName ? (
              <>
                <input
                  type="text"
                  value={editItemName}
                  onChange={(e) => setEditItemName(e.target.value)}
                />
                <input
                  type="number"
                  value={editQuantity}
                  min="1"
                  onChange={(e) => setEditQuantity(parseInt(e.target.value))}
                />
                <button onClick={() => handleEditItem(itemName)}>Save</button>
                <button onClick={handleCancelEdit}>Cancel</button>
              </>
            ) : (
              <>
                {itemName} - {itemDetails.quantity}
                <button onClick={() => handleEditToggle(itemName)}>Edit</button>
                <button onClick={() => handleDeleteItem(itemName)}>Delete</button>
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ShoppingListDetail;
