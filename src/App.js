// Import necessary libraries
import React, { useState } from 'react';

// Define the ShoppingList component
function ShoppingList() {
  // State for managing shopping list data
  const [items, setItems] = useState([]);
  const [newItemText, setNewItemText] = useState('');

  // Function to add a new item to the list
  const addItem = () => {
    if (newItemText.trim() !== '') {
      const newItem = { text: newItemText, checked: false, quantity: 1 };
      setItems([...items, newItem]);
      setNewItemText('');
    }
  };

  // Function to toggle the checked state of an item
  const toggleItem = (index) => {
    const updatedItems = [...items];
    updatedItems[index].checked = !updatedItems[index].checked;
    setItems(updatedItems);
  };

  // Function to delete an item
  const deleteItem = (index) => {
    const updatedItems = items.filter((_, i) => i !== index);
    setItems(updatedItems);
  }

  return (
    <div>
      <h1>Shopping List</h1>
      <input
        type="text"
        placeholder="Add item..."
        value={newItemText}
        onChange={(e) => setNewItemText(e.target.value)}
      />
      <button onClick={addItem}  className='addButton'>Add</button>
      <ul>
        {items.map((item, index) => (
          <li key={index}>
            <input
              type="checkbox"
              checked={item.checked}
              onChange={() => toggleItem(index)}
            />
            {item.text}
            <button onClick={() => deleteItem(index)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ShoppingList;
