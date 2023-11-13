import React, { useState } from 'react';
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

  const handleAddItem = () => {
    if (newItemName && !items[newItemName]) {
      setItems({ ...items, [newItemName]: 1 });
      setNewItemName('');
    }
  };

  const handleEditToggle = (itemName) => {
    setIsEditing(itemName);
    setEditItemName(itemName);
    setEditQuantity(items[itemName]);
    setOriginalItem({ name: itemName, quantity: items[itemName] });
  };

  const handleEditItem = (oldItemName) => {
    const newItems = { ...items };
    delete newItems[oldItemName];
    newItems[editItemName] = editQuantity;

    setItems(newItems);
    setIsEditing(null);
  };

  const handleCancelEdit = () => {
    const newItems = { ...items };
    delete newItems[isEditing];
    newItems[originalItem.name] = originalItem.quantity;

    setItems(newItems);
    setIsEditing(null);
    setOriginalItem({ name: '', quantity: 1 });
  };

  const handleDeleteItem = (itemName) => {
    const newItems = { ...items };
    delete newItems[itemName];
    setItems(newItems);
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
        {Object.entries(items).map(([itemName, quantity]) => (
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
                {itemName} - {quantity} 
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
