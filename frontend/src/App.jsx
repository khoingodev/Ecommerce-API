import { useEffect, useMemo, useState } from 'react';
import { ArrowRight, LogIn, Minus, Plus, Search, ShoppingBag, Sparkles, Trash2, UserPlus, X } from 'lucide-react';
import AdminPanel from './AdminPanel.jsx';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const headers = (token) => ({
  ...(token ? { Authorization: `Bearer ${token}` } : {}),
  'Content-Type': 'application/json',
});

function App() {
  const [products, setProducts] = useState([]);
  const [query, setQuery] = useState('');
  const [cart, setCart] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('cart') ?? '[]');
    } catch {
      return [];
    }
  });
  const [cartOpen, setCartOpen] = useState(false);
  const [authOpen, setAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ email: '', password: '' });
  const [token, setToken] = useState(() => localStorage.getItem('accessToken'));
  const [role, setRole] = useState(() => localStorage.getItem('role'));
  const [adminOpen, setAdminOpen] = useState(false);
  const [adminOrders, setAdminOrders] = useState([]);
  const [myOrders, setMyOrders] = useState([]);
  const [ordersOpen, setOrdersOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetch(`${API_URL}/api/products`, { headers: headers() })
      .then((response) => {
        if (!response.ok) throw new Error();
        return response.json();
      })
      .then(setProducts)
      .catch(() => setMessage('Backend is unavailable. Start Spring Boot on port 8080.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);

  const visibleProducts = useMemo(() => products.filter((product) =>
    product.name.toLowerCase().includes(query.toLowerCase().trim())), [products, query]);
  const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  function add(product) {
    setCart((current) => {
      const existing = current.find((item) => item.id === product.id);
      if (existing) return current.map((item) => item.id === product.id
        ? { ...item, quantity: Math.min(item.quantity + 1, product.stockQuantity) } : item);
      return [...current, { ...product, quantity: 1 }];
    });
    setCartOpen(true);
  }

  function change(id, amount) {
    setCart((current) => current.map((item) => item.id === id
      ? { ...item, quantity: item.quantity + amount } : item).filter((item) => item.quantity > 0));
  }

  async function checkout() {
    if (!token) {
      setAuthOpen(true);
      setMessage('Sign in before checkout.');
      return;
    }
    try {
      for (const item of cart) {
        const response = await fetch(`${API_URL}/api/orders`, {
          method: 'POST', headers: headers(token),
          body: JSON.stringify({ productId: item.id, quantity: item.quantity }),
        });
        if (!response.ok) throw new Error();
      }
      setCart([]); setCartOpen(false); setMessage('Order placed successfully.');
    } catch { setMessage('Checkout failed. Check product stock.'); }
    window.setTimeout(() => setMessage(''), 2400);
  }

  async function authenticate(event) {
    event.preventDefault();
    const endpoint = authMode === 'login' ? 'login' : 'register';
    try {
      const response = await fetch(`${API_URL}/api/auth/${endpoint}`, {
        method: 'POST', headers: headers(), body: JSON.stringify(authForm),
      });
      const body = await response.json();
      if (!response.ok) throw new Error(body.message ?? 'Authentication failed');
      localStorage.setItem('accessToken', body.accessToken);
      localStorage.setItem('role', body.role);
      setToken(body.accessToken);
      setRole(body.role);
      setAuthOpen(false);
      setAuthForm({ email: '', password: '' });
      setMessage(authMode === 'login' ? 'Welcome back.' : 'Account created. You are signed in.');
    } catch (error) {
      setMessage(error.message);
    }
    window.setTimeout(() => setMessage(''), 2600);
  }

  function signOut() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('role');
    setToken(null);
    setRole(null);
    setMessage('Signed out.');
  }

  async function openOrders() {
    if (!token) {
      setAuthOpen(true);
      setMessage('Sign in to view your orders.');
      return;
    }
    const response = await fetch(`${API_URL}/api/orders/mine`, { headers: headers(token) });
    if (response.ok) {
      setMyOrders(await response.json());
      setOrdersOpen(true);
    } else {
      setMessage('Could not load your orders.');
    }
  }

  return <div className="app-shell">
    <header className="topbar">
      <a className="brand" href="/"><span className="brand-mark"><Sparkles size={16} /></span>Northstar <span className="muted">/ market</span></a>
      <nav><a href="#catalog">Catalog</a><a href="#about">Our edit</a></nav>
      <div className="topbar-actions">{token && <button className="account-button" onClick={openOrders}>Orders</button>}{role === 'ADMIN' && <button className="account-button" onClick={async () => { setAdminOpen(true); const response = await fetch(`${API_URL}/api/admin/orders`, { headers: headers(token) }); if (response.ok) setAdminOrders(await response.json()); }} >Admin</button>}<button className="account-button" onClick={() => token ? signOut() : setAuthOpen(true)}>{token ? 'Sign out' : <><LogIn size={16} /> Sign in</>}</button><button className="bag-button" onClick={() => setCartOpen(true)}><ShoppingBag size={17} /> Bag <b>{itemCount}</b></button></div>
    </header>
    <main>
      <section className="hero" id="about">
        <div><p className="eyebrow">Curated daily goods / 01</p><h1>Useful things, <em>beautifully</em> chosen.</h1><p className="hero-text">A small, considered collection for workdays, weekends, and everywhere in between.</p><a className="text-link" href="#catalog">Explore the collection <ArrowRight size={16} /></a></div>
        <div className="hero-art"><i className="sun" /><i className="vase" /><span>FORM / FUNCTION</span></div>
      </section>
      <section className="catalog" id="catalog">
        <div className="section-heading"><div><p className="eyebrow">The collection</p><h2>Objects with a point of view.</h2></div><label className="search"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search products" /></label></div>
        {message && <p className="message">{message}</p>}
        {loading && <p className="message">Loading the collection...</p>}
        <div className="product-grid">{visibleProducts.map((product, index) => <article className="product-card" key={product.id}>
          <div className={`product-image image-${index % 3 + 1}`}><span>{String(index + 1).padStart(2, '0')}</span></div>
          <div className="product-meta"><div><h3>{product.name}</h3><p>{product.stockQuantity} available</p></div><strong>${Number(product.price).toFixed(2)}</strong></div>
          <button className="add-button" disabled={!product.stockQuantity} onClick={() => add(product)}>{product.stockQuantity ? 'Add to bag' : 'Sold out'} <Plus size={15} /></button>
        </article>)}</div>
      </section>
    </main>
    {cartOpen && <div className="backdrop" onClick={() => setCartOpen(false)}><aside className="drawer" onClick={(event) => event.stopPropagation()}><div className="drawer-title"><div><p className="eyebrow">Your selection</p><h2>Shopping bag</h2></div><button onClick={() => setCartOpen(false)} aria-label="Close bag"><X /></button></div>{!cart.length ? <div className="empty"><ShoppingBag size={30} /><p>Your bag is waiting.</p></div> : <><div className="cart-items">{cart.map((item) => <div className="cart-item" key={item.id}><div className={`thumb image-${item.id % 3 + 1}`} /><div><strong>{item.name}</strong><p>${Number(item.price).toFixed(2)}</p><div className="quantity"><button onClick={() => change(item.id, -1)}><Minus size={13} /></button><b>{item.quantity}</b><button onClick={() => change(item.id, 1)}><Plus size={13} /></button></div></div><button className="remove" onClick={() => setCart((current) => current.filter((cartItem) => cartItem.id !== item.id))}><Trash2 size={15} /></button></div>)}</div><div className="cart-footer"><div><span>Total</span><strong>${total.toFixed(2)}</strong></div><button className="checkout" onClick={checkout}>Checkout <ArrowRight size={16} /></button></div></>}</aside></div>}
    {authOpen && <div className="backdrop" onClick={() => setAuthOpen(false)}><section className="auth-panel" onClick={(event) => event.stopPropagation()}><button className="auth-close" onClick={() => setAuthOpen(false)} aria-label="Close authentication"><X /></button><p className="eyebrow">Northstar account</p><h2>{authMode === 'login' ? 'Welcome back.' : 'Join the market.'}</h2><form onSubmit={authenticate}><label>Email<input type="email" required value={authForm.email} onChange={(event) => setAuthForm({ ...authForm, email: event.target.value })} /></label><label>Password<input type="password" required minLength="8" value={authForm.password} onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })} /></label><button className="checkout" type="submit">{authMode === 'login' ? <><LogIn size={16} /> Sign in</> : <><UserPlus size={16} /> Create account</>}</button></form><button className="switch-auth" onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>{authMode === 'login' ? 'Need an account? Register' : 'Already have an account? Sign in'}</button></section></div>}
    {adminOpen && <div className="backdrop" onClick={() => setAdminOpen(false)}><AdminPanel apiUrl={API_URL} token={token} products={products} setProducts={setProducts} orders={adminOrders} onClose={() => setAdminOpen(false)} /></div>}
    {ordersOpen && <div className="backdrop" onClick={() => setOrdersOpen(false)}><section className="orders-panel" onClick={(event) => event.stopPropagation()}><div className="drawer-title"><div><p className="eyebrow">Account history</p><h2>My orders</h2></div><button onClick={() => setOrdersOpen(false)} aria-label="Close orders"><X /></button></div>{myOrders.length === 0 ? <p className="message">You have not placed an order yet.</p> : <div className="my-orders">{myOrders.map((order) => <div className="my-order" key={order.orderId}><div><strong>Order #{order.orderId}</strong><p>Product #{order.productId} x {order.quantity}</p></div><div><span className="status">{order.status}</span><strong>${Number(order.totalAmount).toFixed(2)}</strong></div></div>)}</div>}</section></div>}
  </div>;
}

export default App;
