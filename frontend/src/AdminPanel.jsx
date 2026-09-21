import { useState } from 'react';
import { Check, Edit3, Plus, Save, Trash2, X } from 'lucide-react';

function AdminPanel({ apiUrl, token, products, setProducts, orders, onClose }) {
  const [form, setForm] = useState({ name: '', price: '', stockQuantity: '' });
  const [editingId, setEditingId] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [orderList, setOrderList] = useState(orders);

  const authHeaders = {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  function resetForm() {
    setForm({ name: '', price: '', stockQuantity: '' });
    setEditingId(null);
  }

  function editProduct(product) {
    setEditingId(product.id);
    setForm({ name: product.name, price: product.price, stockQuantity: product.stockQuantity });
  }

  async function saveProduct(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    const method = editingId ? 'PUT' : 'POST';
    const url = editingId ? `${apiUrl}/api/products/${editingId}` : `${apiUrl}/api/products`;
    try {
      const response = await fetch(url, {
        method,
        headers: authHeaders,
        body: JSON.stringify({
          name: form.name,
          price: Number(form.price),
          stockQuantity: Number(form.stockQuantity),
        }),
      });
      if (!response.ok) throw new Error('Could not save product');
      const product = await response.json();
      setProducts((current) => editingId
        ? current.map((item) => item.id === editingId ? product : item)
        : [...current, product]);
      resetForm();
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setBusy(false);
    }
  }

  async function removeProduct(id) {
    if (!window.confirm('Delete this product?')) return;
    const response = await fetch(`${apiUrl}/api/products/${id}`, {
      method: 'DELETE', headers: authHeaders,
    });
    if (response.ok) setProducts((current) => current.filter((product) => product.id !== id));
    else setError('Could not delete product. It may already have orders.');
  }

  async function updateOrderStatus(orderId, status) {
    const response = await fetch(`${apiUrl}/api/admin/orders/${orderId}/status`, {
      method: 'PATCH',
      headers: authHeaders,
      body: JSON.stringify({ status }),
    });
    if (response.ok) {
      const updated = await response.json();
      setOrderList((current) => current.map((order) => order.orderId === orderId ? updated : order));
    } else {
      setError('Could not update order status');
    }
  }

  return <section className="admin-panel" onClick={(event) => event.stopPropagation()}>
    <div className="drawer-title">
      <div><p className="eyebrow">Restricted workspace</p><h2>Admin desk</h2></div>
      <button onClick={onClose} aria-label="Close admin panel"><X /></button>
    </div>
    {error && <p className="admin-error">{error}</p>}
    <div className="admin-columns">
      <div>
        <div className="admin-section-heading"><h3>Products</h3><span>{products.length} items</span></div>
        <form className="admin-product-form" onSubmit={saveProduct}>
          <input required placeholder="Product name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
          <input required min="0.01" step="0.01" type="number" placeholder="Price" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} />
          <input required min="0" type="number" placeholder="Stock" value={form.stockQuantity} onChange={(event) => setForm({ ...form, stockQuantity: event.target.value })} />
          <button className="admin-save" disabled={busy} type="submit">{editingId ? <Save size={15} /> : <Plus size={15} />}{editingId ? 'Save changes' : 'Add product'}</button>
          {editingId && <button className="admin-cancel" type="button" onClick={resetForm}>Cancel</button>}
        </form>
        <div className="admin-list">{products.map((product) => <div className="admin-product" key={product.id}><div><strong>{product.name}</strong><p>${Number(product.price).toFixed(2)} / {product.stockQuantity} in stock</p></div><div className="admin-row-actions"><button onClick={() => editProduct(product)} aria-label={`Edit ${product.name}`}><Edit3 size={15} /></button><button onClick={() => removeProduct(product.id)} aria-label={`Delete ${product.name}`}><Trash2 size={15} /></button></div></div>)}</div>
      </div>
      <div>
        <div className="admin-section-heading"><h3>Orders</h3><span>{orderList.length} total</span></div>
        <div className="admin-list">{orderList.length === 0 ? <p className="message">No orders yet.</p> : orderList.map((order) => <div className="admin-order" key={order.orderId}><div><strong>#{order.orderId} / {order.productName}</strong><p>{order.quantity} units / ${Number(order.totalAmount).toFixed(2)}</p></div><select value={order.status} onChange={(event) => updateOrderStatus(order.orderId, event.target.value)}><option>PENDING</option><option>PROCESSING</option><option>SHIPPED</option><option>COMPLETED</option><option>CANCELLED</option></select></div>)}</div>
      </div>
    </div>
  </section>;
}

export default AdminPanel;
