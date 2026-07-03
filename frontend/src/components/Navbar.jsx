import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    setMobileOpen(false);
    navigate('/');
  };

  const closeMenu = () => setMobileOpen(false);

  return (
    <nav className="glass sticky top-0 z-50 transition-all duration-300">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-20 items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3 text-2xl font-extrabold tracking-tight text-stone-100 transition-transform hover:scale-105 hover:text-amber-400" onClick={closeMenu}>
            <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 shadow-[0_0_15px_rgba(245,158,11,0.4)]">
              <svg className="h-6 w-6 text-stone-950" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
              </svg>
            </div>
            SeatR
          </Link>

          {/* Desktop Nav */}
          <div className="hidden items-center gap-1 md:flex">
            <NavLink to="/" onClick={closeMenu}>Home</NavLink>
            <NavLink to="/events" onClick={closeMenu}>Events</NavLink>


            {user && user.role === 'ADMIN' && (
              <NavLink to="/venues" onClick={closeMenu}>Venues</NavLink>
            )}
            {user && user.role === 'ORGANISER' && (
              <NavLink to="/manage-events" onClick={closeMenu}>Manage Events</NavLink>
            )}

            {!user ? (
              <>
                <NavLink to="/login" onClick={closeMenu}>Login</NavLink>
                <Link
                  to="/register"
                  className="ml-2 btn-primary"
                  onClick={closeMenu}
                >
                  Register
                </Link>
              </>
            ) : (
              <div className="ml-4 flex items-center gap-3 border-l border-stone-700 pl-4">
                <span className="text-sm text-stone-300">
                  {user.firstName} <span className="rounded bg-stone-700 px-1.5 py-0.5 text-xs font-medium text-stone-300">{user.role}</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="rounded-lg bg-stone-800 px-3 py-1.5 text-sm font-medium transition-colors hover:bg-rose-500 hover:text-white"
                >
                  Logout
                </button>
              </div>
            )}
          </div>

          {/* Mobile Hamburger */}
          <button
            className="inline-flex items-center justify-center rounded-lg p-2 text-stone-300 hover:bg-stone-800 hover:text-white md:hidden"
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label="Toggle navigation menu"
          >
            {mobileOpen ? (
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            ) : (
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className="border-t border-stone-800 md:hidden bg-stone-900/95 backdrop-blur-lg">
          <div className="space-y-1 px-4 py-3">
            <MobileLink to="/" onClick={closeMenu}>Home</MobileLink>
            <MobileLink to="/events" onClick={closeMenu}>Events</MobileLink>


            {user && user.role === 'ADMIN' && (
              <MobileLink to="/venues" onClick={closeMenu}>Venues</MobileLink>
            )}
            {user && user.role === 'ORGANISER' && (
              <MobileLink to="/manage-events" onClick={closeMenu}>Manage Events</MobileLink>
            )}

            {!user ? (
              <>
                <MobileLink to="/login" onClick={closeMenu}>Login</MobileLink>
                <MobileLink to="/register" onClick={closeMenu}>Register</MobileLink>
              </>
            ) : (
              <>
                <div className="border-t border-stone-800 pt-3">
                  <p className="px-3 text-sm text-stone-400">
                    Signed in as <span className="font-medium text-amber-500">{user.firstName}</span>
                  </p>
                </div>
                <button
                  onClick={handleLogout}
                  className="mt-1 w-full rounded-lg px-3 py-2 text-left text-sm font-medium text-rose-400 hover:bg-stone-800"
                >
                  Logout
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}

function NavLink({ to, children, onClick }) {
  return (
    <Link
      to={to}
      onClick={onClick}
      className="rounded-lg px-3 py-2 text-sm font-medium text-stone-300 transition-colors hover:bg-stone-800 hover:text-amber-400"
    >
      {children}
    </Link>
  );
}

function MobileLink({ to, children, onClick }) {
  return (
    <Link
      to={to}
      onClick={onClick}
      className="block rounded-lg px-3 py-2 text-sm font-medium text-stone-300 hover:bg-stone-800 hover:text-amber-400"
    >
      {children}
    </Link>
  );
}
