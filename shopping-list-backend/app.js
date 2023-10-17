const express = require('express');
const cors = require('cors');
const sqlite3 = require('sqlite3').verbose();

const app = express();
const port = 5001;

app.use(express.json());
app.use(cors());

// Connect to the SQLite database
const db = new sqlite3.Database('shopping-list.db');

// Create a table for the shopping list if it doesn't exist
db.run(
  'CREATE TABLE IF NOT EXISTS shopping_list (id INTEGER PRIMARY KEY, name TEXT, quantity INTEGER)'
);

// API routes
app.get('/items', (req, res) => {
  db.all('SELECT * FROM shopping_list', (err, rows) => {
    if (err) {
      console.error(err);
      res.status(500).json({ error: 'Internal server error' });
      return;
    }
    res.json(rows);
  });
});

app.post('/items', (req, res) => {
  const { name, quantity } = req.body;
  db.run('INSERT INTO shopping_list (name, quantity) VALUES (?, ?)', [name, quantity], (err) => {
    if (err) {
      console.error(err);
      res.status(500).json({ error: 'Internal server error' });
      return;
    }
    res.json({ message: 'Item added successfully' });
  });
});

app.put('/items/:id', (req, res) => {
  const { name, quantity } = req.body;
  const { id } = req.params;
  db.run('UPDATE shopping_list SET name = ?, quantity = ? WHERE id = ?', [name, quantity, id], (err) => {
    if (err) {
      console.error(err);
      res.status(500).json({ error: 'Internal server error' });
      return;
    }
    res.json({ message: 'Item updated successfully' });
  });
});

app.delete('/items/:id', (req, res) => {
  const { id } = req.params;
  db.run('DELETE FROM shopping_list WHERE id = ?', id, (err) => {
    if (err) {
      console.error(err);
      res.status(500).json({ error: 'Internal server error' });
      return;
    }
    res.json({ message: 'Item deleted successfully' });
  });
});

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
