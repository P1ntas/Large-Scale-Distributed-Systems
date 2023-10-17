import React, { useState, useEffect } from 'react';
import axios from 'axios';

function ShoppingList() {
  const [items, setItems] = useState([]);
  const [newItem, setNewItem] = useState('');
  const [quantity, setQuantity] = useState('');
  const [editIndex, setEditIndex] = useState(null);

  useEffect(() => {
    // Fetch items from the backend when the component mounts
    axios.get('http://localhost:5001/items')
      .then((response) => setItems(response.data))
      .catch((error) => console.error(error));
  }, []);

  const handleAddItem = () => {
    // Send a POST request to add a new item
    axios.post('http://localhost:5001/items', { name: newItem, quantity })
      .then((response) => {
        console.log(response.data);
        // Fetch the updated list of items after adding
        axios.get('http://localhost:5001/items')
          .then((response) => setItems(response.data))
          .catch((error) => console.error(error));
        setNewItem('');
        setQuantity('');
      })
      .catch((error) => console.error(error));
  };

  const handleEditItem = (index) => {
    setEditIndex(index);
    setNewItem(items[index].name);
    setQuantity(items[index].quantity);
  };

  const handleSaveItem = (index) => {
    // Send a PUT request to update an existing item
    axios
      .put(`http://localhost:5001/items/${items[index].id}`, {
        name: newItem,
        quantity,
      })
      .then((response) => {
        console.log(response.data);
        // Fetch the updated list of items after editing
        axios
          .get('http://localhost:5001/items')
          .then((response) => setItems(response.data))
          .catch((error) => console.error(error));
        setNewItem('');
        setQuantity('');
        setEditIndex(null);
      })
      .catch((error) => console.error(error));
  };

  const handleDeleteItem = (index) => {
    // Send a DELETE request to remove an item
    axios.delete(`http://localhost:5001/items/${items[index].id}`)
      .then((response) => {
        console.log(response.data);
        // Fetch the updated list of items after deleting
        axios.get('http://localhost:5001/items')
          .then((response) => setItems(response.data))
          .catch((error) => console.error(error));
        setEditIndex(null);
      })
      .catch((error) => console.error(error));
  };

  return (
    <div>
      <h1>Shopping List</h1>
      <div>
        <input
          type="text"
          placeholder="Product Name"
          value={newItem}
          onChange={(e) => setNewItem(e.target.value)}
        />
        <input
          type="number"
          placeholder="Quantity"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
        />
        {editIndex === null ? (
          <button onClick={handleAddItem}>Add</button>
        ) : (
          <button onClick={handleSaveItem}>Save</button>
        )}
      </div>
      <ul>
        {items.map((item, index) => (
          <li key={item.id}>
            {editIndex === index ? (
              <div>
                <input
                  type="text"
                  value={newItem}
                  onChange={(e) => setNewItem(e.target.value)}
                />
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                />
                <button onClick={() => handleSaveItem(index)}>Save</button>
              </div>
            ) : (
              `${item.name} - ${item.quantity}`
            )}
            {editIndex === index ? (
              <button onClick={() => handleSaveItem(index)}>Save</button>
            ) : (
              <button onClick={() => handleEditItem(index)}>Edit</button>
            )}
            <button onClick={() => handleDeleteItem(index)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ShoppingList;
